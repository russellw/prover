package olivine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Subsumption {
  // Time limit
  private int steps;

  private FMap search(FMap map, Term[] c, Term[] c2, Term[] d, Term[] d2) {
    if (steps-- == 0) throw new TimeoutException();

    // Matched everything in one polarity
    if (c.length == 0) {
      // Already matched everything in the other polarity
      if (c2 == null) return map;

      // Try the other polarity
      return search(map, c2, null, d2, null);
    }

    // Try matching literals
    for (var ci = 0; ci < c.length; ci++) {
      Term[] c1 = null;
      var ce = new Equation(c[ci]);
      for (var di = 0; di < d.length; di++) {
        Term[] d1 = null;
        var de = new Equation(d[di]);

        // Try orienting equation one way
        var m = ce.left.match(map, de.left);
        if (m != null) {
          m = ce.right.match(m, de.right);
          if (m != null) {
            if (c1 == null) c1 = Term.remove(c, ci);
            d1 = Term.remove(d, di);
            m = search(m, c1, c2, d1, d2);
            if (m != null) return m;
          }
        }

        // And the other way
        m = ce.left.match(map, de.right);
        if (m != null) {
          m = ce.right.match(m, de.left);
          if (m != null) {
            if (c1 == null) c1 = Term.remove(c, ci);
            if (d1 == null) d1 = Term.remove(d, di);
            m = search(m, c1, c2, d1, d2);
            if (m != null) return m;
          }
        }
      }
    }

    // No match
    return null;
  }

  public boolean subsumes(Clause c, Clause d) {
    assert Collections.disjoint(c.freeVars(), d.freeVars());

    // Negative and positive literals must subsume separately
    var c1 = c.negative();
    var c2 = c.positive();
    var d1 = d.negative();
    var d2 = d.positive();

    // Fewer literals typically fail faster
    if (c2.length < c1.length) {
      // Swap negative and positive
      var ct = c1;
      c1 = c2;
      c2 = ct;

      // And in the other clause
      var dt = d1;
      d1 = d2;
      d2 = dt;
    }

    try {
      // Search for nondeterministic matches.
      // Worst-case time is exponential,
      // so give up if taking too long
      steps = 1000;
      var map = search(FMap.EMPTY, c1, c2, d1, d2);
      return map != null;
    } catch (TimeoutException e) {
      return false;
    }
  }

  public boolean subsumesForward(List<Clause> clauses, Clause c) {
    for (var d : clauses) if (subsumes(d, c)) return true;
    return false;
  }

  public List<Clause> subsumeBackward(Clause c, List<Clause> clauses) {
    var v = new ArrayList<Clause>(clauses.size());
    for (var d : clauses) if (!subsumes(c, d)) v.add(d);
    return v;
  }
}
