package olivine;

public final class Inference {
  public final String rule;
  public final AbstractFormula[] from;
  public int literalIndex;
  public Equation equation;
  public int literalIndex1;
  public Equation equation1;
  public int[] position;

  public Inference(String rule, AbstractFormula... from) {
    this.rule = rule;
    this.from = from;
  }

  public String status() {
    // during proof search, clauses are derived from other clauses with the status of theorem.
    // However, the initial clauses are derived from formulas by CNF conversion.
    // Because CNF conversion may need to introduce new symbols to avoid exponential expansion,
    // the clauses so derived are strictly speaking only equisatisfiable
    return from[0] instanceof Clause ? "thm" : "esa";
  }
}
