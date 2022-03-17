package olivine;

import static org.junit.Assert.*;

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
}
