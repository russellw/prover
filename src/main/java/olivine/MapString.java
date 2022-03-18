package olivine;

public final class MapString {
  private final MapString parent;
  private final String key;
  private final Term value;
  public static final MapString EMPTY = new MapString(null, null, null);

  public MapString add(String key, Term value) {
    return new MapString(this, key, value);
  }

  private MapString(MapString parent, String key, Term value) {
    this.parent = parent;
    this.key = key;
    this.value = value;
  }

  public Term get(String key) {
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
