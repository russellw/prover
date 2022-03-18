package olivine;

public final class OpaqueType extends Type {
  private final String name;

  public OpaqueType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  Kind kind() {
    return Kind.OPAQUE;
  }
}
