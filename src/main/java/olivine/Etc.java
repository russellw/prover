package olivine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

  public static void trace(Object a) {
    System.out.print(Thread.currentThread().getStackTrace()[2] + ": ");
    System.out.println(a);
  }

  public static BigInteger divideEuclidean(BigInteger a, BigInteger b) {
    var q = a.divide(b);
    if (a.signum() < 0 && !q.multiply(b).equals(a)) q = q.subtract(BigInteger.valueOf(b.signum()));
    return q;
  }

  public static BigInteger divideFloor(BigInteger a, BigInteger b) {
    var qr = a.divideAndRemainder(b);
    var q = qr[0];
    if (a.signum() < 0 != b.signum() < 0 && qr[1].signum() != 0) q = q.subtract(BigInteger.ONE);
    return q;
  }

  public static BigInteger remainderEuclidean(BigInteger a, BigInteger b) {
    // The BigInteger 'mod' function cannot be used, as it rejects negative inputs
    var r = a.remainder(b);
    if (r.signum() < 0) r = r.add(b.abs());
    return r;
  }

  public static BigInteger remainderFloor(BigInteger a, BigInteger b) {
    return a.subtract(divideFloor(a, b).multiply(b));
  }

  public static InputStream stringInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
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

  public static String tptp() {
    var s = System.getenv("TPTP");
    if (s == null) throw new IllegalStateException("TPTP environment variable not set");
    return s;
  }
}
