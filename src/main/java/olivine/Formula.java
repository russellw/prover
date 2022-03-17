package olivine;

public final class Formula extends AbstractFormula {
  final String name;
  final Term term;
  final String file;

  public Formula(String name, Term term, String file) {
    this.name = name;
    this.term = term;
    this.file = file;
  }
}
