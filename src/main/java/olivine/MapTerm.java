package olivine;

public final class MapTerm {
  private final MapTerm parent;
  private final Term key;
  private final Term value;
  public static final MapTerm EMPTY = new MapTerm(null, null, null);

  public MapTerm add(Term key, Term value) {
    assert key.size() == 0;
    return new MapTerm(this, key, value);
  }

  private MapTerm(MapTerm parent, Term key, Term value) {
    this.parent = parent;
    this.key = key;
    this.value = value;
  }

  public Term get(Term key) {
    for (var map = this; map != EMPTY; map = map.parent) if (map.key == key) return map.value;
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
