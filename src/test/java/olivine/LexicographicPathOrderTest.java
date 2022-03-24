package olivine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;

public class LexicographicPathOrderTest {
  LexicographicPathOrder order;

  @Test
  public void greater() {
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

    // order can depend on the contents of the initial clauses. in particular,
    // it can reasonably expect that all functions and global variables will be shown up front
    positive.add(Term.of(Tag.EQUALS, red, green));
    positive.add(Term.of(Tag.EQUALS, a, b));
    clauses.add(new Clause(negative, positive));
    order = new LexicographicPathOrder(clauses);

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
  }

  private void checkOrdered(Term a, Term b) {
    assertTrue(order.greater(a, b) || order.greater(b, a));
  }

  private void checkUnordered(Term a, Term b) {
    assertFalse(order.greater(a, b));
    assertFalse(order.greater(b, a));
  }
}
