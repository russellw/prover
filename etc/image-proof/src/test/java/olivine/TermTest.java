package olivine;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Set;
import org.junit.Test;

public class TermTest {
  @Test
  public void freeVars() {
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var z = new Var(Type.INDIVIDUAL);
    var p2 = new Func("p2", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);

    assertEquals(x.freeVars(), Set.of(x));

    var a = p2.call(x, x);
    assertEquals(a.freeVars(), Set.of(x));

    a = Term.of(Tag.ALL, p2.call(x, y), x);
    assertEquals(a.freeVars(), Set.of(y));

    a = Term.of(Tag.ALL, p2.call(x, y), x, y, z);
    assertEquals(a.freeVars(), Set.of());
  }

  @Test
  public void quantify() {
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var p2 = new Func("p2", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);

    var a = Term.of(Tag.EXISTS, p2.call(x, x), x);
    assertEquals(a.quantify(), a);

    a = Term.of(Tag.EXISTS, p2.call(x, y), x);
    assertEquals(a.quantify(), Term.of(Tag.ALL, Term.of(Tag.EXISTS, p2.call(x, y), x), y));
  }

  @Test
  public void mapLeaves() {
    var a = Term.of(Tag.ADD, Term.of(Tag.MULTIPLY, Term.of(1), Term.of(2)), Term.of(3));
    var a1 =
        a.mapLeaves(
            x -> {
              if (x.tag() == Tag.INTEGER) {
                var n = x.integerValue();
                n = n.multiply(BigInteger.TEN);
                return Term.of(n);
              }
              return x;
            });
    var b = Term.of(Tag.ADD, Term.of(Tag.MULTIPLY, Term.of(10), Term.of(20)), Term.of(30));
    assertEquals(a1, b);
  }

  @Test
  public void remake() {
    var x = new Var(Type.INTEGER);
    var y = new Var(Type.INTEGER);
    var a = Term.of(Tag.ADD, x, y);
    var v = new Term[] {y, x};
    var b = Term.of(Tag.ADD, y, x);
    assertEquals(a.remake(v), b);
  }

  @Test
  public void replace() {
    var x = new Var(Type.INTEGER);
    var y = new Var(Type.INTEGER);
    var map = FMap.EMPTY;
    map = map.add(x, Term.of(10));
    map = map.add(y, Term.of(20));

    assertEquals(x.replace(map), Term.of(10));
    assertEquals(y.replace(map), Term.of(20));

    var a = Term.of(Tag.ADD, Term.of(Tag.MULTIPLY, x, y), Term.of(30));
    var b = Term.of(Tag.ADD, Term.of(Tag.MULTIPLY, Term.of(10), Term.of(20)), Term.of(30));
    assertEquals(a.replace(map), b);
  }

  @Test
  public void simplify() {
    var x = new Var(Type.INTEGER);
    var y = new Var(Type.INTEGER);
    var x1 = new Var(Type.RATIONAL);
    var y1 = new Var(Type.RATIONAL);
    Term a, b;

    a = Term.of(Tag.EQUALS, x, x);
    b = Term.TRUE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.EQUALS, x, y);
    b = Term.of(Tag.EQUALS, x, y);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.EQUALS, Term.of(5), Term.of(6));
    b = Term.FALSE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.LESS, Term.of(5), Term.of(6));
    b = Term.TRUE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.LESS, Term.of(6), Term.of(6));
    b = Term.FALSE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.LESS, Term.of(6), Term.of(5));
    //noinspection ConstantConditions
    b = Term.FALSE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.LESS_EQUALS, Term.of(5), Term.of(6));
    b = Term.TRUE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.LESS_EQUALS, Term.of(6), Term.of(6));
    //noinspection ConstantConditions
    b = Term.TRUE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.LESS_EQUALS, Term.of(6), Term.of(5));
    b = Term.FALSE;
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.NEGATE, rational(-3, 7));
    b = rational(3, 7);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.ADD, Term.of(Tag.ADD, Term.of(1), Term.of(2)), Term.of(7));
    b = Term.of(10);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.ADD, Term.of(Tag.ADD, rational(1, 10), rational(2, 10)), rational(4, 10));
    b = rational(7, 10);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.ADD, Term.of(Tag.ADD, real(1, 10), real(2, 10)), real(4, 10));
    b = real(7, 10);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.SUBTRACT, Term.of(Tag.SUBTRACT, Term.of(1), Term.of(2)), Term.of(7));
    b = Term.of(-8);
    assertEquals(a.simplify(), b);

    a =
        Term.of(
            Tag.SUBTRACT, Term.of(Tag.SUBTRACT, rational(1, 10), rational(2, 10)), rational(4, 10));
    b = rational(-5, 10);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.SUBTRACT, Term.of(Tag.SUBTRACT, real(1, 10), real(2, 10)), real(4, 10));
    b = real(-5, 10);
    assertEquals(a.simplify(), b);

    a = Term.of(Tag.MULTIPLY, Term.of(Tag.MULTIPLY, Term.of(1), Term.of(2)), Term.of(7));
    b = Term.of(14);
    assertEquals(a.simplify(), b);

    a =
        Term.of(
            Tag.MULTIPLY, Term.of(Tag.MULTIPLY, rational(1, 10), rational(2, 10)), rational(4, 10));
    b = rational(8, 1000);
    assertEquals(a.simplify(), b);

    assertSimplify(Term.of(Tag.DIVIDE, rational(1, 1), rational(-17, 1)), rational(-1, 17));

    assertSimplify(Term.of(Tag.IS_INTEGER, x), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_INTEGER, Term.of(3)), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_INTEGER, rational(3, 3)), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_INTEGER, rational(1, 3)), Term.FALSE);

    assertSimplify(Term.of(Tag.IS_RATIONAL, x), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_RATIONAL, x1), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_RATIONAL, Term.of(3)), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_RATIONAL, rational(3, 3)), Term.TRUE);
    assertSimplify(Term.of(Tag.IS_RATIONAL, rational(1, 3)), Term.TRUE);

    assertSimplify(Term.of(Tag.CEILING, rational(0, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.CEILING, rational(1, 10)), rational(1, 1));
    assertSimplify(Term.of(Tag.CEILING, rational(5, 10)), rational(1, 1));
    assertSimplify(Term.of(Tag.CEILING, rational(9, 10)), rational(1, 1));
    assertSimplify(Term.of(Tag.CEILING, rational(-1, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.CEILING, rational(-5, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.CEILING, rational(-9, 10)), rational(0, 1));

    assertSimplify(Term.of(Tag.FLOOR, rational(0, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.FLOOR, rational(1, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.FLOOR, rational(5, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.FLOOR, rational(9, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.FLOOR, rational(-1, 10)), rational(-1, 1));
    assertSimplify(Term.of(Tag.FLOOR, rational(-5, 10)), rational(-1, 1));
    assertSimplify(Term.of(Tag.FLOOR, rational(-9, 10)), rational(-1, 1));

    assertSimplify(Term.of(Tag.ROUND, rational(0, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.ROUND, rational(1, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.ROUND, rational(5, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.ROUND, rational(9, 10)), rational(1, 1));
    assertSimplify(Term.of(Tag.ROUND, rational(-1, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.ROUND, rational(-5, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.ROUND, rational(-9, 10)), rational(-1, 1));

    assertSimplify(Term.of(Tag.TRUNCATE, rational(0, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.TRUNCATE, rational(1, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.TRUNCATE, rational(5, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.TRUNCATE, rational(9, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.TRUNCATE, rational(-1, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.TRUNCATE, rational(-5, 10)), rational(0, 1));
    assertSimplify(Term.of(Tag.TRUNCATE, rational(-9, 10)), rational(0, 1));

    assertSimplify(Term.of(Tag.DIVIDE_EUCLIDEAN, Term.of(7), Term.of(3)), Term.of(2));
    assertSimplify(Term.of(Tag.DIVIDE_EUCLIDEAN, Term.of(7), Term.of(-3)), Term.of(-2));
    assertSimplify(Term.of(Tag.DIVIDE_EUCLIDEAN, Term.of(-7), Term.of(3)), Term.of(-3));
    assertSimplify(Term.of(Tag.DIVIDE_EUCLIDEAN, Term.of(-7), Term.of(-3)), Term.of(3));

    assertSimplify(Term.of(Tag.DIVIDE_FLOOR, Term.of(5), Term.of(3)), Term.of(1));
    assertSimplify(Term.of(Tag.DIVIDE_FLOOR, Term.of(5), Term.of(-3)), Term.of(-2));
    assertSimplify(Term.of(Tag.DIVIDE_FLOOR, Term.of(-5), Term.of(3)), Term.of(-2));
    assertSimplify(Term.of(Tag.DIVIDE_FLOOR, Term.of(-5), Term.of(-3)), Term.of(1));

    assertSimplify(Term.of(Tag.DIVIDE_TRUNCATE, Term.of(5), Term.of(3)), Term.of(1));
    assertSimplify(Term.of(Tag.DIVIDE_TRUNCATE, Term.of(5), Term.of(-3)), Term.of(-1));
    assertSimplify(Term.of(Tag.DIVIDE_TRUNCATE, Term.of(-5), Term.of(3)), Term.of(-1));
    assertSimplify(Term.of(Tag.DIVIDE_TRUNCATE, Term.of(-5), Term.of(-3)), Term.of(1));

    assertSimplify(Term.of(Tag.REMAINDER_EUCLIDEAN, Term.of(7), Term.of(3)), Term.of(1));
    assertSimplify(Term.of(Tag.REMAINDER_EUCLIDEAN, Term.of(7), Term.of(-3)), Term.of(1));
    assertSimplify(Term.of(Tag.REMAINDER_EUCLIDEAN, Term.of(-7), Term.of(3)), Term.of(2));
    assertSimplify(Term.of(Tag.REMAINDER_EUCLIDEAN, Term.of(-7), Term.of(-3)), Term.of(2));

    assertSimplify(Term.of(Tag.REMAINDER_FLOOR, Term.of(5), Term.of(3)), Term.of(2));
    assertSimplify(Term.of(Tag.REMAINDER_FLOOR, Term.of(5), Term.of(-3)), Term.of(-1));
    assertSimplify(Term.of(Tag.REMAINDER_FLOOR, Term.of(-5), Term.of(3)), Term.of(1));
    assertSimplify(Term.of(Tag.REMAINDER_FLOOR, Term.of(-5), Term.of(-3)), Term.of(-2));

    assertSimplify(Term.of(Tag.REMAINDER_TRUNCATE, Term.of(5), Term.of(3)), Term.of(2));
    assertSimplify(Term.of(Tag.REMAINDER_TRUNCATE, Term.of(5), Term.of(-3)), Term.of(2));
    assertSimplify(Term.of(Tag.REMAINDER_TRUNCATE, Term.of(-5), Term.of(3)), Term.of(-2));
    assertSimplify(Term.of(Tag.REMAINDER_TRUNCATE, Term.of(-5), Term.of(-3)), Term.of(-2));

    assertSimplify(Term.cast(Type.INTEGER, x), x);
    assertSimplify(Term.cast(Type.INTEGER, rational(4, 3)), Term.of(1));
    assertSimplify(Term.cast(Type.RATIONAL, Term.of(20)), rational(20, 1));
    assertSimplify(Term.cast(Type.RATIONAL, real(-99, 5)), rational(-99, 5));
    assertSimplify(Term.cast(Type.REAL, rational(-99, 5)), real(-99, 5));
  }

  private static Term rational(long num, long den) {
    return Term.of(Type.RATIONAL, BigRational.of(num, den));
  }

  private static Term real(long num, long den) {
    return Term.of(Type.REAL, BigRational.of(num, den));
  }

  private static void assertSimplify(Term a, Term b) {
    assertEquals(a.simplify(), b);
  }
}
