package olivine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class KnuthBendixOrderTest {
  private static final int ITERATIONS = 10000;
  private final List<Func> funcs = new ArrayList<>();
  private final List<GlobalVar> globalVars = new ArrayList<>();
  private final List<Var> vars = new ArrayList<>();
  private final Random random = new Random(0);
  private KnuthBendixOrder order;

  private Term randomIndividualTerm(int depth) {
    if (depth == 0 || random.nextInt(100) < 40)
      if (vars.isEmpty() || random.nextInt(100) < 30)
        return globalVars.get(random.nextInt(globalVars.size()));
      else return vars.get(random.nextInt(vars.size()));

    var f = funcs.get(random.nextInt(funcs.size()));
    var args = new Term[f.params.length];
    for (var i = 0; i < args.length; i++) args[i] = randomIndividualTerm(depth - 1);
    return f.call(args);
  }

  private void makeOrder() {
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    for (var f : funcs) {
      var args = new Term[f.params.length];
      for (var i = 0; i < args.length; i++) args[i] = vars.get(0);
      positive.add(Term.of(Tag.EQUALS, f.call(args), vars.get(0)));
    }
    for (var a : globalVars) positive.add(Term.of(Tag.EQUALS, a, vars.get(0)));
    var clauses = new ArrayList<Clause>();
    clauses.add(new Clause(negative, positive));
    order = new KnuthBendixOrder(clauses);
  }

  private void makeRandomOrder() {
    for (var i = 0; i < 4; i++)
      funcs.add(
          new Func(String.format("f%d", i), Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL));
    for (var i = 0; i < 4; i++)
      globalVars.add(new GlobalVar(String.format("a%d", i), Type.INDIVIDUAL));
    for (var i = 0; i < 4; i++) vars.add(new Var(Type.INDIVIDUAL));
    makeOrder();
  }

  private boolean greater(Term a, Term b) {
    return order.compare(a, b) == KnuthBendixOrder.GREATER;
  }

  @Test
  public void randomTest() {
    makeRandomOrder();
    for (var i = 0; i < ITERATIONS; i++) {
      var a = randomIndividualTerm(4);
      var b = randomIndividualTerm(4);
      assertFalse(greater(a, b) && a.equals(b));
      assertFalse(greater(a, b) && greater(b, a));
    }
  }

  @Test
  public void greater() {
    var red = new DistinctObject("red");
    var green = new DistinctObject("green");
    var a = new GlobalVar("a", Type.INDIVIDUAL);
    var b = new GlobalVar("b", Type.INDIVIDUAL);
    var p1 = new Func("p1", Type.BOOLEAN, Type.INDIVIDUAL);
    var q1 = new Func("q1", Type.BOOLEAN, Type.INDIVIDUAL);
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    var clauses = new ArrayList<Clause>();

    // order can depend on the contents of the initial clauses. in particular,
    // it can reasonably expect that all functions and global variables will be shown up front
    positive.add(Term.of(Tag.EQUALS, red, green));
    positive.add(Term.of(Tag.EQUALS, a, b));
    positive.add(Term.of(Tag.CALL, p1, red));
    positive.add(Term.of(Tag.CALL, q1, red));
    clauses.add(new Clause(negative, positive));
    order = new KnuthBendixOrder(clauses);

    checkUnordered(x, y);
    checkUnordered(Term.of(1), Term.of(1));
    checkOrdered(Term.of(1), Term.of(2));
    checkOrdered(red, green);
    checkOrdered(a, b);

    checkUnordered(
        Term.of(
            Tag.ADD,
            Term.of(Type.RATIONAL, BigRational.of(1, 3)),
            Term.of(Type.RATIONAL, BigRational.of(1, 3))),
        Term.of(
            Tag.ADD,
            Term.of(Type.RATIONAL, BigRational.of(1, 3)),
            Term.of(Type.RATIONAL, BigRational.of(1, 3))));
    checkOrdered(
        Term.of(
            Tag.ADD,
            Term.of(Type.RATIONAL, BigRational.of(1, 3)),
            Term.of(Type.RATIONAL, BigRational.of(1, 3))),
        Term.of(
            Tag.ADD,
            Term.of(Type.RATIONAL, BigRational.of(1, 3)),
            Term.of(Type.RATIONAL, BigRational.of(2, 3))));
    checkOrdered(
        Term.of(
            Tag.ADD,
            Term.of(Type.RATIONAL, BigRational.of(1, 3)),
            Term.of(Type.RATIONAL, BigRational.of(1, 3))),
        Term.of(
            Tag.SUBTRACT,
            Term.of(Type.RATIONAL, BigRational.of(1, 3)),
            Term.of(Type.RATIONAL, BigRational.of(1, 3))));

    checkUnordered(Term.of(Tag.CALL, p1, red), Term.of(Tag.CALL, p1, red));
    checkOrdered(Term.of(Tag.CALL, p1, red), Term.of(Tag.CALL, p1, green));
    checkOrdered(Term.of(Tag.CALL, p1, red), Term.of(Tag.CALL, q1, red));

    checkUnordered(Term.of(Tag.CALL, p1, x), Term.of(Tag.CALL, p1, x));
    checkUnordered(Term.of(Tag.CALL, p1, x), Term.of(Tag.CALL, p1, y));
    checkOrdered(Term.of(Tag.CALL, p1, x), Term.of(Tag.CALL, q1, x));
  }

  private void checkOrdered(Term a, Term b) {
    assertTrue(greater(a, b) || greater(b, a));
  }

  private void checkUnordered(Term a, Term b) {
    assertFalse(greater(a, b));
    assertFalse(greater(b, a));
  }

  private void checkEqual(Term a, Term b) {
    assertEquals(order.compare(a, b), KnuthBendixOrder.EQUALS);
  }

  private static boolean containsSubterm(Term a, Term b) {
    if (a.equals(b)) return true;
    for (var ai : a) if (containsSubterm(ai, b)) return true;
    return false;
  }

  @Test
  public void totalOnGroundTerms() {
    makeRandomOrder();
    vars.clear();
    for (var i = 0; i < ITERATIONS; i++) {
      var a = randomIndividualTerm(4);
      var b = randomIndividualTerm(4);
      if (!a.equals(b)) checkOrdered(a, b);
    }
  }

  @Test
  public void containsSubtermRelation() {
    makeRandomOrder();
    for (var i = 0; i < ITERATIONS; i++) {
      var a = randomIndividualTerm(4);
      var b = randomIndividualTerm(4);
      if (a.equals(b)) continue;
      if (containsSubterm(a, b)) assertTrue(greater(a, b));
      if (containsSubterm(b, a)) assertTrue(greater(b, a));
    }
  }

  @Test
  public void cast() {
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    var a = new GlobalVar("a", Type.REAL);
    var b = new GlobalVar("b", Type.REAL);
    positive.add(Term.of(Tag.EQUALS, a, b));
    var clauses = new ArrayList<Clause>();
    clauses.add(new Clause(negative, positive));
    order = new KnuthBendixOrder(clauses);

    checkOrdered(Term.cast(Type.REAL, a), Term.cast(Type.RATIONAL, a));
    checkOrdered(Term.cast(Type.REAL, a), Term.cast(Type.REAL, b));
    checkEqual(Term.cast(Type.REAL, a), Term.cast(Type.REAL, a));
  }
}
