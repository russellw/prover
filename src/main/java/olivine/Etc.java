package olivine;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class Etc {
  private Etc() {}

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
}
