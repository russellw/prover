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
    var x = new Var(Type.INDIVIDUAL);
    var y = new Var(Type.INDIVIDUAL);
    var negative = new ArrayList<Term>();
    var positive = new ArrayList<Term>();
    var clauses = new ArrayList<Clause>();
    SZS szs;
    final int clauseLimit = 1000000;
    final long steps = 1000;

    // true
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // false
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

    // red=red
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, red, red));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // red!=red
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, red, red));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

    // red=green
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, red, green));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

    // red!=green
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, red, green));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // a=a
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, a));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // a!=a
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, a, a));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

    // a=b
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, a, b));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // a!=b
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, a, b));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // x=x
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, x, x));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // x!=x
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, x, x));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

    // x=y
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.EQUALS, x, y));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // x!=y
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.EQUALS, x, y));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);
  }
}
