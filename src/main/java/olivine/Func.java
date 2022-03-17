package olivine;

public final class Func extends Term {
  final String name;
  final Type returnType;
  final Type[] params;

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
  public Tag tag() {
    return Tag.GLOBAL_VAR;
  }
}
