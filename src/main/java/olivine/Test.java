package olivine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class Test {
  private static List<String> files = new ArrayList<>();
  private static boolean shuffle;
  private static int maxFiles = -1;

  private Test() {}

  private static void addFile(String s) {
    if (s.contains("^")) return;
    if (s.endsWith(".rm")) return;
    files.add(s);
  }

  private static void args(String[] args) throws IOException {
    for (var i = 0; i < args.length; i++) {
      var arg = args[i];
      if (arg.charAt(0) != '-') {
        var path = Path.of(arg);
        if (Files.isDirectory(path)) {
          for (var s :
              Files.walk(path)
                  .filter(p -> !Files.isDirectory(p))
                  .map(Path::toString)
                  .toArray(String[]::new)) addFile(s);
          continue;
        }
        if (arg.endsWith(".lst")) {
          try (var reader = new BufferedReader(new FileReader(arg, StandardCharsets.UTF_8))) {
            String s;
            while ((s = reader.readLine()) != null) addFile(s);
          }
          continue;
        }
        addFile(arg);
        continue;
      }
      var option = arg;
      while (option.startsWith("-")) option = option.substring(1);
      switch (option) {
        case "V", "version" -> {
          Etc.printVersion();
          System.exit(0);
        }
        case "s" -> shuffle = true;
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
    if (shuffle) Collections.shuffle(files, new Random(0));
    if (maxFiles >= 0 && files.size() > maxFiles) files = files.subList(0, maxFiles);

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
