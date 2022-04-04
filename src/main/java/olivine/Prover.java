package olivine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

final class Prover {
  private static String file;
  private static long steps = Long.MAX_VALUE;

  private Prover() {}

  private static void help() {
    System.out.println("-h  Show help");
    System.out.println("-V  Show version");
  }

  private static void setFile(String s) {
    if (file != null) {
      System.err.printf("%s: file already specified\n", s);
      System.exit(1);
    }
    file = s;
  }

  private static void args(String[] v) throws IOException {
    for (var s : v) {
      if (s.charAt(0) == '-') {
        if (s.equals("-")) {
          setFile("stdin");
          continue;
        }
        var option = new Option(s);
        switch (option.option) {
          case "?", "h", "help" -> {
            help();
            System.exit(0);
          }
          case "V", "version" -> {
            Etc.printVersion();
            System.exit(0);
          }
          default -> {
            System.err.printf("%s: unknown option\n", s);
            System.exit(1);
          }
        }
        continue;
      }
      setFile(s);
    }
  }

  public static boolean solve(String file, int clauseLimit, long steps) throws IOException {
    try (var stream = new BufferedInputStream(new FileInputStream(file))) {
      var cnf = new CNF();
      TptpParser.parse(file, stream, cnf);
      return Superposition.sat(cnf.clauses, clauseLimit, steps);
    }
  }

  public static void main(String[] args) throws IOException {
    args(args);
    if (file == null) {
      System.err.println("Input not specified");
      System.exit(1);
    }

    try {
      System.out.println(solve(file, 10000000, steps) ? "sat" : "unsat");
    } catch (Fail ignored) {
    }
  }
}
