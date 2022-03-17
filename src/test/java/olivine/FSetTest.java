package olivine;

import static org.junit.Assert.*;

import org.junit.Test;

public class FSetTest {
  @Test
  public void contains() {
    var x = new Var(Type.INTEGER);
    var y = new Var(Type.INTEGER);
    var z = new Var(Type.INTEGER);

    var set = FSet.EMPTY;
    assert !set.contains(x);
    assert !set.contains(y);
    assert !set.contains(z);

    set = set.add(x);
    assert set.contains(x);
    assert !set.contains(y);
    assert !set.contains(z);

    set = set.add(y);
    assert set.contains(x);
    assert set.contains(y);
    assert !set.contains(z);

    assertNotEquals(set, FSet.EMPTY);
  }
}
