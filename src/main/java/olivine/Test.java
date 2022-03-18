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
  private static int randomSeed = -1;
  private static int maxFiles = -1;

  private Test() {}

  private static void addFile(String s) {
    if (s.contains("^")) return;
    if (s.endsWith(".rm")) return;
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

  private static void header(String file) throws IOException {
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
    if (shuffle) {
      var random = randomSeed == -1 ? new Random() : new Random(randomSeed);
      Collections.shuffle(files, random);
    }
    if (maxFiles >= 0 && files.size() > maxFiles) files = files.subList(0, maxFiles);

    var start = System.currentTimeMillis();
    for (var file : files) {
      header(file);
      var cnf = new CNF();
      var st = System.currentTimeMillis();
      try (var stream = new BufferedInputStream(new FileInputStream(file))) {
        TptpParser.parse(file, stream, cnf);
      } catch (InappropriateException e) {
        System.out.println("% SZS status Inappropriate");
      }
      System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - st) / 1000.0);
      System.out.println();
    }
    System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - start) / 1000.0);
  }
}
