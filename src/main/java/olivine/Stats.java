package olivine;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Stats {
  private Stats() {}

  private static final Map<String, Long> stats = new LinkedHashMap<>();

  public static void inc(String s) {
    inc(s, 1);
  }

  public static void inc(String s, long n) {
    stats.put(s, stats.getOrDefault(s, 0L) + n);
  }

  public static void print() {
    if (stats.isEmpty()) return;
    var df = new DecimalFormat("#,###");
    for (var kv : stats.entrySet())
      System.out.printf("%20s  %s\n", df.format(kv.getValue()), kv.getKey());
    System.out.println();
  }
}
