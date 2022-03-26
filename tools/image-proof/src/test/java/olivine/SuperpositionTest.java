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
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

    // p(red)
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, red));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // !p(red)
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, red));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // p(a)
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, a));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // !p(a)
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, a));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // p(x)
    clauses.clear();
    negative.clear();
    positive.clear();
    positive.add(Term.of(Tag.CALL, p1, x));
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

    // !p(x)
    clauses.clear();
    negative.clear();
    negative.add(Term.of(Tag.CALL, p1, x));
    positive.clear();
    clauses.add(new Clause(negative, positive));
    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Satisfiable);

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

    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

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

    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

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

    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

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

    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);

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

    szs = new Superposition(clauses, clauseLimit, steps).answer.szs;
    assertEquals(szs, SZS.Unsatisfiable);
  }
}
