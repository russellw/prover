package olivine;

public final class Inference {
  public final String rule;
  public final AbstractFormula from;
  public Clause from1;

  public int literalIndex = -1;
  public boolean reversed;

  public int literalIndex1 = -1;
  public boolean reversed1;
  public int[] position;

  public Inference(String rule, AbstractFormula from) {
    this.rule = rule;
    this.from = from;
  }

  public String status() {
    // during proof search, clauses are derived from other clauses with the status of theorem.
    // However, the initial clauses are derived from formulas by CNF conversion.
    // Because CNF conversion may need to introduce new symbols to avoid exponential expansion,
    // the clauses so derived are strictly speaking only equisatisfiable
    return from instanceof Clause ? "thm" : "esa";
  }
}
