package olivine;

public final class OpaqueType extends Type {
  @Override
  Kind kind() {
    return Kind.OPAQUE;
  }
}
