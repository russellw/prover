package olivine;

public final class DistinctObject extends Term {
  final String name;

  DistinctObject(String name) {
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
