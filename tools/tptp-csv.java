import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.*;

final class Main {
  private static final Pattern STATUS_PATTERN = Pattern.compile("%\\s*Status\\s*:\\s*(\\w+)");
  private static final Pattern RATING_PATTERN = Pattern.compile("%\\s*Rating\\s*:\\s*(\\S+).*");
  private static final Pattern PROBLEM_PATTERN = Pattern.compile("%\\s*Problem\\s*:\\s*(\\S.*)");

  private static void header(String file) throws IOException {
    if (file.contains("^")) return;
    if (!file.endsWith(".p")) return;

    var status = "-";
    var rating = "-";
    var problem = "-";
    try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String s;
      while ((s = reader.readLine()) != null) {
        if (!s.isBlank() && s.charAt(0) != '%') break;
        Matcher matcher;

        matcher = STATUS_PATTERN.matcher(s);
        if (matcher.matches()) status = matcher.group(1);

        matcher = RATING_PATTERN.matcher(s);
        if (matcher.matches()) rating = matcher.group(1);

        matcher = PROBLEM_PATTERN.matcher(s);
        if (matcher.matches()) problem = matcher.group(1);
      }
    }
    System.out.printf("%s\t%s\t%s\t%s\n", file, status, rating, problem);
  }

  public static void main(String[] args) throws IOException {
    System.out.println("name\tstatus\trating\tproblem");
    for (var s : args) {
      var path = Path.of(s);
      if (Files.isDirectory(path)) {
        for (var file :
            Files.walk(path)
                .filter(p -> !Files.isDirectory(p))
                .map(Path::toString)
                .toArray(String[]::new)) header(file);
        continue;
      }
      header(s);
    }
  }
}
