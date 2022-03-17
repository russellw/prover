package olivine;

public final class GlobalVar extends Term {
  final String name;
  private final Type type;

  public GlobalVar(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public Tag tag() {
    return Tag.GLOBAL_VAR;
  }
}
