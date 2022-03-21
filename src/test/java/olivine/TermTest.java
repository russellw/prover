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
}
