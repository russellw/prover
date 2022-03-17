package olivine;

public final class FMap {
  private final FMap parent;
  private final Term key, value;
  public static final FMap EMPTY = new FMap(null, null, null);

  public FMap add(Term key, Term value) {
    return new FMap(this, key, value);
  }

  private FMap(FMap parent, Term key, Term value) {
    this.parent = parent;
    this.key = key;
    this.value = value;
  }

  public Term get(Term key) {
    for (var m = this; m != EMPTY; m = m.parent) if (m.key.equals(key)) return m.value;
    return null;
  }
}
