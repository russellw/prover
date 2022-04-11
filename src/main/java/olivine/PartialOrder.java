package olivine;

public enum PartialOrder {
  LESS,
  EQUALS,
  GREATER,
  UNORDERED;

  public static PartialOrder of(int c) {
    if (c < 0) return LESS;
    if (c == 0) return EQUALS;
    return GREATER;
  }

  public PartialOrder flip() {
    return switch (this) {
      case GREATER -> LESS;
      case LESS -> GREATER;
      default -> this;
    };
  }
}
