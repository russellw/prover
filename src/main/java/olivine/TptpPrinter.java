package olivine;

import java.util.HashMap;
import java.util.Map;

public final class TptpPrinter {
  private final Map<Term, Integer> vars = new HashMap<>();
  private final Map<AbstractFormula, Integer> formulas = new HashMap<>();

  private static boolean isWord(String s) {
    if (s.isEmpty()) return false;
    if (!Etc.isLower(s.charAt(0))) return false;
    for (var i = 0; i < s.length(); i++) if (!Etc.isIdPart(s.charAt(i))) return false;
    return true;
  }

  private static void quote(char q, String s) {
    System.out.print(q);
    for (var i = 0; i < s.length(); i++) {
      var c = s.charAt(i);
      if (c == q || c == '\\') System.out.print('\\');
      System.out.print(c);
    }
    System.out.print(q);
  }

  private static void maybeQuote(String s) {
    if (isWord(s)) {
      System.out.print(s);
      return;
    }
    quote('\'', s);
  }

  // types
  public static void print(Type type) {
    switch (type.kind()) {
      case BOOLEAN -> System.out.print("$o");
      case INDIVIDUAL -> System.out.print("$i");
      case INTEGER -> System.out.print("$int");
      case RATIONAL -> System.out.print("$rat");
      case REAL -> System.out.print("$real");
      case OPAQUE -> maybeQuote(type.toString());
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

  // terms
  private void definedAtomicTerm(String op, Term a) {
    System.out.print(op);
    System.out.print('(');
    var n = a.size();
    for (var i = 0; i < n; i++) {
      if (i > 0) System.out.print(',');
      print(a.get(i));
    }
    System.out.print(')');
  }

  private void quant(char op, Term a) {
    System.out.print(op);
    System.out.print('[');
    var n = a.size();
    for (var i = 1; i < n; i++) {
      if (i > 1) System.out.print(',');
      var x = a.get(i);
      print(x);
      if (x.type() != Type.INDIVIDUAL) {
        System.out.print(':');
        print(x.type());
      }
    }
    System.out.print("]:");
    print(a, a.get(0));
  }

  private static boolean needParens(Term parent, Term a) {
    if (parent == null) return false;
    return switch (parent.tag()) {
      case ALL, AND, EQV, EXISTS, NOT, OR -> switch (a.tag()) {
        case AND, EQV, OR -> true;
        default -> false;
      };
      default -> false;
    };
  }

  private void infixFormula(Term parent, String op, Term a) {
    var parens = needParens(parent, a);
    if (parens) System.out.print('(');
    var n = a.size();
    for (var i = 0; i < n; i++) {
      if (i > 0) System.out.print(op);
      print(a.get(i));
    }
    if (parens) System.out.print(')');
  }

  private void print(Term parent, Term a) {
    switch (a.tag()) {
      case NOT -> {
        System.out.print('~');
        print(a, a.get(0));
      }
      case AND -> infixFormula(parent, " & ", a);
      case OR -> infixFormula(parent, " | ", a);
      case EQV -> infixFormula(parent, " <=> ", a);
      case EQUALS -> {
        print(a.get(0));
        System.out.print('=');
        print(a.get(1));
      }
      case CAST -> {
        switch (a.type().kind()) {
          case INTEGER -> definedAtomicTerm("$to_int", a);
          case RATIONAL -> definedAtomicTerm("$to_rat", a);
          case REAL -> definedAtomicTerm("$to_real", a);
          default -> throw new IllegalArgumentException(a.toString());
        }
      }
      case ALL -> quant('!', a);
      case EXISTS -> quant('?', a);
      case TRUE -> System.out.print("$true");
      case FALSE -> System.out.print("$false");
      case INTEGER -> System.out.print(a);
      case RATIONAL -> {
        if (a.type() == Type.REAL) {
          System.out.print("$to_real(");
          System.out.print(a);
          System.out.print(')');
        } else System.out.print(a);
      }
      case GLOBAL_VAR, FUNC -> maybeQuote(a.toString());
      case NEGATE -> definedAtomicTerm("$uminus", a);
      case FLOOR -> definedAtomicTerm("$floor", a);
      case CEILING -> definedAtomicTerm("$ceiling", a);
      case TRUNCATE -> definedAtomicTerm("$truncate", a);
      case ROUND -> definedAtomicTerm("$round", a);
      case IS_INTEGER -> definedAtomicTerm("$is_int", a);
      case IS_RATIONAL -> definedAtomicTerm("$is_rat", a);
      case LESS -> definedAtomicTerm("$less", a);
      case LESS_EQUALS -> definedAtomicTerm("$lesseq", a);
      case ADD -> definedAtomicTerm("$sum", a);
      case SUBTRACT -> definedAtomicTerm("$difference", a);
      case MULTIPLY -> definedAtomicTerm("$product", a);
      case DIVIDE -> definedAtomicTerm("$quotient", a);
      case DIVIDE_EUCLIDEAN -> definedAtomicTerm("$quotient_e", a);
      case DIVIDE_FLOOR -> definedAtomicTerm("$quotient_f", a);
      case DIVIDE_TRUNCATE -> definedAtomicTerm("$quotient_t", a);
      case REMAINDER_EUCLIDEAN -> definedAtomicTerm("$remainder_e", a);
      case REMAINDER_FLOOR -> definedAtomicTerm("$remainder_f", a);
      case REMAINDER_TRUNCATE -> definedAtomicTerm("$remainder_t", a);
      case DISTINCT_OBJECT -> quote('"', a.toString());
      case VAR -> {
        var i = vars.get(a);
        if (i == null) {
          i = vars.size();
          vars.put(a, i);
        }
        if (i < 26) {
          System.out.print('A' + i);
          return;
        }
        System.out.print('Z');
        System.out.print(i - 25);
      }
      case CALL -> {
        print(a.get(0));
        System.out.print('(');
        var n = a.size();
        for (var i = 1; i < n; i++) {
          if (i > 1) System.out.print(',');
          print(a.get(i));
        }
        System.out.print(')');
      }
    }
  }

  public void print(Term a) {
    print(null, a);
  }

  // formulas
  private void id(AbstractFormula formula) {
    var i = formulas.get(formula);
    if (i == null) {
      i = formulas.size();
      formulas.put(formula, i);
    }
    System.out.print(i);
  }

  public void print(Formula formula) {
    System.out.print("tff(");

    // name
    id(formula);

    // role
    System.out.print(formula.negatedConjecture ? ", negated_conjecture, " : ", axiom, ");

    // formula
    print(formula.term);
    System.out.print(", ");

    // source
    if (formula.negatedConjecture) System.out.print("inference(negate,[status(ceq)],[");
    System.out.print("file(");
    quote('\'', formula.file);
    System.out.print(',');
    maybeQuote(formula.name);
    if (formula.negatedConjecture) System.out.print(")]");
    System.out.println(")).");
  }

  public void print(Clause c) {
    System.out.print("tcf(");

    // name
    id(c);

    // role
    System.out.print(", plain, ");

    // literals
    print(c.term());

    // source
    System.out.print(", inference(");
    var from = c.from;
    System.out.print(from.length == 1 ? 'o' : 's');
    System.out.print(",[status(thm)],[");
    for (var i = 0; i < from.length; i++) {
      if (i > 0) System.out.print(',');
      id(from[i]);
    }
    System.out.println("])).");
  }
}
