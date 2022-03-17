package olivine;

public final class Clause extends AbstractFormula {
  final Term[] literals;
  final int negCount;
  AbstractFormula[] from;

  public Clause(Term[] literals, int negCount) {
    this.literals = literals;
    this.negCount = negCount;
  }
}
