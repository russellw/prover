package olivine;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class CNFTest {
  private List<Clause> convert(Term a) {
    var formula = new Formula("", false, a, null);
    var cnf = new CNF();
    cnf.add(formula);
    return cnf.clauses;
  }

  @Test
  public void add() {
    List<Clause> clauses;
    Term a;

    // false
    a = Term.FALSE;
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertTrue(clauses.get(0).isFalse());

    // true
    a = Term.TRUE;
    clauses = convert(a);
    assertEquals(clauses.size(), 0);

    // !false
    a = Term.of(Tag.NOT, Term.FALSE);
    clauses = convert(a);
    assertEquals(clauses.size(), 0);

    // !true
    a = Term.of(Tag.NOT, Term.TRUE);
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertTrue(clauses.get(0).isFalse());

    // false & false
    a = Term.of(Tag.AND, Term.FALSE, Term.FALSE);
    clauses = convert(a);
    assertEquals(clauses.size(), 2);
    assertTrue(clauses.get(0).isFalse());
    assertTrue(clauses.get(1).isFalse());

    // false & true
    a = Term.of(Tag.AND, Term.FALSE, Term.TRUE);
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertTrue(clauses.get(0).isFalse());

    // true & false
    a = Term.of(Tag.AND, Term.TRUE, Term.FALSE);
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertTrue(clauses.get(0).isFalse());

    // true & true
    a = Term.of(Tag.AND, Term.TRUE, Term.TRUE);
    clauses = convert(a);
    assertEquals(clauses.size(), 0);

    // false | false
    a = Term.of(Tag.OR, Term.FALSE, Term.FALSE);
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertTrue(clauses.get(0).isFalse());

    // false | true
    a = Term.of(Tag.OR, Term.FALSE, Term.TRUE);
    clauses = convert(a);
    assertEquals(clauses.size(), 0);

    // true | false
    a = Term.of(Tag.OR, Term.TRUE, Term.FALSE);
    clauses = convert(a);
    assertEquals(clauses.size(), 0);

    // true | true
    a = Term.of(Tag.OR, Term.TRUE, Term.TRUE);
    clauses = convert(a);
    assertEquals(clauses.size(), 0);

    // p & q
    var p = new GlobalVar("p", Type.BOOLEAN);
    var q = new GlobalVar("q", Type.BOOLEAN);
    a = Term.of(Tag.AND, p, q);
    clauses = convert(a);
    assertEquals(clauses.size(), 2);
    assertEql(clauses.get(0), p);
    assertEql(clauses.get(1), q);

    // p | q
    a = Term.of(Tag.OR, p, q);
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertEql(clauses.get(0), p, q);

    // !(p & q)
    a = Term.of(Tag.NOT, Term.of(Tag.AND, p, q));
    clauses = convert(a);
    assertEquals(clauses.size(), 1);
    assertEql(clauses.get(0), Term.of(Tag.NOT, p), Term.of(Tag.NOT, q));

    // !(p | q)
    a = Term.of(Tag.NOT, Term.of(Tag.OR, p, q));
    clauses = convert(a);
    assertEquals(clauses.size(), 2);
    assertEql(clauses.get(0), Term.of(Tag.NOT, p));
    assertEql(clauses.get(1), Term.of(Tag.NOT, q));
  }

  private static void assertEql(Clause c, Term... q) {
    var negativeIndex = 0;
    var positiveIndex = 0;
    for (var a : q) {
      if (a.tag() == Tag.NOT) assertEquals(c.negative()[negativeIndex++], a.get(0));
      else assertEquals(c.positive()[positiveIndex++], a);
    }
  }
}
