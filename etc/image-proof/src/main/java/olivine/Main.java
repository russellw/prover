package olivine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class Main {
  private static String file;
  private static long steps = Long.MAX_VALUE;

  private Main() {}

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
    var xs = Files.readAllLines(Path.of(file), StandardCharsets.UTF_8);
    var i = 0;
    while (!xs.get(i).contains("SZS output start CNFRefutation")) i++;
    i++;
    while (!xs.get(i).contains("SZS output end CNFRefutation")) {
      var s = xs.get(i++);
      System.out.println(s);
      TptpParser.parse(null, new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    }
    for (var c : TptpParser.clauses) new TptpPrinter().println(c);
    new Image(file, TptpParser.clauses);
  }
}
