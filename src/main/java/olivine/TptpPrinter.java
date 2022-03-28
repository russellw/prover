package olivine;

import java.util.HashMap;
import java.util.LinkedHashSet;
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
          System.out.print((char) ('A' + i));
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

  private void println(Formula formula) {
    System.out.print("tff(");

    // name
    id(formula);

    // role
    System.out.print(formula.negatedConjecture ? ", negated_conjecture, " : ", axiom, ");

    // formula
    print(formula.term());
    System.out.print(", ");

    // source
    if (formula.file == null) System.out.print("introduced(definition)");
    else if (formula.negatedConjecture) {
      System.out.print("inference(negate,[status(cth)],[file(");
      quote('\'', formula.file);
      System.out.print(',');
      maybeQuote(formula.name);
      System.out.print(")])");
    } else {
      System.out.print("file(");
      quote('\'', formula.file);
      System.out.print(',');
      maybeQuote(formula.name);
      System.out.print(')');
    }
    System.out.println(").");
  }

  private void printLiteralInfo(Clause c, int i, boolean reversed) {
    if (i < 0) return;
    System.out.print(", ");
    System.out.print(i);
    System.out.print(',');
    var e = new Equation(c.literals[i]);
    var a = e.left;
    var b = e.right;
    if (reversed) {
      a = e.right;
      b = e.left;
    }
    if (i < c.negativeSize) System.out.print('~');
    print(a);
    if (b != Term.TRUE) {
      System.out.print('=');
      print(b);
    }
  }

  private void println(Clause c) {
    System.out.print("tcf(");

    // name
    id(c);

    // role
    System.out.print(", plain, ");

    // literals
    print(c.term().quantify());

    // source
    var inference = c.inference;
    assert inference != null;

    System.out.printf(", inference(%s,[status(%s)],[", inference.rule, inference.status());
    id(inference.from);
    if (inference.from1 != null) {
      System.out.print(',');
      id(inference.from1);
    }
    System.out.print("])");

    // more  info
    if (inference.from instanceof Clause from) {
      printLiteralInfo(from, inference.literalIndex, inference.reversed);
      var from1 = inference.from1 == null ? from : inference.from1;
      printLiteralInfo(from1, inference.literalIndex1, inference.reversed1);
      if (inference.position != null)
        for (var i : inference.position) {
          System.out.print(',');
          System.out.print(i);
        }
    }

    System.out.println(").");
  }

  public void println(AbstractFormula formula) {
    vars.clear();
    if (formula instanceof Formula) println((Formula) formula);
    else println((Clause) formula);
  }

  public void proof(Clause c) {
    System.out.println("% SZS output start CNFRefutation");
    var proof = c.proof();

    // Skolem symbols that ended up being used in the proof, need to be assigned names
    // before printing. These consist of a string prefix and an integer sequence,
    // which is straightforward enough. The complication is that the input formulas might also
    // contain
    // symbols of this pattern, so we need to check for existing symbols to avoid duplicates.
    // The easiest way to do this is to check for the largest existing number of that pattern
    // and start our sequence at that point
    final long[] i = {-1};
    for (var formula : proof)
      formula
          .term()
          .walkGlobals(
              a -> {
                var s = a.name;
                if (s == null) return;
                if (s.length() < 3) return;
                if (s.charAt(0) != 's') return;
                if (s.charAt(1) != 'K') return;
                var i1 = 0L;
                for (var j = 2; j < s.length(); j++) {
                  var ch = s.charAt(j);
                  if (!Etc.isDigit(ch)) return;
                  i1 = i1 * 10 + ch;
                }
                // integer overflow could have occurred, if the existing name was long enough.
                // That's okay. If the overflowed number was negative, it will be effectively
                // ignored.
                // If it wrapped around all the way to positive again,
                // avoiding that number will constitute erring on the side of safety.
                // in any case, we will never print incorrect output
                i[0] = Math.max(i[0], i1);
              });
    for (var formula : proof)
      formula
          .term()
          .walkGlobals(
              a -> {
                var s = a.name;
                if (s != null) return;
                i[0] = Math.addExact(i[0], 1);
                a.name = String.format("sK%d", i[0]);
              });

    // print type declarations for all symbols
    var globals = new LinkedHashSet<Global>();
    for (var formula : proof) formula.term().walkGlobals(globals::add);
    for (var a : globals) {
      System.out.print("tff(t, type, ");
      print(a);
      System.out.print(": ");
      print(a.type());
      System.out.println(").");
    }

    // print formulas and clauses
    for (var formula : proof) println(formula);
    System.out.println("% SZS output end CNFRefutation");
  }
}
