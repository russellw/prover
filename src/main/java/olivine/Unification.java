package olivine;

public final class Unification {
  private Unification() {}

  public static FMap unifyVar(FMap map, Var a, Term b) {
    var a1 = map.get(a);
    if (a1 != null) return unify(map, a1, b);

    var b1 = map.get(b);
    if (b1 != null) return unify(map, a, b1);

    if (b.contains(map, a)) return null;
    return map.add(a, b);
  }

  public static FMap unify(FMap map, Term a, Term b) {
    assert map != null;
    if (a.equals(b)) return map;
    if (!a.type().equals(b.type())) return null;
    if (a instanceof Var a1) return unifyVar(map, a1, b);
    if (b instanceof Var b1) return unifyVar(map, b1, a);
    if (a.tag() != b.tag()) return null;
    var n = a.size();
    if (n == 0) return null;
    if (n != b.size()) return null;
    for (var i = 0; i < n; i++) {
      map = unify(map, a.get(i), b.get(i));
      if (map == null) break;
    }
    return map;
  }
}
