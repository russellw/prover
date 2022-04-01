package olivine;

public final class GlobalVar extends Global {
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
    super(name);
  }

  public GlobalVar(String name, Type type) {
    super(name);
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
