package olivine;

import static org.junit.Assert.*;

import java.util.HashSet;
import org.junit.Test;

public class GraphTest {
  @Test
  public void idom() {
    // https://tanujkhattar.wordpress.com/2016/01/11/dominator-tree-of-a-directed-graph/
    var s = new HashSet<Character>();
    s.add('a');
    s.add('b');
    s.add('c');
    s.add('d');
    s.add('e');
    s.add('f');
    s.add('g');
    s.add('h');
    s.add('i');
    s.add('j');
    s.add('k');
    s.add('l');
    s.add('r');

    var graph = new Graph<Character>();
    graph.add('a', 'd');
    graph.add('b', 'a');
    graph.add('b', 'd');
    graph.add('b', 'e');
    graph.add('c', 'f');
    graph.add('c', 'g');
    graph.add('d', 'l');
    graph.add('e', 'h');
    graph.add('f', 'i');
    graph.add('g', 'i');
    graph.add('g', 'j');
    graph.add('h', 'e');
    graph.add('h', 'k');
    graph.add('i', 'k');
    graph.add('j', 'i');
    graph.add('k', 'i');
    graph.add('k', 'r');
    graph.add('l', 'h');
    graph.add('r', 'a');
    graph.add('r', 'b');
    graph.add('r', 'c');
    assertEquals(graph.nodes(), s);

    var entry = 'r';

    // a dominates a
    for (var a : s) assert graph.dominates(entry, a, a);

    // entry dominates a
    for (var a : s) assert graph.dominates(entry, entry, a);

    // immediate dominators
    for (var b : s) {
      var a = graph.idom(entry, b);
      switch (b) {
        case 'f', 'g' -> {
          assert a != null;
          assertEquals(a.charValue(), 'c');
        }
        case 'j' -> {
          assert a != null;
          assertEquals(a.charValue(), 'g');
        }
        case 'l' -> {
          assert a != null;
          assertEquals(a.charValue(), 'd');
        }
        case 'r' -> {
          assert a == null;
        }
        default -> {
          assert a != null;
          assertEquals(a.charValue(), 'r');
        }
      }
    }
  }

  @Test
  public void domFrontier() {
    // Tiger book page 439.
    var graph = new Graph<Integer>();
    graph.add(1, 2);
    graph.add(1, 5);
    graph.add(1, 9);
    graph.add(2, 3);
    graph.add(3, 3);
    graph.add(3, 4);
    graph.add(4, 13);
    graph.add(5, 6);
    graph.add(5, 7);
    graph.add(6, 4);
    graph.add(6, 8);
    graph.add(7, 8);
    graph.add(7, 12);
    graph.add(8, 13);
    graph.add(8, 5);
    graph.add(9, 10);
    graph.add(9, 11);
    graph.add(10, 12);
    graph.add(11, 12);
    graph.add(12, 13);
    assert graph.nodes().size() == 13;

    var entry = 1;

    // a dominates a
    for (var a = 1; a <= 13; a++) assert graph.dominates(entry, a, a);

    // entry dominates a
    for (var a = 1; a <= 13; a++) assert graph.dominates(entry, entry, a);

    // Dominance frontier
    var r = new HashSet<Integer>();
    r.add(4);
    r.add(5);
    r.add(12);
    r.add(13);
    assert graph.domFrontier(entry, 5).equals(r);
  }

  @Test
  public void transSuccessors() {
    // https://users.aalto.fi/~tjunttil/2020-DP-AUT/notes-sat/cdcl.html
    var graph = new Graph<Integer>();
    graph.add(1, 2);
    graph.add(1, 3);
    graph.add(2, 5);
    graph.add(2, 8);
    graph.add(3, 4);
    graph.add(4, 5);
    graph.add(5, 7);
    graph.add(6, 7);
    graph.add(7, 8);
    graph.add(8, 9);
    graph.add(8, 10);
    graph.add(9, 11);
    graph.add(10, 11);
    graph.add(10, 12);
    graph.add(11, 0);
    graph.add(12, 0);
    assert graph.nodes().size() == 13;
    assert graph.reaches(1, 8);

    // First UIP
    var idom = graph.idom(6, 0);
    assert idom != null;
    assert idom == 8;

    var r = new HashSet<Integer>();
    r.add(7);
    r.add(8);
    r.add(9);
    r.add(10);
    r.add(11);
    r.add(12);
    r.add(0);
    assert graph.transSuccessors(6).equals(r);
  }
}
