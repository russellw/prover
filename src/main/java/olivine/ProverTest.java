package olivine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

final class ProverTest {
  private static final Pattern TPTP_DOMAIN_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z][a-zA-Z]");
  private static final Pattern TPTP_PROBLEM_PATTERN =
      Pattern.compile("[a-zA-Z][a-zA-Z][a-zA-Z]\\d\\d\\d.\\d+(\\.\\d+)?");

  private static final Pattern DIMACS_STATUS_PATTERN = Pattern.compile("c.* (SAT|UNSAT) .*");
  private static final Pattern TPTP_STATUS_PATTERN = Pattern.compile("%\\s*Status\\s*:\\s*(\\w+)");

  private static boolean shuffle;
  private static Random random = new Random();
  private static int maxAttempted = -1;
  private static long steps = 100;

  private static final Option[] OPTIONS =
      new Option[] {
        new Option('h', "help", null, "show help") {
          @Override
          public void accept(String arg) {
            Option.help(OPTIONS);
          }
        },
        new Option('V', "version", null, "show version") {
          @Override
          public void accept(String arg) {
            Etc.printVersion();
            System.exit(0);
          }
        },
        new Option('m', "max", "N", "max number of problems to attempt") {
          @Override
          public void accept(String arg) {
            maxAttempted = Integer.parseInt(arg);
          }
        },
        new Option('n', "steps", "N", "number of superposition steps to try") {
          @Override
          public void accept(String arg) {
            steps = Long.parseLong(arg);
          }
        },
        new Option('r', "random", "seed", "deterministic random sequence") {
          @Override
          public void accept(String arg) {
            random = new Random(arg.hashCode());
          }
        },
        new Option('s', "shuffle", null, "shuffle problem list") {
          @Override
          public void accept(String arg) {
            shuffle = true;
          }
        },
      };

  private ProverTest() {}

  private static boolean isTptpFirstOrder(String file) {
    return file.endsWith(".p") && !file.contains("^");
  }

  private static String status(String file) throws IOException {
    try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String s;
      switch (Etc.extension(file)) {
        case "p" -> {
          while ((s = reader.readLine()) != null) {
            if (!s.isBlank() && s.charAt(0) != '%') break;
            var matcher = TPTP_STATUS_PATTERN.matcher(s);
            if (matcher.matches()) return matcher.group(1);
          }
        }
        case "cnf" -> {
          while ((s = reader.readLine()) != null) {
            if (!s.isBlank() && s.charAt(0) != 'c') break;
            var matcher = DIMACS_STATUS_PATTERN.matcher(s);
            if (matcher.matches()) return matcher.group(1);
          }
        }
      }
    }
    return null;
  }

  private static double time(long start) {
    return (System.currentTimeMillis() - start) * 0.001;
  }

  public static void main(String[] args) throws IOException {
    Option.parse(OPTIONS, args);
    List<String> files = new ArrayList<>();
    for (var s : Option.positionalArgs) {
      // accept unadorned TPTP
      if (s.equalsIgnoreCase("tptp")) {
        for (var file :
            Files.walk(Path.of(Etc.tptp()))
                .filter(p -> !Files.isDirectory(p))
                .map(Path::toString)
                .toArray(String[]::new)) if (isTptpFirstOrder(file)) files.add(file);
        continue;
      }

      // accept unadorned TPTP domain
      var matcher = TPTP_DOMAIN_PATTERN.matcher(s);
      if (matcher.matches()) {
        s = s.toUpperCase(Locale.ROOT);
        for (var file :
            Files.walk(Path.of(Etc.tptp(), "Problems", s))
                .filter(p -> !Files.isDirectory(p))
                .map(Path::toString)
                .toArray(String[]::new)) if (isTptpFirstOrder(file)) files.add(file);
        continue;
      }

      // accept unadorned TPTP problem name
      matcher = TPTP_PROBLEM_PATTERN.matcher(s);
      if (matcher.matches()) {
        s = s.toUpperCase(Locale.ROOT);
        var domain = s.substring(0, 3);
        files.add(Path.of(Etc.tptp(), "Problems", domain, s + ".p").toString());
        continue;
      }

      // list file
      if (s.endsWith(".lst")) {
        files.addAll(Files.readAllLines(Path.of(s), StandardCharsets.UTF_8));
        continue;
      }

      // this is a file to be processed
      files.add(s);
    }
    if (shuffle) Collections.shuffle(files, random);
    if (maxAttempted >= 0 && files.size() > maxAttempted) files = files.subList(0, maxAttempted);

    var solved = 0;
    var start = System.currentTimeMillis();
    for (var file : files) {
      var status = status(file);
      System.out.printf("%s\t%s\t", file, status);
      var start1 = System.currentTimeMillis();
      try {
        var sat = Prover.solve(file, steps);
        System.out.printf("%s\t%.3f\n", sat ? "sat" : "uns", time(start1));
        if (status != null)
          switch (status) {
            case "UNSAT", "ContradictoryAxioms", "Unsatisfiable", "Theorem" -> {
              if (sat) throw new IllegalStateException(status);
            }
            case "SAT", "Satisfiable", "CounterSatisfiable" -> {
              if (!sat) throw new IllegalStateException(status);
            }
            default -> throw new IllegalStateException(status);
          }
        solved++;
      } catch (Inappropriate e) {
        System.out.println("iap");
      } catch (Fail e) {
        System.out.printf("-\t%.3f\n", time(start1));
      }
    }
    System.out.println(solved);
    System.out.printf("%.3f\n", time(start));
    Stats.print();
  }
}
