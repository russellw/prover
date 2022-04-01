package olivine;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class WeakTrie<T> {
  private Object root;

  public void add(int[] key, T value) {
    root = add(key, value, 0, root);
  }

  private Object add(int[] key, T value, int i, Object node) {
    if (i == key.length) {
      @SuppressWarnings("unchecked")
      var set = (Map<T, Boolean>) node;
      if (set == null) set = new WeakHashMap<>();
      set.put(value, true);
      return set;
    }
    var k = key[i];
    var v = realloc((Object[]) node, k + 1);
    v[k] = add(key, value, i + 1, v[k]);
    return v;
  }

  public T findLessEqual(int[] key, Predicate<T> f) {
    return findLessEqual(key, f, 0, root);
  }

  private T findLessEqual(int[] key, Predicate<T> f, int i, Object node) {
    if (i == key.length) {
      @SuppressWarnings("unchecked")
      var set = (Map<T, Boolean>) node;
      if (set == null) return null;
      for (var value : set.keySet()) if (f.test(value)) return value;
      return null;
    }
    var v = (Object[]) node;
    if (v == null) return null;
    for (var k = 0; k <= key[i] && k < v.length; k++) {
      var value = findLessEqual(key, f, i + 1, v[k]);
      if (value != null) return value;
    }
    return null;
  }

  public void forGreaterEqual(int[] key, Consumer<T> f) {
    forGreaterEqual(key, f, 0, root);
  }

  private void forGreaterEqual(int[] key, Consumer<T> f, int i, Object node) {
    if (i == key.length) {
      @SuppressWarnings("unchecked")
      var set = (Map<T, Boolean>) node;
      if (set == null) return;
      for (var value : set.keySet()) f.accept(value);
      return;
    }
    var v = (Object[]) node;
    if (v == null) return;
    for (var k = key[i]; k < v.length; k++) forGreaterEqual(key, f, i + 1, v[k]);
  }

  private static Object[] realloc(Object[] v, int n) {
    if (v == null) return new Object[n];
    if (n <= v.length) return v;
    return Arrays.copyOf(v, Math.max(n, v.length * 2));
  }
}
