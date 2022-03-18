package olivine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class Test {
  private static final List<String> files = new ArrayList<>();

  private Test() {}

  private static void args(String[] args) throws IOException {
    for (var i = 0; i < args.length; i++) {
      var arg = args[i];
      if (arg.charAt(0) != '-') {
        var path = Path.of(arg);
        if (Files.isDirectory(path)) {
          args(
              Files.walk(path)
                  .filter(p -> !Files.isDirectory(p))
                  .map(Path::toString)
                  .toArray(String[]::new));
          continue;
        }
        if (arg.contains("^")) continue;
        switch (Etc.extension(arg)) {
          case "lst" -> args(
              Files.readAllLines(path, StandardCharsets.UTF_8).toArray(new String[0]));
          case "rm" -> {}
          default -> files.add(arg);
        }
        continue;
      }
      var opt = arg;
      while (opt.startsWith("-")) opt = opt.substring(1);
      switch (opt) {
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

  private static void printHeader(String file) throws IOException {
    try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String s;
      while ((s = reader.readLine()) != null) {
        if (!s.isBlank() && s.charAt(0) != '%') break;
        System.out.println(s);
      }
    }
  }

  public static void main(String[] args) throws IOException {
    args(args);
    var start = System.currentTimeMillis();
    for (var file : files) {
      printHeader(file);
      var cnf = new CNF();
      var st = System.currentTimeMillis();
      try (var stream = new BufferedInputStream(new FileInputStream(file))) {
        TptpParser.parse(file, stream, cnf);
      }
      System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - st) / 1000.0);
      System.out.println();
    }
    System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - start) / 1000.0);
  }
}
