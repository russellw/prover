package olivine;

public final class GlobalVar extends Term {
  final String name;
  private Type type;

  @Override
  public void defaultType(Type type) {
    if (this.type == null) setType(type);
  }

  @Override
  public void setType(Type type) {
    if (this.type == null) this.type = type;
    check(type);
  }

  public GlobalVar(String name) {
    this.name = name;
  }

  public GlobalVar(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Tag tag() {
    return Tag.GLOBAL_VAR;
  }
}
