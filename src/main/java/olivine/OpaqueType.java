package olivine;

public final class OpaqueType extends Type {
  public final String name;

  public OpaqueType(String name) {
    this.name = name;
  }

  @Override
  Kind kind() {
    return Kind.OPAQUE;
  }
}
