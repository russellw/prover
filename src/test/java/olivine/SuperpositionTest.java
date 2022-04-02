package olivine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;

public class SuperpositionTest {
  @Test
  public void solve() {
    var red = new DistinctObject("red");
    var green = new DistinctObject("green");
    var a = new GlobalVar("a", Type.INDIVIDUAL);
    var b = new GlobalVar("b", Type.INDIVIDUAL);
    var p1 = new Func("p1", Type.BOOLEAN, Type.INDIVIDUAL);
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    var clauses = new ArrayList<Clause>();
    boolean sat;
    final int clauseLimit = 1000000;
    final long steps = 1000;

    // true
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // false
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // red=red
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, red, red));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // red!=red
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, red, red));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // red=green
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, red, green));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // red!=green
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, red, green));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // a=a
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, a));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // a!=a
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, a, a));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // a=b
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, b));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // a!=b
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, a, b));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // x=x
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, x, x));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // x!=x
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, x, x));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // x=y
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, x, y));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // x!=y. this differs from a!=b; it is unsatisfiable. Semantically because of the difference
    // between
    // the implied 'exists' versus 'all' for global versus local variables; there exists an
    // assignment of global variable values
    // that makes a!=b, but none that forces x!=y for all x, y. Mechanically because of equality
    // resolution
    // (which uses the fact that we are free to substitute terms for variables in an equation).
    // This is therefore a test that equality resolution (or some equivalent) is implemented
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, x, y));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // p(red)
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, red));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // !p(red)
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, red));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // p(a)
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, a));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // !p(a)
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, a));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // p(x)
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, x));
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // !p(x)
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, x));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertTrue(sat);

    // !p(a) & p(x)
    clauses.clear();

    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, a));
    positive.clear();
    clauses.add(new Clause(negative, positive));

    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, x));
    clauses.add(new Clause(negative, positive));

    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // !p(a) & (p(x) | p(y)). this can be solved with equality factoring,
    // but does not suffice to test such, because it has an alternative proof
    clauses.clear();

    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, a));
    positive.clear();
    clauses.add(new Clause(negative, positive));

    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, x));
    positive.add(Term.of(Tag.CALL, p1, y));
    clauses.add(new Clause(negative, positive));

    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // (!p(a) | !p(b)) & (p(x) | p(y)). This is a test of equality factoring
    clauses.clear();

    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, a));
    negative.add(Term.of(Tag.CALL, p1, b));
    positive.clear();
    clauses.add(new Clause(negative, positive));

    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, x));
    positive.add(Term.of(Tag.CALL, p1, y));
    clauses.add(new Clause(negative, positive));

    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // p(a) & (!p(x) | !p(y))
    clauses.clear();

    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, a));
    clauses.add(new Clause(negative, positive));

    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, x));
    negative.add(Term.of(Tag.CALL, p1, y));
    positive.clear();
    clauses.add(new Clause(negative, positive));

    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);

    // (p(a) | p(b)) & (!p(x) | !p(y))
    clauses.clear();

    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, a));
    positive.add(Term.of(Tag.CALL, p1, b));
    clauses.add(new Clause(negative, positive));

    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, x));
    negative.add(Term.of(Tag.CALL, p1, y));
    positive.clear();
    clauses.add(new Clause(negative, positive));

    sat = Superposition.sat(clauses, clauseLimit, steps);
    assertFalse(sat);
  }

  @Test(expected = Fail.class)
  public void complicated() {
    var f = new Func("f", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);
    var g = new Func("g", Type.INDIVIDUAL, Type.INDIVIDUAL);
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var z = new Var(Type.INDIVIDUAL);
    var w = new Var(Type.INDIVIDUAL);
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    var clauses = new ArrayList<Clause>();
    final int clauseLimit = 1000000;
    final long steps = 1;

    var gx = g.call(x);
    var gy = g.call(y);
    var gw = g.call(w);

    var c0 =
        f.call(
            g.call(
                f.call(
                    gx,
                    f.call(
                        g.call(
                            f.call(
                                gy,
                                f.call(
                                    g.call(
                                        f.call(
                                            g.call(f.call(z, g.call(f.call(g.call(y), x)))),
                                            f.call(z, gx))),
                                    gx))),
                        gx))),
            g.call(f.call(gx, x)));
    c0.check(Type.INDIVIDUAL);
    positive.add(Term.of(Tag.EQUALS, c0, x));
    var c = new Clause(negative, positive);
    positive.clear();

    var d0 =
        f.call(
            g.call(f.call(g.call(f.call(x, gy)), f.call(x, g.call(f.call(z, gw))))),
            g.call(f.call(g.call(f.call(z, gw)), f.call(z, gw))));
    d0.check(Type.INDIVIDUAL);
    var d1 = f.call(z, g.call(f.call(g.call(f.call(gy, g.call(f.call(gw, w)))), w)));
    d1.check(Type.INDIVIDUAL);
    positive.add(Term.of(Tag.EQUALS, d0, d1));
    var d = new Clause(negative, positive);

    clauses.add(c);
    clauses.add(d);
    Superposition.sat(clauses, clauseLimit, steps);
  }
}
