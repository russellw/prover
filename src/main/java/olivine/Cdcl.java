package olivine;

import java.util.ArrayList;
import java.util.List;

public final class Cdcl {
  private final class Assignment {
    final Term term;
    final boolean value;
    final Clause reason;

    Assignment(Term term, boolean value, Clause reason) {
      this.term = term;
      this.value = value;
      this.reason = reason;
    }
  }

  private final List<Clause> clauses;
  private final boolean result;

  private static FMap map(List<Assignment> trail) {
    var map = FMap.EMPTY;
    for (var assignment : trail) map = map.add(assignment.term, Term.of(assignment.value));
    return map;
  }

  private static boolean isFalse(List<Clause> clauses) {
    for (var c : clauses) if (c.isFalse()) return true;
    return false;
  }

  private static boolean isTrue(List<Clause> clauses) {
    return clauses.isEmpty();
  }

  private static Graph<Assignment> implicationGraph(List<Assignment> trail) {
    var graph = new Graph<Assignment>();
    for (var assignment : trail) {}

    return graph;
  }

  private Cdcl(List<Clause> clauses, long steps) {
    this.clauses = clauses;
    var trail = new ArrayList<Assignment>();
    loop:
    while (steps-- > 0) {
      var map = map(trail);
      var cs = Clause.replace(map, clauses);

      // solution
      if (isTrue(cs)) {
        result = true;
        return;
      }

      // contradiction
      if (isFalse(cs)) {}

      // unit propagation
      for (var c : clauses) {
        var c1 = c.replace(map);
        if (c1.literals.length == 1) {
          trail.add(new Assignment(c1.literals[0], c1.negativeSize == 0, c));
          continue loop;
        }
      }

      // choice
      trail.add(new Assignment(cs.get(0).literals[0], false, null));
    }
    throw new Fail();
  }

  public static boolean sat(List<Clause> clauses, long steps) {
    assert Clause.propositional(clauses);
    // TODO: make sure no duplicate literals
    return new Cdcl(clauses, steps).result;
  }
}
