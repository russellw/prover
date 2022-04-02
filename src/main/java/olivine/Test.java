package olivine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

final class Test {
  private static final Pattern TPTP_PATTERN =
      Pattern.compile("[a-zA-Z][a-zA-Z][a-zA-Z]\\d\\d\\d.\\d+(\\.\\d+)?");
  private static final Pattern STATUS_PATTERN = Pattern.compile("%\\s*Status\\s*:\\s*(\\w+)");

  private static List<String> files = new ArrayList<>();
  private static boolean shuffle;
  private static int randomSeed = -1;
  private static int maxFiles = -1;
  private static long steps = 100;

  private Test() {}

  private static void addFile(String s) throws IOException {
    // accept unadorned TPTP
    if (s.equalsIgnoreCase("tptp")) {
      var dir = System.getenv("TPTP");
      if (dir == null) throw new IllegalStateException("TPTP environment variable not set");
      for (var file :
          Files.walk(Path.of(dir))
              .filter(p -> !Files.isDirectory(p))
              .map(Path::toString)
              .toArray(String[]::new)) addFile(file);
      return;
    }

    // accept unadorned TPTP problem names
    var matcher = TPTP_PATTERN.matcher(s);
    if (matcher.matches()) {
      var dir = System.getenv("TPTP");
      if (dir == null) throw new IllegalStateException("TPTP environment variable not set");
      dir = String.format("%s/Problems/%s", dir, s.substring(0, 3));
      s = s.toUpperCase(Locale.ROOT) + ".p";
      files.add(Path.of(dir, s).toString());
      return;
    }

    // skip things that are not TPTP problem files
    if (!s.endsWith(".p")) return;

    // skip higher order problems
    if (s.contains("^")) return;

    // this is a file to be processed
    files.add(s);
  }

  private static void args(String[] v) throws IOException {
    for (var s : v) {
      if (s.charAt(0) == '-') {
        var option = new Option(s);
        switch (option.option) {
          case "V", "version" -> {
            Etc.printVersion();
            System.exit(0);
          }
          case "b" -> maxFiles = Integer.parseInt(option.getArg());
          case "n" -> steps = Long.parseLong(option.getArg());
          case "s" -> {
            shuffle = true;
            if (option.arg != null) randomSeed = Integer.parseInt(option.arg);
          }
          default -> {
            System.err.printf("%s: unknown option\n", s);
            System.exit(1);
          }
        }
        continue;
      }
      var path = Path.of(s);
      if (Files.isDirectory(path)) {
        for (var file :
            Files.walk(path)
                .filter(p -> !Files.isDirectory(p))
                .map(Path::toString)
                .toArray(String[]::new)) addFile(file);
        continue;
      }
      if (s.endsWith(".lst")) {
        try (var reader = new BufferedReader(new FileReader(s, StandardCharsets.UTF_8))) {
          String file;
          while ((file = reader.readLine()) != null) addFile(file);
        }
        continue;
      }
      addFile(s);
    }
  }

  private static String status(String file) throws IOException {
    try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String s;
      while ((s = reader.readLine()) != null) {
        if (!s.isBlank() && s.charAt(0) != '%') break;
        var matcher = STATUS_PATTERN.matcher(s);
        if (matcher.matches()) return matcher.group(1);
      }
    }
    return null;
  }

  private static double time(long start) {
    return (System.currentTimeMillis() - start) * 0.001;
  }

  public static void main(String[] args) throws IOException {
    args(args);
    if (shuffle) {
      var random = randomSeed == -1 ? new Random() : new Random(randomSeed);
      Collections.shuffle(files, random);
    }
    if (maxFiles >= 0 && files.size() > maxFiles) files = files.subList(0, maxFiles);

    var solved = 0;
    var start = System.currentTimeMillis();
    for (var file : files) {
      var status = status(file);
      System.out.printf("%s\t%s\t", file, status);
      var start1 = System.currentTimeMillis();
      try (var stream = new BufferedInputStream(new FileInputStream(file))) {
        // read
        var cnf = new CNF();
        TptpParser.parse(file, stream, cnf);

        // solve
        var sat = Superposition.sat(cnf.clauses, 10000000, steps);

        // output
        System.out.printf("%s\t%.3f\n", sat ? "sat" : "uns", time(start1));

        // check
        if (status != null)
          switch (status) {
            case "ContradictoryAxioms", "Unsatisfiable", "Theorem" -> {
              if (sat) throw new IllegalStateException(status);
            }
            case "Satisfiable", "CounterSatisfiable" -> {
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
