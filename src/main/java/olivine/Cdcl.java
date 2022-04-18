package olivine;

import java.util.ArrayList;
import java.util.List;

public final class Cdcl {
  private static final class Assignment {
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
  private final List<Assignment> trail = new ArrayList<>();
  private final boolean result;

  private Assignment assignment(Term a) {
    for (var assignment : trail) if (assignment.term == a) return assignment;
    throw new IllegalStateException(a.toString());
  }

  private Graph<Assignment> implicationGraph() {
    var graph = new Graph<Assignment>();
    for (var assignment : trail) {
      var c = assignment.reason;
      if (c != null)
        for (var a : c.literals) if (a != assignment.term) graph.add(assignment(a), assignment);
    }
    return graph;
  }

  private Cdcl(List<Clause> clauses, long steps) {
    this.clauses = clauses;
    loop:
    while (steps-- > 0) {
      var map = FMap.EMPTY;
      for (var assignment : trail) map = map.add(assignment.term, Term.of(assignment.value));

      // contradiction?
      for (var c : clauses) {
        var c1 = c.replace(map);
        if (c1.isFalse()) {
          //  backtrack
          var graph = implicationGraph();
          continue loop;
        }
      }

      // unit propagation
      Clause c2 = null;
      for (var c : clauses) {
        var c1 = c.replace(map);
        if (c1.isTrue()) continue;
        if (c1.literals.length == 1) {
          trail.add(new Assignment(c1.literals[0], c1.negativeSize == 0, c));
          continue loop;
        }
        if (c2 == null) c2 = c1;
      }

      // no clause remains unsatisfied
      if (c2 == null) {
        result = true;
        return;
      }

      // choice
      trail.add(new Assignment(c2.literals[0], false, null));
    }
    throw new Fail();
  }

  public static boolean sat(List<Clause> clauses, long steps) {
    assert Clause.propositional(clauses);
    // TODO: make sure no duplicate literals
    return new Cdcl(clauses, steps).result;
  }
}
