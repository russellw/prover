package olivine;

public final class Formula extends AbstractFormula {
  final String name;
  final boolean negatedConjecture;
  private final Term term;
  final String file;

  public Formula(String name, boolean negatedConjecture, Term term, String file) {
    this.name = name;
    this.negatedConjecture = negatedConjecture;
    this.term = term;
    this.file = file;
  }

  @Override
  Term term() {
    return term;
  }
}
