package olivine;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Record {
  public final String file;
  public String answer;
  public double time;
  public final Map<String, Long> map = new LinkedHashMap<>();

  public static Record current;
  public static final Record total = new Record("total");

  private Record(String file) {
    this.file = file;
  }

  public static void init(String file) {
    current = new Record(file);
  }

  public static void inc(String key) {
    inc(key, 1);
  }

  public long get(String key) {
    return map.getOrDefault(key, 0L);
  }

  public static void inc(String key, long n) {
    current.inc1(key, n);
    total.inc1(key, n);
  }

  private void inc1(String key, long n) {
    map.put(key, get(key) + n);
  }
}
