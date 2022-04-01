package olivine;

import java.util.*;
import java.util.function.Consumer;

final class Graph<T> {
  private final Map<T, Set<T>> arcs = new HashMap<>();

  void add(T a, T b) {
    var v = arcs.computeIfAbsent(a, k -> new HashSet<>());
    v.add(b);
  }

  Set<T> nodes() {
    var v = new HashSet<T>();
    for (var kv : arcs.entrySet()) {
      v.add(kv.getKey());
      v.addAll(kv.getValue());
    }
    return v;
  }

  void dfs(T a, Consumer<T> f) {
    dfs(a, f, new HashSet<>());
  }

  private void dfs(T a, Consumer<T> f, Set<T> visited) {
    if (!visited.add(a)) return;
    f.accept(a);
    for (var b : successors(a)) dfs(b, f, visited);
  }

  void dfsWithout(T a, T w, Consumer<T> f) {
    dfsWithout(a, w, f, new HashSet<>());
  }

  private void dfsWithout(T a, T w, Consumer<T> f, Set<T> visited) {
    if (a.equals(w)) return;
    if (!visited.add(a)) return;
    f.accept(a);
    for (var b : successors(a)) dfsWithout(b, w, f, visited);
  }

  Set<T> predecessors(T a) {
    var v = new HashSet<T>();
    for (var kv : arcs.entrySet()) if (kv.getValue().contains(a)) v.add(kv.getKey());
    return v;
  }

  boolean reaches(T a, T b) {
    final boolean[] r = {false};
    dfs(
        a,
        c -> {
          if (c.equals(b)) r[0] = true;
        });
    return r[0];
  }

  T idom(T entry, T b) {
    for (var a : nodes()) if (isIdom(entry, a, b)) return a;
    return null;
  }

  boolean isIdom(T entry, T a, T b) {
    if (!strictlyDominates(entry, a, b)) return false;
    for (var c : strictDominators(entry, b)) if (!dominates(entry, c, a)) return false;
    return true;
  }

  boolean strictlyDominates(T entry, T a, T b) {
    if (a.equals(b)) return false;
    return dominates(entry, a, b);
  }

  Set<T> domFrontier(T entry, T a) {
    var v = new HashSet<T>();
    for (var b : nodes()) {
      if (strictlyDominates(entry, a, b)) continue;
      for (var c : predecessors(b))
        if (dominates(entry, a, c)) {
          v.add(b);
          break;
        }
    }
    return v;
  }

  Set<T> strictDominators(T entry, T b) {
    var v = new HashSet<T>();
    for (var a : nodes()) if (strictlyDominates(entry, a, b)) v.add(a);
    return v;
  }

  boolean dominates(T entry, T a, T b) {
    return !reachesWithout(entry, b, a);
  }

  boolean reachesWithout(T a, T b, T w) {
    final boolean[] r = {false};
    dfsWithout(
        a,
        w,
        c -> {
          if (c.equals(b)) r[0] = true;
        });
    return r[0];
  }

  Set<T> transSuccessors(T a) {
    var v = new HashSet<T>();
    dfs(a, v::add);
    v.remove(a);
    return v;
  }

  Set<T> successors(T a) {
    var v = arcs.get(a);
    if (v == null) v = new HashSet<>();
    return v;
  }
}
