package olivine;

import static org.junit.Assert.*;

import org.junit.Test;

public class UnificationTest {
  @Test
  public void match() {
    // Subset of unify.
    // Gives different results in several cases;
    // in particular, has no notion of an occurs check.
    // Assumes the inputs have disjoint variables
    var a = new GlobalVar("a", Type.INDIVIDUAL);
    var b = new GlobalVar("b", Type.INDIVIDUAL);
    var f1 = new Func("f1", Type.INDIVIDUAL, Type.INDIVIDUAL);
    var f2 = new Func("f2", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);
    var g1 = new Func("g1", Type.INDIVIDUAL, Type.INDIVIDUAL);
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var z = new Var(Type.INDIVIDUAL);
    FMap map;

    // Succeeds. (tautology)
    map = a.match(FMap.EMPTY, a);
    assertNotNull(map);
    assertEquals(map, FMap.EMPTY);

    // a and b do not match
    map = a.match(FMap.EMPTY, b);
    assertNull(map);

    // Succeeds. (tautology)
    map = x.match(FMap.EMPTY, x);
    assertNotNull(map);
    assertEquals(map, FMap.EMPTY);

    // a and x do not match
    map = a.match(FMap.EMPTY, x);
    assertNull(map);

    // x and y are aliased
    map = x.match(FMap.EMPTY, y);
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), y);

    // Function and constant symbols match, x is unified with the constant b
    map = f2.call(a, x).match(FMap.EMPTY, f2.call(a, b));
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), b);

    // f and g do not match
    map = f1.call(a).match(FMap.EMPTY, g1.call(a));
    assertNull(map);

    // x and y are aliased
    map = f1.call(x).match(FMap.EMPTY, f1.call(y));
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), y);

    // f and g do not match
    map = f1.call(x).match(FMap.EMPTY, g1.call(y));
    assertNull(map);

    // Fails. The f function symbols have different arity
    map = f1.call(x).match(FMap.EMPTY, f2.call(y, z));
    assertNull(map);

    // g(x) and y do not match
    map = f1.call(g1.call(x)).match(FMap.EMPTY, f1.call(y));
    assertNull(map);

    // g(x) and y do not match
    map = f2.call(g1.call(x), x).match(FMap.EMPTY, f2.call(y, a));
    assertNull(map);
  }

  @Test
  public void unify() {
    // https://en.wikipedia.org/wiki/Unification_(computer_science)#Examples_of_syntactic_unification_of_first-order_terms
    var a = new GlobalVar("a", Type.INDIVIDUAL);
    var b = new GlobalVar("b", Type.INDIVIDUAL);
    var f1 = new Func("f1", Type.INDIVIDUAL, Type.INDIVIDUAL);
    var f2 = new Func("f2", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);
    var g1 = new Func("g1", Type.INDIVIDUAL, Type.INDIVIDUAL);
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var z = new Var(Type.INDIVIDUAL);
    FMap map;

    // Succeeds. (tautology)
    map = Unification.unify(FMap.EMPTY, a, a);
    assertNotNull(map);
    assertEquals(map, FMap.EMPTY);

    // a and b do not match
    map = Unification.unify(FMap.EMPTY, a, b);
    assertNull(map);

    // Succeeds. (tautology)
    map = Unification.unify(FMap.EMPTY, x, x);
    assertNotNull(map);
    assertEquals(map, FMap.EMPTY);

    // x is unified with the constant a
    map = Unification.unify(FMap.EMPTY, a, x);
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), a);

    // x and y are aliased
    map = Unification.unify(FMap.EMPTY, x, y);
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), y.replace(map));

    // Function and constant symbols match, x is unified with the constant b
    map = Unification.unify(FMap.EMPTY, f2.call(a, x), f2.call(a, b));
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), b);

    // f and g1 do not match
    map = Unification.unify(FMap.EMPTY, f1.call(a), g1.call(a));
    assertNull(map);

    // x and y are aliased
    map = Unification.unify(FMap.EMPTY, f1.call(x), f1.call(y));
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), y.replace(map));

    // f and g1 do not match
    map = Unification.unify(FMap.EMPTY, f1.call(x), g1.call(y));
    assertNull(map);

    // Fails. The f function symbols have different arity
    map = Unification.unify(FMap.EMPTY, f1.call(x), f2.call(y, z));
    assertNull(map);

    // Unifies y with the term g1(x)
    map = Unification.unify(FMap.EMPTY, f1.call(g1.call(x)), f1.call(y));
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(y.replace(map), g1.call(x));

    // Unifies x with constant a, and y with the term g1(a)
    map = Unification.unify(FMap.EMPTY, f2.call(g1.call(x), x), f2.call(y, a));
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), a);
    assertEquals(y.replace(map), g1.call(a));

    // Returns false in first-order logic and many modern Prolog dialects (enforced by the occurs
    // check).
    map = Unification.unify(FMap.EMPTY, x, f1.call(x));
    assertNull(map);

    // Both x and y are unified with the constant a
    map = Unification.unify(FMap.EMPTY, x, y);
    map = Unification.unify(map, y, a);
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), a);
    assertEquals(y.replace(map), a);

    // As above (order of equations in set doesn't matter)
    map = Unification.unify(FMap.EMPTY, a, y);
    map = Unification.unify(map, x, y);
    assertNotNull(map);
    assertNotEquals(map, FMap.EMPTY);
    assertEquals(x.replace(map), a);
    assertEquals(y.replace(map), a);

    // Fails. a and b do not match, so x can't be unified with both
    map = Unification.unify(FMap.EMPTY, x, a);
    assertNotNull(map);
    map = Unification.unify(map, b, x);
    assertNull(map);
  }
}
