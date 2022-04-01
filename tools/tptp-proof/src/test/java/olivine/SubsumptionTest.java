package olivine;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import org.junit.Test;

public class SubsumptionTest {
  private void typeCheck(Clause c) {}

  private Term ofInt(int n) {
    return Term.of(BigInteger.valueOf(n));
  }

  @Test
  public void subsumes() {
    var a = new GlobalVar("a", Type.INTEGER);
    var a1 = new Func("a1", Type.INTEGER, Type.INTEGER);
    var b = new GlobalVar("b", Type.INTEGER);
    var p = new GlobalVar("p", Type.BOOLEAN);
    var p1 = new Func("p1", Type.BOOLEAN, Type.INTEGER);
    var p2 = new Func("p2", Type.BOOLEAN, Type.INTEGER, Type.INTEGER);
    var q = new GlobalVar("q", Type.BOOLEAN);
    var q1 = new Func("q1", Type.BOOLEAN, Type.INTEGER);
    var q2 = new Func("q2", Type.BOOLEAN, Type.INTEGER, Type.INTEGER);
    var x = new Var(Type.INTEGER);
    var y = new Var(Type.INTEGER);
    var z = new Var(Type.INTEGER);
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    Clause c, d;
    var subsumption = new Subsumption();

    // false <= false
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));

    // false <= p
    negative.clear();
    positive.clear();
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p);
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p <= p
    negative.clear();
    positive.clear();
    positive.add(p);
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p);
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));

    // !p <= !p
    negative.clear();
    negative.add(p);
    positive.clear();
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    negative.add(p);
    positive.clear();
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));

    // p <= p | p
    negative.clear();
    positive.clear();
    positive.add(p);
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p);
    positive.add(p);
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p !<= !p
    negative.clear();
    positive.clear();
    positive.add(p);
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    negative.add(p);
    positive.clear();
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertFalse(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p | q <= q | p
    negative.clear();
    positive.clear();
    positive.add(p);
    positive.add(q);
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(q);
    positive.add(p);
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertTrue(subsumption.subsumes(d, c));

    // p | q <= p | q | p
    negative.clear();
    positive.clear();
    positive.add(p);
    positive.add(q);
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p);
    positive.add(q);
    positive.add(p);
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p(a) | p(b) | q(a) | q(b) | <= p(a) | q(a) | p(b) | q(b)
    negative.clear();
    positive.clear();
    positive.add(p1.call(a));
    positive.add(p1.call(b));
    positive.add(q1.call(a));
    positive.add(q1.call(b));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p1.call(a));
    positive.add(q1.call(a));
    positive.add(p1.call(b));
    positive.add(q1.call(b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertTrue(subsumption.subsumes(d, c));

    // p(6,7) | p(4,5) <= q(6,7) | q(4,5) | p(0,1) | p(2,3) | p(4,4) | p(4,5) | p(6,6) | p(6,7)
    negative.clear();
    positive.clear();
    positive.add(p2.call(ofInt(6), ofInt(7)));
    positive.add(p2.call(ofInt(4), ofInt(5)));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(q2.call(ofInt(6), ofInt(7)));
    positive.add(q2.call(ofInt(4), ofInt(5)));
    positive.add(p2.call(ofInt(0), ofInt(1)));
    positive.add(p2.call(ofInt(2), ofInt(3)));
    positive.add(p2.call(ofInt(4), ofInt(4)));
    positive.add(p2.call(ofInt(4), ofInt(5)));
    positive.add(p2.call(ofInt(6), ofInt(6)));
    positive.add(p2.call(ofInt(6), ofInt(7)));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p(x,y) <= p(a,b)
    negative.clear();
    positive.clear();
    positive.add(p2.call(x, y));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p2.call(a, b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p(x,x) !<= p(a,b)
    negative.clear();
    positive.clear();
    positive.add(p2.call(x, x));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p2.call(a, b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertFalse(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p(x) <= p(y)
    negative.clear();
    positive.clear();
    positive.add(p1.call(x));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p1.call(y));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertTrue(subsumption.subsumes(d, c));

    // p(x) | p(a(x)) | p(a(a(x))) <= p(y) | p(a(y)) | p(a(a(y)))
    negative.clear();
    positive.clear();
    positive.add(p1.call(x));
    positive.add(p1.call(a1.call(x)));
    positive.add(p1.call(a1.call(a1.call(x))));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p1.call(y));
    positive.add(p1.call(a1.call(y)));
    positive.add(p1.call(a1.call(a1.call(y))));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertTrue(subsumption.subsumes(d, c));

    // p(x) | p(a) <= p(a) | p(b)
    negative.clear();
    positive.clear();
    positive.add(p1.call(x));
    positive.add(p1.call(a));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p1.call(a));
    positive.add(p1.call(b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p(x) | p(a(x)) <= p(a(y)) | p(y)
    negative.clear();
    positive.clear();
    positive.add(p1.call(x));
    positive.add(p1.call(a1.call(x)));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p1.call(a1.call(y)));
    positive.add(p1.call(y));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertTrue(subsumption.subsumes(d, c));

    // p(x) | p(a(x)) | p(a(a(x))) <= p(a(a(y))) | p(a(y)) | p(y)
    negative.clear();
    positive.clear();
    positive.add(p1.call(x));
    positive.add(p1.call(a1.call(x)));
    positive.add(p1.call(a1.call(a1.call(x))));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p1.call(a1.call(a1.call(y))));
    positive.add(p1.call(a1.call(y)));
    positive.add(p1.call(y));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertTrue(subsumption.subsumes(d, c));

    // (a = x) <= (a = b)
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, x));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // (x = a) <= (a = b)
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, x, a));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // !p(y) | !p(x) | q(x) <= !p(a) | !p(b) | q(b)
    negative.clear();
    negative.add(p1.call(y));
    negative.add(p1.call(x));
    positive.clear();
    positive.add(q1.call(x));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    negative.add(p1.call(a));
    negative.add(p1.call(b));
    positive.clear();
    positive.add(q1.call(b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // !p(x) | !p(y) | q(x) <= !p(a) | !p(b) | q(b)
    negative.clear();
    negative.add(p1.call(x));
    negative.add(p1.call(y));
    positive.clear();
    positive.add(q1.call(x));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    negative.add(p1.call(a));
    negative.add(p1.call(b));
    positive.clear();
    positive.add(q1.call(b));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertTrue(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // (x = a) | (1 = y) !<= (1 = a) | (z = 0)
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, x, a));
    positive.add(Term.of(Tag.EQUALS, ofInt(1), y));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, ofInt(1), a));
    positive.add(Term.of(Tag.EQUALS, z, ofInt(0)));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertFalse(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));

    // p(x,a(x)) !<= p(a(y),a(y))
    negative.clear();
    positive.clear();
    positive.add(p2.call(x, a1.call(x)));
    c = new Clause(negative, positive, null);
    typeCheck(c);
    negative.clear();
    positive.clear();
    positive.add(p2.call(a1.call(y), a1.call(y)));
    d = new Clause(negative, positive, null);
    typeCheck(d);
    assertFalse(subsumption.subsumes(c, d));
    assertFalse(subsumption.subsumes(d, c));
  }
}
