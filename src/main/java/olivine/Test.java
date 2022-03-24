package olivine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

final class Test {
  private static final Pattern STATUS_PATTERN = Pattern.compile("%\\s*Status\\s*:\\s*(\\w+)");

  private static List<String> files = new ArrayList<>();
  private static boolean shuffle;
  private static int randomSeed = -1;
  private static int maxFiles = -1;

  private Test() {}

  private static void addFile(String s) {
    if (s.contains("^")) return;
    if (!s.endsWith(".p")) return;
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
          case "n" -> maxFiles = Integer.parseInt(option.getArg());
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

  private static String header(String file) throws IOException {
    String expected = null;
    try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String s;
      while ((s = reader.readLine()) != null) {
        if (!s.isBlank() && s.charAt(0) != '%') break;
        System.out.println(s);
        if (expected == null) {
          var matcher = STATUS_PATTERN.matcher(s);
          if (matcher.matches()) expected = matcher.group(1);
        }
      }
    }
    return expected;
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
      var expected = header(file);
      var start1 = System.currentTimeMillis();

      var cnf = new CNF();
      try (var stream = new BufferedInputStream(new FileInputStream(file))) {
        // read
        TptpParser.parse(file, stream, cnf);

        // solve
        var answer = new Superposition(cnf.clauses, 10000000, 1000).answer;

        // output
        System.out.print("% SZS status ");
        var answerString = answer.szs.string(cnf.conjecture);
        System.out.println(answerString);
        if (answer.proof != null) new TptpPrinter().proof(answer.proof);

        // check
        if (answer.szs.success()) {
          if (expected != null) {
            if (answer.szs == SZS.Unsatisfiable && expected.equals("ContradictoryAxioms"))
              expected = answerString;
            if (!answerString.equals(expected)) throw new IllegalStateException(answerString);
          }
          solved++;
        }
      } catch (InappropriateException e) {
        System.out.println("% SZS status Inappropriate");
      }

      System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - start1) / 1000.0);
      System.out.println();
    }
    Stats.print();
    System.out.printf("Solved %d/%d\n", solved, files.size());
    System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - start) / 1000.0);
  }
}
