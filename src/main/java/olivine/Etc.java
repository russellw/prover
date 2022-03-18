package olivine;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public final class Etc {
  private Etc() {}

  public static boolean isDigit(int c) {
    return '0' <= c && c <= '9';
  }

  public static boolean isUpper(int c) {
    return 'A' <= c && c <= 'Z';
  }

  public static boolean isAlpha(int c) {
    return isLower(c) || isUpper(c);
  }

  public static boolean isIdPart(int c) {
    return isAlnum(c) || c == '_';
  }

  public static boolean isAlnum(int c) {
    return isAlpha(c) || isDigit(c);
  }

  public static boolean isLower(int c) {
    return 'a' <= c && c <= 'z';
  }

  public static String extension(String file) {
    var i = file.lastIndexOf('.');
    if (i < 0) return "";
    return file.substring(i + 1);
  }

  public static <T> List<List<T>> cartesianProduct(List<List<T>> qs) {
    var js = new int[qs.size()];
    var rs = new ArrayList<List<T>>();
    cartesianProduct(qs, 0, js, rs);
    return rs;
  }

  private static <T> void cartesianProduct(List<List<T>> qs, int i, int[] js, List<List<T>> rs) {
    if (i == js.length) {
      var ys = new ArrayList<T>();
      for (i = 0; i < js.length; i++) ys.add(qs.get(i).get(js[i]));
      rs.add(ys);
      return;
    }
    for (js[i] = 0; js[i] < qs.get(i).size(); js[i]++) cartesianProduct(qs, i + 1, js, rs);
  }

  public static void debug(Object a) {
    System.out.print(Thread.currentThread().getStackTrace()[2] + ": ");
    System.out.println(a);
  }

  public static BigInteger divideEuclidean(BigInteger a, BigInteger b) {
    return a.subtract(remainderEuclidean(a, b)).divide(b);
  }

  public static BigInteger divideFloor(BigInteger a, BigInteger b) {
    var r = a.divideAndRemainder(b);
    if (a.signum() < 0 != b.signum() < 0 && r[1].signum() != 0)
      r[0] = r[0].subtract(BigInteger.ONE);
    return r[0];
  }

  public static BigInteger remainderEuclidean(BigInteger a, BigInteger b) {
    var r = a.remainder(b);
    if (r.signum() < 0) r = r.add(b.abs());
    return r;
  }

  public static BigInteger remainderFloor(BigInteger a, BigInteger b) {
    return a.subtract(divideFloor(a, b).multiply(b));
  }

  public static String quote(char q, String s) {
    var sb = new StringBuilder();
    sb.append(q);
    for (var i = 0; i < s.length(); i++) {
      var c = s.charAt(i);
      if (c == q || c == '\\') sb.append('\\');
      sb.append(c);
    }
    sb.append(q);
    return sb.toString();
  }

  public static String version() throws IOException {
    var properties = new Properties();
    var stream =
        Prover.class
            .getClassLoader()
            .getResourceAsStream("META-INF/maven/olivine/olivine/pom.properties");
    if (stream == null) return null;
    properties.load(stream);
    return properties.getProperty("version");
  }

  public static void printVersion() throws IOException {
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
  }
}
