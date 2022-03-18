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

  private static void args(String[] args) throws IOException {
    for (var i = 0; i < args.length; i++) {
      var arg = args[i];
      if (arg.charAt(0) != '-') {
        setFile(arg);
        continue;
      }
      if ("-".equals(arg)) {
        setFile("stdin");
        continue;
      }
      var option = arg;
      while (option.startsWith("-")) option = option.substring(1);
      switch (option) {
        case "?", "h", "help" -> {
          help();
          System.exit(0);
        }
        case "V", "version" -> {
          Etc.printVersion();
          System.exit(0);
        }
        default -> {
          System.err.printf("%s: unknown option\n", arg);
          System.exit(1);
        }
      }
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
