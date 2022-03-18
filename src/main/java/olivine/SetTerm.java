package olivine;

public final class SetTerm {
  private final SetTerm parent;
  private final Term value;
  public static final SetTerm EMPTY = new SetTerm(null, null);

  public SetTerm add(Term value) {
    assert value.tag() == Tag.VAR;
    return new SetTerm(this, value);
  }

  private SetTerm(SetTerm parent, Term value) {
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
