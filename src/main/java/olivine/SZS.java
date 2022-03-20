package olivine;

// the full SZS ontology is much larger, but Olivine only needs to distinguish a few cases
public enum SZS {
  Satisfiable,
  Unsatisfiable,
  Timeout,
  GaveUp,
  ResourceOut;

  public String string(boolean conjecture) {
    if (conjecture)
      switch (this) {
        case Satisfiable -> {
          return "CounterSatisfiable";
        }
        case Unsatisfiable -> {
          return "Theorem";
        }
      }
    return toString();
  }
}
