package olivine;

import java.io.IOException;

final class Prover {
  private static String file;

  private Prover() {}

  private static void setFile(String s) {
    if (file != null) {
      System.err.printf("%s: file already specified\n", s);
      System.exit(1);
    }
    file = s;
  }

  private static void help() {
    System.out.println("-h  Show help");
    System.out.println("-V  Show version");
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

  public static void main(String[] args) throws IOException {
    args(args);
    if (file == null) {
      System.err.println("Input not specified");
      System.exit(1);
    }
  }
}
