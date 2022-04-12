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
          if (b instanceof Var b1) map.put(b1, map.getOrDefault(b1, 0) + 1);
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

  // TODO: shortcut comparison of identical terms?
  // TODO: pacman lemma?
  public PartialOrder compare(Term a, Term b) {
    // variables
    var avars = vars(a);
    var bvars = vars(b);
    var maybeLess = true;
    var maybeGreater = true;
    for (var kv : avars.entrySet())
      if (kv.getValue() > bvars.getOrDefault(kv.getKey(), 0)) {
        maybeLess = false;
        break;
      }
    for (var kv : bvars.entrySet())
      if (kv.getValue() > avars.getOrDefault(kv.getKey(), 0)) {
        maybeGreater = false;
        break;
      }
    if (!maybeLess && !maybeGreater)
      return a.equals(b) ? PartialOrder.EQUALS : PartialOrder.UNORDERED;

    // total weight
    var atotalWeight = totalWeight(a);
    var btotalWeight = totalWeight(b);
    if (atotalWeight < btotalWeight) return maybeLess ? PartialOrder.LESS : PartialOrder.UNORDERED;
    if (atotalWeight > btotalWeight)
      return maybeGreater ? PartialOrder.GREATER : PartialOrder.UNORDERED;

    // different tags or functions mean different symbols
    var asymbolWeight = symbolWeight(a);
    var bsymbolWeight = symbolWeight(b);
    if (asymbolWeight < bsymbolWeight)
      return maybeLess ? PartialOrder.LESS : PartialOrder.UNORDERED;
    if (asymbolWeight > bsymbolWeight)
      return maybeGreater ? PartialOrder.GREATER : PartialOrder.UNORDERED;
    assert a.tag() == b.tag();
    assert a.size() == b.size();

    // in some cases, the same tags can still mean different symbols, e.g. constants
    // with different values, or casts to different types
    switch (a.tag()) {
      case CAST -> {
        var c = a.type().compareTo(b.type());
        if (c != 0) return PartialOrder.of(c);
      }
      case INTEGER -> {
        return PartialOrder.of(a.integerValue().compareTo(b.integerValue()));
      }
      case RATIONAL -> {
        return PartialOrder.of(a.rationalValue().compareTo(b.rationalValue()));
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
        assert !(a != b && a.toString().equals(b.toString()));
        return PartialOrder.of(a.toString().compareTo(b.toString()));
      }
    }

    // recur
    var n = a.size();
    var i = 0;
    while (i < n && a.get(i).equals(b.get(i))) i++;
    if (i == n) return PartialOrder.EQUALS;
    return compare(a.get(i), b.get(i));
  }

  public PartialOrder compare(boolean apol, Equation a, boolean bpol, Equation b) {
    if (apol == bpol) return EquationComparison.compare(this, a, b);
    return bpol
        ? EquationComparison.compareNP(this, a, b)
        : EquationComparison.compareNP(this, b, a).flip();
  }
}
