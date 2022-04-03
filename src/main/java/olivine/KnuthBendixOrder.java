package olivine;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class KnuthBendixOrder {
  private final Map<Term, Integer> weights = new HashMap<>();

  public KnuthBendixOrder(List<Clause> clauses) {
    var globals = new LinkedHashSet<Term>();
    for (var c : clauses) for (var a : c.literals) a.walkGlobals(globals::add);
    var i = 2 + Tag.values().length;
    for (var a : globals) weights.put(a, i++);
  }

  private static Map<Var, Integer> vars(Term a) {
    var map = new HashMap<Var, Integer>();
    a.walkLeaves(
        b -> {
          if (b instanceof Var b1) map.put(b1, map.getOrDefault(b1, 0));
        });
    return map;
  }

  private int symbolWeight(Term a) {
    var tag = a.tag();
    return switch (tag) {
      case GLOBAL_VAR -> weights.get(a);
      case TRUE, VAR -> 1;
      case CALL -> weights.get(a.get(0));
      default -> tag.ordinal();
    };
  }

  private long totalWeight(Term a) {
    long n = symbolWeight(a);
    for (var b : a) n += totalWeight(b);
    return n;
  }

  public boolean greater(Term a, Term b) {
    // variables
    var avars = vars(a);
    for (var kv : vars(b).entrySet()) if (kv.getValue() > avars.get(kv.getKey())) return false;

    // total weight
    var atotal = totalWeight(a);
    var btotal = totalWeight(b);
    if (atotal > btotal) return true;
    if (atotal < btotal) return false;

    // different symbols
    var asymbol = symbolWeight(a);
    var bsymbol = symbolWeight(b);
    if (asymbol > bsymbol) return true;
    if (asymbol < bsymbol) return false;
    assert a.tag() == b.tag();

    // recur
    var n = a.size();
    assert n == b.size();
    return true;
  }
}
