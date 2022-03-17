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
}
