package olivine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

final class Prover {
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
        new Option('t', "cpu-limit", "seconds", "time limit") {
          @Override
          public void accept(String arg) {
            var seconds = Double.parseDouble(arg);
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
        },
      };

  private Prover() {}

  public static boolean solve(String file, long steps) throws IOException {
    // TODO: accept stdin
    try (var stream = new BufferedInputStream(new FileInputStream(file))) {
      var cnf = new CNF();
      TptpParser.parse(file, stream, cnf);
      return Superposition.sat(cnf.clauses, steps);
    }
  }

  public static void main(String[] args) throws IOException {
    try {
      Option.parse(OPTIONS, args);
      if (Option.positionalArgs.isEmpty()) {
        System.err.println("Input not specified");
        System.exit(1);
      }
      if (Option.positionalArgs.size() > 1) {
        System.err.printf("%s: file already specified\n", Option.positionalArgs.get(1));
        System.exit(1);
      }
      var file = Option.positionalArgs.get(0);

      System.out.println(solve(file, Long.MAX_VALUE) ? "sat" : "unsat");
    } catch (Fail ignored) {
      System.exit(0);
    } catch (Throwable e) {
      // this needs to be done explicitly in case there is a timer thread still running,
      // which would cause the program to hang until timeout without the explicit System.exit
      e.printStackTrace();
      System.exit(1);
    }
    System.exit(0);
  }
}
