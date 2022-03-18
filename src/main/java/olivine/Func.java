package olivine;

public final class Func extends Term {
  final String name;
  Type returnType;
  Type[] params;

  public Func(String name) {
    this.name = name;
  }

  @Override
  public void setType(Type type) {
    if (returnType == null) {
      returnType = type.get(0);
      params = new Type[type.size() - 1];
      for (var i = 0; i < params.length; i++) params[i] = type.get(1 + i);
    }
    check(type);
  }

  public Func(String name, Type returnType, Type... params) {
    this.name = name;
    this.returnType = returnType;
    this.params = params;
  }

  @Override
  public Type type() {
    var v = new Type[1 + params.length];
    v[0] = returnType;
    System.arraycopy(params, 0, v, 1, params.length);
    return Type.of(Kind.FUNC, v);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Tag tag() {
    return Tag.FUNC;
  }
}
