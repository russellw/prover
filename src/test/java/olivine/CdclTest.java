package olivine;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class CdclTest {
  private static final class Assignment {
    final Term atom;
    final boolean value;
    final Clause reason;

    @Override
    public String toString() {
      return atom.toString();
    }

    Assignment(Term atom, boolean value, Clause reason) {
      this.atom = atom;
      this.value = value;
      this.reason = reason;
    }
  }

  private final List<Assignment> trail = new ArrayList<>();

  private Assignment assignment(Term a) {
    for (var assignment : trail) if (assignment.atom == a) return assignment;
    throw new IllegalStateException(a.toString());
  }

  private Graph<Assignment> implicationGraph() {
    var graph = new Graph<Assignment>();
    for (var assignment : trail) {
      var c = assignment.reason;
      if (c != null)
        for (var a : c.literals) if (a != assignment.atom) graph.add(assignment(a), assignment);
    }
    return graph;
  }

  private void assertSuccessors(Graph<Assignment> graph, Term x, Term... ys) {
    var zs = new HashSet<Term>();
    for (var assignment : graph.successors(assignment(x))) zs.add(assignment.atom);
    assertEquals(Set.of(ys), zs);
  }

  @Test
  public void graph() {
    // https://users.aalto.fi/~tjunttil/2020-DP-AUT/notes-sat/cdcl.html
    var x1 = new GlobalVar("x1", Type.BOOLEAN);
    var x2 = new GlobalVar("x2", Type.BOOLEAN);
    var x3 = new GlobalVar("x3", Type.BOOLEAN);
    var x4 = new GlobalVar("x4", Type.BOOLEAN);
    var x5 = new GlobalVar("x5", Type.BOOLEAN);
    var x6 = new GlobalVar("x6", Type.BOOLEAN);
    var x7 = new GlobalVar("x7", Type.BOOLEAN);
    var x8 = new GlobalVar("x8", Type.BOOLEAN);
    var x9 = new GlobalVar("x9", Type.BOOLEAN);
    var x10 = new GlobalVar("x10", Type.BOOLEAN);
    var x11 = new GlobalVar("x11", Type.BOOLEAN);
    var x12 = new GlobalVar("x12", Type.BOOLEAN);

    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    negative.add(x1);
    negative.add(x2);
    var c_1_2 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x1);
    positive.add(x3);
    var c_1_3 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x3);
    negative.add(x4);
    var c_3_4 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    positive.add(x2);
    positive.add(x4);
    positive.add(x5);
    var c_2_4_5 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x5);
    positive.add(x6);
    negative.add(x7);
    var c_5_6_7 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    positive.add(x2);
    positive.add(x7);
    positive.add(x8);
    var c_2_7_8 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x8);
    negative.add(x9);
    var c_8_9 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x8);
    positive.add(x10);
    var c_8_10 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    positive.add(x9);
    negative.add(x10);
    positive.add(x11);
    var c_9_10_11 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x10);
    negative.add(x12);
    var c_10_12 = new Clause(negative, positive);

    negative.clear();
    positive.clear();
    negative.add(x11);
    positive.add(x12);
    var c_11_12 = new Clause(negative, positive);

    trail.add(new Assignment(x1, true, null));
    trail.add(new Assignment(x2, false, c_1_2));
    trail.add(new Assignment(x3, true, c_1_3));
    trail.add(new Assignment(x4, false, c_3_4));
    trail.add(new Assignment(x5, true, c_2_4_5));

    var graph = implicationGraph();
    assertSuccessors(graph, x1, x2, x3);
    assertSuccessors(graph, x2, x5);
    assertSuccessors(graph, x3, x4);
    assertSuccessors(graph, x4, x5);
    assertSuccessors(graph, x5);

    trail.add(new Assignment(x6, false, null));
    trail.add(new Assignment(x7, false, c_5_6_7));
    trail.add(new Assignment(x8, true, c_2_7_8));
    trail.add(new Assignment(x9, false, c_8_9));
    trail.add(new Assignment(x10, true, c_8_10));
    trail.add(new Assignment(x11, true, c_9_10_11));
    trail.add(new Assignment(x12, false, c_10_12));

    graph = implicationGraph();
    assertSuccessors(graph, x6, x7);
    assertSuccessors(graph, x7, x8);
    assertSuccessors(graph, x8, x9, x10);
    assertSuccessors(graph, x9, x11);
    assertSuccessors(graph, x10, x11, x12);
  }
}
