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

    var g = new Graph<Character>();
    g.add('a', 'd');
    g.add('b', 'a');
    g.add('b', 'd');
    g.add('b', 'e');
    g.add('c', 'f');
    g.add('c', 'g');
    g.add('d', 'l');
    g.add('e', 'h');
    g.add('f', 'i');
    g.add('g', 'i');
    g.add('g', 'j');
    g.add('h', 'e');
    g.add('h', 'k');
    g.add('i', 'k');
    g.add('j', 'i');
    g.add('k', 'i');
    g.add('k', 'r');
    g.add('l', 'h');
    g.add('r', 'a');
    g.add('r', 'b');
    g.add('r', 'c');
    assertEquals(g.nodes(), s);

    var entry = 'r';

    // a dominates a
    for (var a : s) assert g.dominates(entry, a, a);

    // entry dominates a
    for (var a : s) assert g.dominates(entry, entry, a);

    // immediate dominators
    for (var b : s) {
      var a = g.idom(entry, b);
      switch (b) {
        case 'f':
        case 'g':
          assert a != null;
          assertEquals(a.charValue(), 'c');
          break;
        case 'j':
          assert a != null;
          assertEquals(a.charValue(), 'g');
          break;
        case 'l':
          assert a != null;
          assertEquals(a.charValue(), 'd');
          break;
        case 'r':
          assert a == null;
          break;
        default:
          assert a != null;
          assertEquals(a.charValue(), 'r');
          break;
      }
    }
  }

  @Test
  public void domFrontier() {
    // Tiger book page 439.
    var g = new Graph<Integer>();
    g.add(1, 2);
    g.add(1, 5);
    g.add(1, 9);
    g.add(2, 3);
    g.add(3, 3);
    g.add(3, 4);
    g.add(4, 13);
    g.add(5, 6);
    g.add(5, 7);
    g.add(6, 4);
    g.add(6, 8);
    g.add(7, 8);
    g.add(7, 12);
    g.add(8, 13);
    g.add(8, 5);
    g.add(9, 10);
    g.add(9, 11);
    g.add(10, 12);
    g.add(11, 12);
    g.add(12, 13);
    assert g.nodes().size() == 13;

    var entry = 1;

    // a dominates a
    for (var a = 1; a <= 13; a++) assert g.dominates(entry, a, a);

    // entry dominates a
    for (var a = 1; a <= 13; a++) assert g.dominates(entry, entry, a);

    // Dominance frontier
    var r = new HashSet<Integer>();
    r.add(4);
    r.add(5);
    r.add(12);
    r.add(13);
    assert g.domFrontier(entry, 5).equals(r);
  }

  @Test
  public void transSuccessors() {
    // https://users.aalto.fi/~tjunttil/2020-DP-AUT/notes-sat/cdcl.html
    var g = new Graph<Integer>();
    g.add(1, 2);
    g.add(1, 3);
    g.add(2, 5);
    g.add(2, 8);
    g.add(3, 4);
    g.add(4, 5);
    g.add(5, 7);
    g.add(6, 7);
    g.add(7, 8);
    g.add(8, 9);
    g.add(8, 10);
    g.add(9, 11);
    g.add(10, 11);
    g.add(10, 12);
    g.add(11, 0);
    g.add(12, 0);
    assert g.nodes().size() == 13;
    assert g.reaches(1, 8);

    // First UIP
    var idom = g.idom(6, 0);
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
    assert g.transSuccessors(6).equals(r);
  }
}
