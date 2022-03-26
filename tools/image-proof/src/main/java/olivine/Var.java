package olivine;

public final class Var extends Term {
  private final Type type;

  public Var(Type type) {
    this.type = type;
  }

  @Override
  public boolean contains(Var b) {
    return this == b;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("%%%x", hashCode());
  }

  @Override
  public Tag tag() {
    return Tag.VAR;
  }
}
