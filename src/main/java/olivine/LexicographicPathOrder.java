package olivine;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class LexicographicPathOrder {
  private final Map<Term, Integer> weights = new HashMap<>();

  public LexicographicPathOrder(List<Clause> clauses) {
    var globals = new LinkedHashSet<Term>();
    for (var c : clauses) for (var a : c.literals) a.walkGlobals(globals::add);
    var i = Tag.values().length;
    for (var a : globals) weights.put(a, i++);
  }

  private int weight(Term a) {
    var tag = a.tag();
    return switch (tag) {
      case GLOBAL_VAR -> weights.get(a);
      case TRUE -> -1;
      case CALL -> weights.get(a.get(0));
      default -> tag.ordinal();
    };
  }

  public boolean greater(Term a, Term b) {
    // Fast equality test
    if (a == b) return false;

    // Variables are unordered unless contained in other term
    if (a instanceof Var) return false;
    if (b instanceof Var b1) return a.contains(b1);

    // Sufficient condition: exists ai >= b
    var atag = a.tag();
    var a0 = atag == Tag.CALL ? 1 : 0;
    var an = a.size();
    for (var i = a0; i < an; i++) if (greaterEq(a.get(i), b)) return true;

    // Necessary condition: a > all bi
    var btag = b.tag();
    var b0 = btag == Tag.CALL ? 1 : 0;
    var bn = b.size();
    for (var i = b0; i < bn; i++) if (!greater(a, b.get(i))) return false;

    // Different function symbols
    var wa = weight(a);
    var wb = weight(b);
    if (wa != wb) return wa > wb;

    // Same weights means similar terms
    assert atag == btag;
    assert an == bn;
    assert atag != Tag.CALL || a.get(0).equals(b.get(0));

    // Constants
    switch (atag) {
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
        assert a.toString() != null;
        assert b.toString() != null;
        assert !a.toString().equals(b.toString());
        return a.toString().compareTo(b.toString()) > 0;
      }
    }

    // Lexicographic extension
    for (var i = a0; i < an; i++) {
      var ai = a.get(i);
      var bi = b.get(i);
      if (greater(ai, bi)) return true;
      if (!ai.equals(bi)) return false;
    }
    assert a.equals(b);
    return false;
  }

  private boolean greaterEq(Term a, Term b) {
    return greater(a, b) || a.equals(b);
  }
}
