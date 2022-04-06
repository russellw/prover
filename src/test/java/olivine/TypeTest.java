package olivine;

import static org.junit.Assert.*;

import org.junit.Test;

public class TypeTest {
  @Test
  public void compareTo() {
    assert Type.INTEGER.compareTo(Type.INTEGER) == 0;
    assert Type.INTEGER.compareTo(Type.REAL) != 0;

    var a = new OpaqueType("a");
    var b = new OpaqueType("b");
    assert a.compareTo(a) == 0;
    assert a.compareTo(b) != 0;
    assert a.compareTo(Type.INTEGER) != 0;
    assert Type.INTEGER.compareTo(a) != 0;

    assert Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER)
            .compareTo(Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER))
        == 0;
    assert Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER)
            .compareTo(Type.of(Kind.FUNC, Type.BOOLEAN, Type.REAL))
        != 0;
    assert Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER)
            .compareTo(Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER, Type.INTEGER))
        != 0;
    assert Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER, Type.INTEGER)
            .compareTo(Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER))
        != 0;
    assert Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER).compareTo(a) != 0;
    assert a.compareTo(Type.of(Kind.FUNC, Type.BOOLEAN, Type.INTEGER)) != 0;
  }
}
