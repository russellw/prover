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

  public static void inc(String s) {
    inc(s, 1);
  }

  public static void inc(String s, long n) {
    current.inc1(s, n);
    total.inc1(s, n);
  }

  private void inc1(String s, long n) {
    map.put(s, map.getOrDefault(s, 0L) + n);
  }
}
