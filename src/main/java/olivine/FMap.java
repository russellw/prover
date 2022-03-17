package olivine;

public final class FMap {
  private final FMap parent;
  private final Term key, value;
  public static final FMap EMPTY = new FMap(null, null, null);

  public FMap add(Term key, Term value) {
    assert key.size() == 0;
    return new FMap(this, key, value);
  }

  private FMap(FMap parent, Term key, Term value) {
    this.parent = parent;
    this.key = key;
    this.value = value;
  }

  public Term get(Term key) {
    for (var map = this; map != EMPTY; map = map.parent) if (map.key.equals(key)) return map.value;
    return null;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append('{');
    for (var map = this; map != EMPTY; map = map.parent) {
      if (map != this) sb.append(", ");
      sb.append(map.key);
      sb.append(':');
      sb.append(map.value);
    }
    sb.append('}');
    return sb.toString();
  }
}
