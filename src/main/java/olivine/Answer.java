package olivine;

public final class Answer {
  public final SZS szs;
  public final Clause proof;

  public Answer(SZS szs) {
    this.szs = szs;
    this.proof = null;
  }

  public Answer(SZS szs, Clause proof) {
    this.szs = szs;
    this.proof = proof;
  }
}
