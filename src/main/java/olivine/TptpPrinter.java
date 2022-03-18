package olivine;

public final class TptpPrinter {
  public void print(Type type) {
    switch (type.kind()) {
      case BOOLEAN -> System.out.print("$o");
      case INDIVIDUAL -> System.out.print("$i");
      case INTEGER -> System.out.print("$int");
      case RATIONAL -> System.out.print("$rat");
      case REAL -> System.out.print("$real");
      case OPAQUE -> System.out.print(type);
      case FUNC -> {
        var n = type.size();
        if (n > 2) System.out.print('(');
        for (var i = 1; i < n; i++) {
          if (i > 1) System.out.print(" * ");
          print(type.get(i));
        }
        if (n > 2) System.out.print(')');
        System.out.print(" > ");
        print(type.get(0));
      }
    }
  }

  private void definedAtomicTerm(String s, Term a) {
    System.out.printf("$%s(", s);
    var n = a.size();
    for (var i = 0; i < n; i++) {
      if (i > 0) System.out.print(',');
      print(a.get(i));
    }
    System.out.print(')');
  }

  private void print(Term parent, Term a) {
    switch (a.tag()) {
      case TRUE -> System.out.print("$true");
      case FALSE -> System.out.print("$false");
      case INTEGER, RATIONAL -> System.out.print(a);
      case GLOBAL_VAR, FUNC -> System.out.print(Etc.quote('\'', a.toString()));
      case NEGATE -> definedAtomicTerm("uminus", a);
      case FLOOR -> definedAtomicTerm("floor", a);
      case CEILING -> definedAtomicTerm("ceiling", a);
      case TRUNCATE -> definedAtomicTerm("truncate", a);
      case ROUND -> definedAtomicTerm("round", a);
      case IS_INTEGER -> definedAtomicTerm("is_int", a);
      case IS_RATIONAL -> definedAtomicTerm("is_rat", a);
      case LESS -> definedAtomicTerm("less", a);
      case LESS_EQUALS -> definedAtomicTerm("lesseq", a);
      case ADD -> definedAtomicTerm("sum", a);
      case SUBTRACT -> definedAtomicTerm("difference", a);
      case MULTIPLY -> definedAtomicTerm("product", a);
      case DIVIDE -> definedAtomicTerm("quotient", a);
      case DIVIDE_EUCLIDEAN -> definedAtomicTerm("quotient_e", a);
      case DIVIDE_FLOOR -> definedAtomicTerm("quotient_f", a);
      case DIVIDE_TRUNCATE -> definedAtomicTerm("quotient_t", a);
      case REMAINDER_EUCLIDEAN -> definedAtomicTerm("remainder_e", a);
      case REMAINDER_FLOOR -> definedAtomicTerm("remainder_f", a);
      case REMAINDER_TRUNCATE -> definedAtomicTerm("remainder_t", a);
      case DISTINCT_OBJECT -> System.out.print(Etc.quote('"', a.toString()));
    }
  }

  public void print(Term a) {
    print(null, a);
  }
}
