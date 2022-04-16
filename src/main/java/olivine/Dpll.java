package olivine;

import java.util.List;

public final class Dpll {
  private long steps;
  private final boolean result;

  private static boolean isFalse(List<Clause> clauses) {
    for (var c : clauses) if (c.isFalse()) return true;
    return false;
  }

  private static boolean isTrue(List<Clause> clauses) {
    return clauses.isEmpty();
  }

  private boolean sat(List<Clause> clauses) {
    if (steps-- == 0) throw new Fail();
    if (isFalse(clauses)) return false;
    if (isTrue(clauses)) return true;

    // unit clause
    for (var c : clauses)
      if (c.literals.length == 1) {
        var map = FMap.EMPTY.add(c.literals[0], Term.of(c.negativeSize == 0));
        return sat(Clause.replace(map, clauses));
      }

    // search
    var a = clauses.get(0).literals[0];
    return sat(Clause.replace(FMap.EMPTY.add(a, Term.FALSE), clauses))
        || sat(Clause.replace(FMap.EMPTY.add(a, Term.TRUE), clauses));
  }

  private Dpll(List<Clause> clauses, long steps) {
    this.steps = steps;
    result = sat(clauses);
  }

  public static boolean sat(List<Clause> clauses, long steps) {
    assert Clause.propositional(clauses);
    for (var c : clauses) assert !c.isTrue();
    return new Dpll(clauses, steps).result;
  }
}
