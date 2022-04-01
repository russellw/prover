package olivine;

import java.util.*;

public final class Clause {
  final Term[] literals;
  final int negativeSize;

  private Clause(Term[] literals, int negativeSize) {
    this.literals = literals;
    this.negativeSize = negativeSize;
  }

  public Clause renameVars() {
    var map = new HashMap<Term, Term>();
    var v = new Term[literals.length];
    for (var i = 0; i < v.length; i++) {
      v[i] =
          literals[i].mapLeaves(
              a -> {
                if (a instanceof Var) {
                  var b = map.get(a);
                  if (b == null) {
                    b = new Var(a.type());
                    map.put(a, b);
                  }
                  return b;
                }
                return a;
              });
    }
    return new Clause(v, negativeSize);
  }

  @Override
  public String toString() {
    return String.format("%s => %s", Arrays.toString(negative()), Arrays.toString(positive()));
  }

  public long volume() {
    var n = literals.length * 2L;
    for (var a : literals) n += a.symbolCount();
    return n;
  }

  public Set<Term> freeVars() {
    var free = new LinkedHashSet<Term>();
    for (var a : literals) a.freeVars(Set.of(), free);
    return free;
  }

  public Clause(List<Term> negative, List<Term> positive) {
    // Simplify
    for (var i = 0; i < negative.size(); i++) negative.set(i, negative.get(i).simplify());
    for (var i = 0; i < positive.size(); i++) positive.set(i, positive.get(i).simplify());

    // Redundancy
    negative.removeIf(a -> a == Term.TRUE);
    positive.removeIf(a -> a == Term.FALSE);

    // Tautology?
    if (tautology(negative, positive)) {
      literals = new Term[] {Term.TRUE};
      negativeSize = 0;
      return;
    }

    // Literals
    negativeSize = negative.size();
    literals = new Term[negativeSize + positive.size()];
    for (var i = 0; i < negativeSize; i++) literals[i] = negative.get(i);
    for (var i = 0; i < positive.size(); i++) literals[negativeSize + i] = positive.get(i);
  }

  public Term[] negative() {
    return Arrays.copyOf(literals, negativeSize);
  }

  public Term[] positive() {
    return Arrays.copyOfRange(literals, negativeSize, literals.length);
  }

  public int positiveSize() {
    return literals.length - negativeSize;
  }

  public boolean isFalse() {
    return literals.length == 0;
  }

  public boolean isTrue() {
    if (literals.length == 1 && literals[0] == Term.TRUE) {
      assert negativeSize == 0;
      return true;
    }
    return false;
  }

  private static boolean tautology(List<Term> negative, List<Term> positive) {
    if (negative.contains(Term.FALSE)) return true;
    if (positive.contains(Term.TRUE)) return true;
    for (var a : negative) if (positive.contains(a)) return true;
    return false;
  }

  public Term term() {
    var v = new Term[literals.length];
    for (var i = 0; i < literals.length; i++) {
      var a = literals[i];
      if (i < negativeSize) a = Term.of(Tag.NOT, a);
      v[i] = a;
    }
    return switch (v.length) {
      case 0 -> Term.FALSE;
      case 1 -> v[0];
      default -> Term.of(Tag.OR, v);
    };
  }
}
