package olivine;

// the full SZS ontology is much larger, but Olivine only needs to distinguish a few cases
public enum SZS {
  Satisfiable,
  Unsatisfiable,
  Timeout,
  GaveUp,
  ResourceOut,
}
