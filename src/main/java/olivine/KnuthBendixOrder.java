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
  // TODO: change to enum PartialOrder?
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

  public int compare(Equation a, Equation b) {
    switch (compare(a.left, b.left)) {
      case EQUALS -> {
        // left terms are equal, so answer depends only on the right
        return compare(a.right, b.right);
      }
      case GREATER -> {
        // we have a result on the left, now check the right
        switch (compare(a.right, b.right)) {
          case EQUALS, GREATER -> {
            // result on the right confirms or will not oppose the left
            return GREATER;
          }
          case LESS -> {
            // opposite result on the right, but now we know which terms are larger, so we can
            // compare them. (on the face of it, we don't have the larger terms *within* each
            // equation, which is what we need to decide the answer, but we can rely on
            // transitivity, e.g. if a.left > b.right and b.right > a.right then a.left > a.right.)
            var c = compare(a.left, b.right);

            // if the larger terms give a result, or are unordered, that's the answer
            if (c != EQUALS) return c;

            // otherwise, compare the smaller terms
            return compare(a.right, b.left);
          }
          case UNORDERED -> {
            // no result on the right, but if a.left is greater than both b terms, then we do have
            // an answer,
            // because it doesn't matter what a.right ultimately resolves to
            if (compare(a.left, b.right) == GREATER) return GREATER;
          }
        }
      }
      case LESS -> {
        // we have a result on the left, now check the right
        switch (compare(a.right, b.right)) {
          case EQUALS, LESS -> {
            // result on the right confirms or will not oppose the left
            return LESS;
          }
          case GREATER -> {
            // opposite result on the right, but now we know which terms are larger, so we can
            // compare them
            var c = compare(a.right, b.left);

            // if the larger terms give a result, or are unordered, that's the answer
            if (c != EQUALS) return c;

            // otherwise, compare the smaller terms
            return compare(a.left, b.right);
          }
          case UNORDERED -> {
            // no result on the right, but if b.left is greater than both a terms, then we do have
            // an answer,
            // because it doesn't matter what b.right ultimately resolves to
            if (compare(a.right, b.left) == LESS) return LESS;
          }
        }
      }
      case UNORDERED -> {
        // if the left terms are unordered, it is still possible to have an answer, but the
        // requirements
        // are quite specific. if a.right is greater than both b terms, then it doesn't matter what
        // a.left ultimately resolves to; no matter whether it is greater or less than a.right,
        // the answer will stand. But if a.right is merely equal to one of the b terms, the answer
        // could end up depending on whether a.left ultimately resolves to something
        // greater than both, or less than both, so we have no order. The same applies if b.right
        // is greater than both a terms
        switch (compare(a.right, b.right)) {
          case GREATER -> {
            if (compare(a.right, b.left) == GREATER) return GREATER;
          }
          case LESS -> {
            if (compare(a.left, b.right) == LESS) return LESS;
          }
        }
      }
    }
    return UNORDERED;
  }

  public int comparePN(Equation a, Equation b) {
    // compare a positive and negative equation. We don't need both PN and NP; if we have NP,
    // we can calculate PN and flip the answer around

    // Why PN rather than NP, when logically either would be an equally valid basis? Because PN
    // is perhaps intuitively simpler: a is greater when it has a term that is strictly greater than
    // both b
    // terms. If its greatest term is merely equal to one of the b terms, then the answer will be
    // decided by
    // polarity, and a will be less. If we cannot prove that either of these will ultimately be the
    // case, then the equations
    // are unordered

    // We don't need to worry about the case where the equations are equal, because equations are
    // only compared
    // to other equations in the same clause, and a clause containing equal positive and negative
    // equations
    // would have been discarded as a tautology
    switch (compare(a.left, b.left)) {
      case EQUALS -> {
        // when comparing equations with the same polarity, if they were equal on the left,
        // we could ignore the left and return the result of comparing the right. in this case we
        // cannot do that,
        // because if the right terms are smaller, the answer would instead be decided by polarity
      }
      case GREATER -> {
        // we have a result on the left, now check the right
        switch (compare(a.right, b.right)) {
          case GREATER -> {
            // both sides agree on the answer
            return GREATER;
          }
          case EQUALS, UNORDERED -> {
            // if there is no result on the right, but a.left is greater than both b terms, then we
            // do have
            // an answer,
            // because it doesn't matter what a.right ultimately resolves to.
            // so a.left > b.right is a sufficient condition

            // if the right terms are equal, the result on the right  will not oppose the left, but
            // that's not necessarily enough;
            // the result on the left is only decisive if a.left is greater than the right
            // terms, otherwise a substitution could produce ground equations
            // where the right terms are greater than the left, and equal,
            // leaving the answer to be decided by polarity.
            // so a.left > b.right is a necessary condition
            if (compare(a.left, b.right) == GREATER) return GREATER;
          }
          case LESS -> {
            // opposite result on the right, but now we know which terms are larger, so we can
            // compare them
            var c = compare(a.left, b.right);

            // if the larger terms give a result, or are unordered, that's the answer
            if (c != EQUALS) return c;

            // otherwise, the answer is decided by polarity
            return LESS;
          }
        }
      }
      case UNORDERED -> {
        // if the left terms are unordered, it is still possible to have an answer, but the
        // requirements
        // are quite specific. if a.right is greater than both b terms, then it doesn't matter what
        // a.left ultimately resolves to; no matter whether it is greater or less than a.right,
        // the answer will stand. But if a.right is merely equal to one of the b terms, the answer
        // could end up depending on whether a.left ultimately resolves to something
        // greater than both, or less than both, so we have no order. The same applies if b.right
        // is greater than both a terms
        switch (compare(a.right, b.right)) {
          case GREATER -> {
            if (compare(a.right, b.left) == GREATER) return GREATER;
          }
          case LESS -> {
            if (compare(a.left, b.right) == LESS) return LESS;
          }
        }
      }
    }
    return UNORDERED;
  }

  private static int flip(int c) {
    return switch (c) {
      case GREATER -> LESS;
      case LESS -> GREATER;
      default -> c;
    };
  }

  public int compare(boolean apol, Equation a, boolean bpol, Equation b) {
    if (apol == bpol) return compare(a, b);
    return apol ? comparePN(a, b) : flip(comparePN(b, a));
  }
}
