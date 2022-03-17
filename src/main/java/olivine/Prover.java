package olivine;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

final class Prover {
  private enum Language {
    DIMACS,
    TPTP,
  }

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

  private static String version() throws IOException {
    var properties = new Properties();
    var stream =
        Prover.class
            .getClassLoader()
            .getResourceAsStream("META-INF/maven/olivine/olivine/pom.properties");
    if (stream == null) return null;
    properties.load(stream);
    return properties.getProperty("version");
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
      var opt = arg;
      while (opt.charAt(0) == '-') opt = opt.substring(1);
      switch (opt) {
        case "?", "h", "help" -> {
          help();
          System.exit(0);
        }
        case "V", "version" -> {
          System.out.printf(
              "Olivine %s, %s\n",
              Objects.toString(version(), "[unknown version, not running from jar]"),
              System.getProperty("java.class.path"));
          System.out.printf(
              "%s, %s, %s\n",
              System.getProperty("java.vm.name"),
              System.getProperty("java.vm.version"),
              System.getProperty("java.home"));
          System.out.printf(
              "%s, %s, %s\n",
              System.getProperty("os.name"),
              System.getProperty("os.version"),
              System.getProperty("os.arch"));
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
  }
}
