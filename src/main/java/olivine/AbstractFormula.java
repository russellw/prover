package olivine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractFormula {
  public List<AbstractFormula> proof() {
    var proof = new ArrayList<AbstractFormula>();
    getProof(new HashSet<>(), proof);
    return proof;
  }

  protected void getProof(Set<AbstractFormula> visited, List<AbstractFormula> proof) {
    if (!visited.add(this)) return;
    proof.add(this);
  }
}
