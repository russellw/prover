package olivine;

public final class DistinctObject extends Term {
  private final String name;

  @Override
  public String toString() {
    return name;
  }

  DistinctObject(String name) {
    assert name != null;
    this.name = name;
  }

  @Override
  public Type type() {
    return Type.INDIVIDUAL;
  }

  @Override
  public Tag tag() {
    return Tag.DISTINCT_OBJECT;
  }
}
