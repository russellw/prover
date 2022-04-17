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

  public Set<Term> freeVars() {
    var free = new LinkedHashSet<Term>();
    for (var a : literals) a.freeVars(Set.of(), free);
    return free;
  }

  public static List<Clause> replace(FMap map, List<Clause> clauses) {
    var v = new ArrayList<Clause>(clauses.size());
    for (var c : clauses) {
      c = c.replace(map);
      if (!c.isTrue()) v.add(c);
    }
    return v;
  }

  public Clause replace(FMap map) {
    var negative = new ArrayList<Term>(negativeSize);
    for (var i = 0; i < negativeSize; i++) negative.add(literals[i].replace(map));

    var positive = new ArrayList<Term>(positiveSize());
    for (var i = negativeSize; i < literals.length; i++) positive.add(literals[i].replace(map));

    return new Clause(negative, positive);
  }

  public static boolean propositional(List<Clause> clauses) {
    for (var c : clauses) for (var a : c.literals) if (!(a instanceof GlobalVar)) return false;
    return true;
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
}
