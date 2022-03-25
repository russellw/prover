package olivine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

final class Test {
  private static final Pattern STATUS_PATTERN = Pattern.compile("%\\s*Status\\s*:\\s*(\\w+)");

  private static List<String> files = new ArrayList<>();
  private static boolean shuffle;
  private static int randomSeed = -1;
  private static int maxFiles = -1;
  private static long steps = 100000;

  private Test() {}

  private static void addFile(String s) {
    if (s.contains("^")) return;
    if (!s.endsWith(".p")) return;
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
          case "b" -> maxFiles = Integer.parseInt(option.getArg());
          case "n" -> steps = Long.parseLong(option.getArg());
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

  private static String header(String file) throws IOException {
    String expected = null;
    try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String s;
      while ((s = reader.readLine()) != null) {
        if (!s.isBlank() && s.charAt(0) != '%') break;
        System.out.println(s);
        if (expected == null) {
          var matcher = STATUS_PATTERN.matcher(s);
          if (matcher.matches()) expected = matcher.group(1);
        }
      }
    }
    return expected;
  }

  private static void printBlock(Map<String, Long> map) {
    if (map.isEmpty()) return;
    System.out.println();
    var df = new DecimalFormat("#,###");
    for (var kv : map.entrySet())
      System.out.printf("%20s  %s\n", df.format(kv.getValue()), kv.getKey());
  }

  private static void writeCSV(List<Record> records, String file) throws FileNotFoundException {
    var keys = Record.total.keySet();
    var totalTime = 0.0;
    var total = new LinkedHashMap<String, Long>();
    try (var writer = new PrintWriter(file + ".csv")) {
      writer.print("file\tszs\ttime");
      for (var key : keys) {
        writer.print('\t');
        writer.print(key);
      }
      writer.println();

      for (var record : records) {
        writer.printf("%s\t%s\t%.3f", record.file, record.answer, record.time);
        totalTime += record.time;
        for (var key : keys) {
          writer.print('\t');
          var n = record.get(key);
          writer.print(n);
          total.put(key, total.getOrDefault(key, 0L) + n);
        }
        writer.println();
      }

      writer.printf("total\t-\t%.3f", totalTime);
      for (var key : keys) {
        writer.print('\t');
        writer.print(total.getOrDefault(key, 0L));
      }
      writer.println();
    }
  }

  public static void main(String[] args) throws IOException {
    // command line
    args(args);
    if (shuffle) {
      var random = randomSeed == -1 ? new Random() : new Random(randomSeed);
      Collections.shuffle(files, random);
    }
    if (maxFiles >= 0 && files.size() > maxFiles) files = files.subList(0, maxFiles);

    // attempt problems
    var solved = new ArrayList<Record>();
    var unsolved = new ArrayList<Record>();

    var start = System.currentTimeMillis();
    for (var file : files) {
      var expected = header(file);
      Record.init(file);
      var cnf = new CNF();

      var start1 = System.currentTimeMillis();
      try (var stream = new BufferedInputStream(new FileInputStream(file))) {
        // read
        TptpParser.parse(file, stream, cnf);

        // solve
        var answer = new Superposition(cnf.clauses, 10000000, steps).answer;

        // output
        System.out.print("% SZS status ");
        Record.current.answer = answer.szs.string(cnf.conjecture);
        System.out.println(Record.current.answer);
        if (answer.proof != null) new TptpPrinter().proof(answer.proof);

        Record.current.time = (System.currentTimeMillis() - start1) / 1000.0;
        System.out.printf("%.3f seconds\n", Record.current.time);
        printBlock(Record.current.map);

        // check and record
        if (answer.szs.success()) {
          if (expected != null) {
            if (answer.szs == SZS.Unsatisfiable && expected.equals("ContradictoryAxioms"))
              expected = Record.current.answer;
            if (!Record.current.answer.equals(expected))
              throw new IllegalStateException(Record.current.answer);
          }
          solved.add(Record.current);
        } else unsolved.add(Record.current);
      } catch (InappropriateException e) {
        System.out.println("% SZS status Inappropriate");
      }
      System.out.println();
    }

    // print summary
    System.out.printf(
        "Solved %d/%d (%.1f%%)\n",
        solved.size(), files.size(), solved.size() * 100.0 / files.size());
    System.out.printf("%.3f seconds\n", (System.currentTimeMillis() - start) / 1000.0);
    printBlock(Record.total);

    // write summary files
    writeCSV(solved, "solved");
    writeCSV(unsolved, "unsolved");
  }
}
