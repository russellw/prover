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

      Clause c1 = null;
      for (var c : clauses) {
        c1 = c.replace(map);
        if (c1.isTrue()) continue;
        switch (c1.literals.length) {
          case 1 -> {
            // unit propagation
            trail.add(new Assignment(c1.literals[0], c1.negativeSize == 0, c));
            continue loop;
          }
          case 0 -> {
            // contradiction, backtrack
            var graph = implicationGraph();
          }
        }
      }

      // no clause remains unsatisfied
      if (c1 == null) {
        result = true;
        return;
      }

      // choice
      trail.add(new Assignment(c1.literals[0], false, null));
    }
    throw new Fail();
  }

  public static boolean sat(List<Clause> clauses, long steps) {
    assert Clause.propositional(clauses);
    // TODO: make sure no duplicate literals
    return new Cdcl(clauses, steps).result;
  }
}
