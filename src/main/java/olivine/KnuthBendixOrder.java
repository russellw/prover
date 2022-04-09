package olivine;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class KnuthBendixOrder {
  // comparison is usually encoded as a three-way integer. Here, the presence of variables
  // (which substitution could expand into any term) make some pairs of terms unordered
  // (because the result could change depending on what the variables are replaced with),
  // so we need four-way comparison. The actual values should be treated as arbitrary
  // by client code; they are chosen to make certain logical operations efficient
  public static final int EQUALS = 1;
  public static final int LESS = 1 << 1;
  public static final int GREATER = 1 << 2;
  public static final int UNORDERED = 0;

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

  private static int fourWay(int c) {
    if (c < 0) return LESS;
    if (c == 0) return EQUALS;
    return GREATER;
  }

  // TODO: shortcut comparison of identical terms?
  // TODO: pacman lemma?
  public int compare(Term a, Term b) {
    // variables
    var avars = vars(a);
    var bvars = vars(b);
    var possible = LESS | GREATER;
    for (var kv : avars.entrySet())
      if (kv.getValue() > bvars.getOrDefault(kv.getKey(), 0)) {
        possible &= ~LESS;
        break;
      }
    for (var kv : bvars.entrySet())
      if (kv.getValue() > avars.getOrDefault(kv.getKey(), 0)) {
        possible &= ~GREATER;
        break;
      }
    if (possible == UNORDERED) return UNORDERED;

    // total weight
    var atotalWeight = totalWeight(a);
    var btotalWeight = totalWeight(b);
    if (atotalWeight < btotalWeight) return possible & LESS;
    if (atotalWeight > btotalWeight) return possible & GREATER;

    // different tags or functions mean different symbols
    var asymbolWeight = symbolWeight(a);
    var bsymbolWeight = symbolWeight(b);
    if (asymbolWeight < bsymbolWeight) return possible & LESS;
    if (asymbolWeight > bsymbolWeight) return possible & GREATER;
    assert a.tag() == b.tag();
    assert a.size() == b.size();

    // in some cases, the same tags can still mean different symbols, e.g. constants
    // with different values, or casts to different types
    switch (a.tag()) {
      case CAST -> {
        var c = a.type().compareTo(b.type());
        if (c != 0) return fourWay(c);
      }
      case INTEGER -> {
        return fourWay(a.integerValue().compareTo(b.integerValue()));
      }
      case RATIONAL -> {
        return fourWay(a.rationalValue().compareTo(b.rationalValue()));
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
        return fourWay(a.toString().compareTo(b.toString()));
      }
    }

    // recur
    var n = a.size();
    var i = 0;
    while (i < n && a.get(i).equals(b.get(i))) i++;
    if (i == n) return EQUALS;
    return compare(a.get(i), b.get(i));
  }

  public int compare(boolean pol0, Equation e0, boolean pol1, Equation e1) {
    var c = compare(e0.left, e1.left);
    if (c != EQUALS) return c;
    if (pol0 != pol1) return pol0 ? LESS : GREATER;
    return compare(e0.right, e1.right);
  }
}
