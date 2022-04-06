package olivine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

final class Prover {
  private static String file;
  private static long steps = Long.MAX_VALUE;

  private Prover() {}

  private static void help() {
    System.out.println("-h          Show help");
    System.out.println("-V          Show version");
    System.out.println("-t seconds  Time limit");
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
          case "t", "T", "cpu-limit" -> {
            var seconds = Double.parseDouble(option.getArg());
            new Timer()
                .schedule(
                    new TimerTask() {
                      @Override
                      public void run() {
                        System.exit(1);
                      }
                    },
                    (long) (seconds * 1000));
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
    try {
      args(args);
      if (file == null) {
        System.err.println("Input not specified");
        System.exit(1);
      }

      System.out.println(solve(file, 10000000, steps) ? "sat" : "unsat");
    } catch (Fail ignored) {
      System.exit(0);
    } catch (Throwable e) {
      // this needs to be done explicitly in case there is a timer thread still running,
      // which would cause the program to hang until timeout without the explicit System.exit
      e.printStackTrace();
      System.exit(1);
    }
  }
}
