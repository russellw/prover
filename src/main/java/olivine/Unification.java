package olivine;

public final class Unification {
  private Unification() {}

  // TODO: rearrange?
  private static boolean occurs(FMap map, Term a, Term b) {
    assert a instanceof Var;
    if (b instanceof Var) {
      if (a == b) return true;
      var b1 = map.get(b);
      if (b1 != null) return occurs(map, a, b1);
      return false;
    }
    for (var bi : b) if (occurs(map, a, bi)) return true;
    return false;
  }

  public static FMap unifyVar(FMap map, Term a, Term b) {
    assert a instanceof Var;

    var a1 = map.get(a);
    if (a1 != null) return unify(map, a1, b);

    var b1 = map.get(b);
    if (b1 != null) return unify(map, a, b1);

    if (occurs(map, a, b)) return null;
    return map.add(a, b);
  }

  public static FMap unify(FMap map, Term a, Term b) {
    assert map != null;
    if (a.equals(b)) return map;
    if (!a.type().equals(b.type())) return null;
    if (a instanceof Var) return unifyVar(map, a, b);
    if (b instanceof Var) return unifyVar(map, b, a);
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
