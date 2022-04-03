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
    var atotalWeight = totalWeight(a);
    var btotalWeight = totalWeight(b);
    if (atotalWeight > btotalWeight) return true;
    if (atotalWeight < btotalWeight) return false;

    // different symbols
    var asymbolWeight = symbolWeight(a);
    var bsymbolWeight = symbolWeight(b);
    if (asymbolWeight > bsymbolWeight) return true;
    if (asymbolWeight < bsymbolWeight) return false;
    assert a.tag() == b.tag();
    assert a.size() == b.size();

    // Constants
    switch (a.tag()) {
      case INTEGER -> {
        return a.integerValue().compareTo(b.integerValue()) > 0;
      }
      case RATIONAL -> {
        return a.rationalValue().compareTo(b.rationalValue()) > 0;
      }
      case DISTINCT_OBJECT -> {
        // here, we rely on distinct objects being ordered by their names, in other words behaving
        // as though they had
        // value semantics. Strictly speaking, this is only guaranteed by the TPTP parser; it is not
        // guaranteed by the
        // DistinctObject class itself, which doesn't enforce unique names, and is happy to allow
        // distinct objects
        // to be compared by reference for efficiency in other contexts. so assert that
        // the precondition holds here, i.e. different objects have different names
        assert !a.toString().equals(b.toString());
        return a.toString().compareTo(b.toString()) > 0;
      }
    }

    // recur
    var n = a.size();
    var i = 0;
    while (i < n && !a.get(i).equals(b.get(i))) i++;
    if (i == n) return false;
    return greater(a.get(i), b.get(i));
  }
}
