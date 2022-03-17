package olivine;

public final class FSet {
  private final FSet parent;
  private final Term value;
  public static final FSet EMPTY = new FSet(null, null);

  public FSet add(Term value) {
    assert value.tag() == Tag.VAR;
    return new FSet(this, value);
  }

  private FSet(FSet parent, Term value) {
    this.parent = parent;
    this.value = value;
  }

  public boolean contains(Term value) {
    for (var set = this; set != EMPTY; set = set.parent) if (set.value == value) return true;
    return false;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append('{');
    for (var set = this; set != EMPTY; set = set.parent) {
      if (set != this) sb.append(", ");
      sb.append(set.value);
    }
    sb.append('}');
    return sb.toString();
  }
}
