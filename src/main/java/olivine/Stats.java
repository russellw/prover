package olivine;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Stats {
  private static final Map<String, Long> map = new LinkedHashMap<>();

  private Stats() {}

  public static void inc(String key) {
    inc(key, 1);
  }

  public static void inc(String key, long n) {
    map.put(key, map.getOrDefault(key, 0L) + n);
  }

  public static void inc(String key, long i, long n) {
    inc(key + '/' + i, n);
  }

  public static void print() {
    if (map.isEmpty()) return;
    System.out.println();
    var df = new DecimalFormat("#,###");
    for (var kv : map.entrySet())
      System.out.printf("%20s  %s\n", df.format(kv.getValue()), kv.getKey());
  }
}
