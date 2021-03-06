package olivine;

public final class Var extends Term {
  private final Type type;

  public Var(Type type) {
    this.type = type;
  }

  @Override
  public FMap unify(FMap map, Term b) {
    assert map != null;
    if (this == b) return map;
    if (!type.equals(b.type())) return null;

    var a1 = map.get(this);
    if (a1 != null) return a1.unify(map, b);

    var b1 = map.get(b);
    if (b1 != null) return unify(map, b1);

    if (b.contains(map, this)) return null;
    return map.add(this, b);
  }

  @Override
  public boolean contains(FMap map, Var b) {
    if (this == b) return true;
    var a = map.get(this);
    return a != null && a.contains(map, b);
  }

  @Override
  public FMap match(FMap map, Term b) {
    assert map != null;
    if (this == b) return map;
    if (!type.equals(b.type())) return null;

    var a = map.get(this);
    if (a != null) return a.equals(b) ? map : null;
    return map.add(this, b);
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
