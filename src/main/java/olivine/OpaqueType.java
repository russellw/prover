package olivine;

public final class OpaqueType extends Type {
  private final String name;

  @Override
  public int compareTo(Type b) {
    if (b instanceof OpaqueType b1) {
      // To obtain a total order on opaque types, we compare by name. this does rely on
      // each opaque type having a unique name. That's not otherwise enforced by this class;
      // in particular, for efficiency, equality comparison just defaults to by reference.
      // So check that precondition here
      assert !(this != b && name.equals(b1.name));
      return name.compareTo(b1.name);
    }
    return super.compareTo(b);
  }

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
