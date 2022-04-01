package olivine;

import java.util.function.Consumer;

public abstract class Global extends Term {
  String name;

  public Global(String name) {
    this.name = name;
  }

  @Override
  public void walkGlobals(Consumer<Global> f) {
    f.accept(this);
  }

  @Override
  public String toString() {
    return name;
  }
}
