package olivine;

public abstract class Global extends Term {
  String name;

  public Global(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
