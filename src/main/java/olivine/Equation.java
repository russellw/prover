package olivine;

public final class Equation {
  public final Term left, right;

  public Equation(Term left, Term right) {
    assert equatable(left, right);
    this.left = left;
    this.right = right;
  }

  public static boolean equatable(Term a, Term b) {
    if (!a.type().equals(b.type())) return false;
    return b.type() != Type.BOOLEAN || b == Term.TRUE;
  }

  public Equation(Term a) {
    assert a.type() == Type.BOOLEAN;
    if (a.tag() == Tag.EQUALS) {
      left = a.get(0);
      right = a.get(1);
      return;
    }
    left = a;
    right = Term.TRUE;
  }

  public Term term() {
    if (right == Term.TRUE) return left;
    return Term.of(Tag.EQUALS, left, right);
  }

  @Override
  public String toString() {
    return left.toString() + '=' + right;
  }
}
