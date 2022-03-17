package olivine;

import java.util.Arrays;
import java.util.List;

public final class Clause extends AbstractFormula {
  final Term[] literals;
  final int negativeSize;
  AbstractFormula[] from;

  public Clause(List<Term> negative, List<Term> positive) {
    // Simplify
    for (var i = 0; i < negative.size(); i++) negative.set(i, negative.get(i).simplify());
    for (var i = 0; i < positive.size(); i++) positive.set(i, positive.get(i).simplify());

    // Redundancy
    negative.removeIf(a -> a == Term.TRUE);
    positive.removeIf(a -> a == Term.FALSE);

    // Tautology?
    if (tautology(negative, positive)) {
      literals = new Term[] {Term.TRUE};
      negativeSize = 0;
      return;
    }

    // Literals
    negativeSize = negative.size();
    literals = new Term[negativeSize + positive.size()];
    for (var i = 0; i < negativeSize; i++) literals[i] = negative.get(i);
    for (var i = 0; i < positive.size(); i++) literals[negativeSize + i] = positive.get(i);
  }

  public Term[] negative() {
    return Arrays.copyOf(literals, negativeSize);
  }

  public Term[] positive() {
    return Arrays.copyOfRange(literals, negativeSize, literals.length);
  }

  public int positiveSize() {
    return literals.length - negativeSize;
  }

  public boolean isFalse() {
    return literals.length == 0;
  }

  public boolean isTrue() {
    if (literals.length == 1 && literals[0] == Term.TRUE) {
      assert negativeSize == 0;
      return true;
    }
    return false;
  }

  private static boolean tautology(List<Term> negative, List<Term> positive) {
    if (negative.contains(Term.FALSE)) return true;
    if (positive.contains(Term.TRUE)) return true;
    for (var a : negative) if (positive.contains(a)) return true;
    return false;
  }
}
