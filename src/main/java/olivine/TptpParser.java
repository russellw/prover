package olivine;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

public final class TptpParser {
  // Tokens
  private static final int DEFINED_WORD = -2;
  private static final int DISTINCT_OBJECT = -3;
  private static final int EQV = -4;
  private static final int IMPLIES = -5;
  private static final int IMPLIESR = -6;
  private static final int INTEGER = -7;
  private static final int NAND = -8;
  private static final int NOT_EQUALS = -9;
  private static final int NOR = -10;
  private static final int RATIONAL = -11;
  private static final int REAL = -12;
  private static final int VAR = -13;
  private static final int WORD = -14;
  private static final int XOR = -15;

  // Problem state
  private static final class Problem {
    final Map<String, OpaqueType> types = new HashMap<>();
    final Map<String, Term> globals = new HashMap<>();
    final Map<String, DistinctObject> distinctObjects = new HashMap<>();
    Formula negatedConjecture;
    final List<Formula> formulas;

    Problem(List<Formula> formulas) {
      this.formulas = formulas;
    }
  }

  final Map<String, OpaqueType> types;
  final Map<String, Term> globals;
  final Map<String, DistinctObject> distinctObjects;
  final List<Formula> formulas;

  // File state
  private final String file;
  private final InputStream stream;
  private final Set<String> select;
  private final Problem problem;
  private int c;
  private int line = 1;
  private int tok;
  private String tokString;
  private final Map<String, Var> free = new HashMap<>();

  private IOException err(String s) {
    return new IOException(String.format("%s:%d: %s", file, line, s));
  }

  // Tokenizer
  private void readc(StringBuilder sb) throws IOException {
    sb.append((char) c);
    c = stream.read();
  }

  private void lexQuote() throws IOException {
    var quote = c;
    var sb = new StringBuilder();
    c = stream.read();
    while (c != quote) {
      if (c < ' ') throw err("unclosed quote");
      if (c == '\\') c = stream.read();
      readc(sb);
    }
    c = stream.read();
    tokString = sb.toString();
  }

  private void lex() throws IOException {
    for (; ; ) {
      tok = c;
      switch (c) {
        case '\n' -> {
          line++;
          c = stream.read();
          continue;
        }
        case ' ', '\f', '\r', '\t' -> {
          c = stream.read();
          continue;
        }
        case '!' -> {
          c = stream.read();
          if (c == '=') {
            c = stream.read();
            tok = NOT_EQUALS;
          }
        }
        case '"' -> {
          lexQuote();
          tok = DISTINCT_OBJECT;
        }
        case '$' -> {
          c = stream.read();
          var sb = new StringBuilder();
          while (Etc.isIdPart(c)) readc(sb);
          tok = DEFINED_WORD;
          tokString = sb.toString();
        }
        case '%' -> {
          do c = stream.read();
          while (c != '\n' && c >= 0);
          continue;
        }
        case '+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
          var sb = new StringBuilder();
          do readc(sb);
          while (Etc.isDigit(c));
          switch (c) {
            case '.' -> {
              do readc(sb);
              while (Etc.isDigit(c));
            }
            case '/' -> {
              do readc(sb);
              while (Etc.isDigit(c));
              tok = RATIONAL;
              tokString = sb.toString();
              return;
            }
            case 'E', 'e' -> {}
            default -> {
              tok = INTEGER;
              tokString = sb.toString();
              return;
            }
          }
          if (c == 'e' || c == 'E') readc(sb);
          if (c == '+' || c == '-') readc(sb);
          while (Etc.isDigit(c)) readc(sb);
          tok = REAL;
          tokString = sb.toString();
        }
        case '/' -> {
          c = stream.read();
          if (c != '*') throw err("'*' expected");
          do {
            do {
              if (c < 0) throw err("unclosed block comment");
              c = stream.read();
            } while (c != '*');
            // TODO: fix
            c = stream.read();
          } while (c != '/');
          c = stream.read();
          continue;
        }
        case '<' -> {
          c = stream.read();
          switch (c) {
            case '=' -> {
              c = stream.read();
              if (c == '>') {
                c = stream.read();
                tok = EQV;
                break;
              }
              tok = IMPLIESR;
            }
            case '~' -> {
              c = stream.read();
              if (c == '>') {
                c = stream.read();
                tok = XOR;
                break;
              }
              throw err("expected '>'");
            }
          }
        }
        case '=' -> {
          c = stream.read();
          if (c == '>') {
            c = stream.read();
            tok = IMPLIES;
          }
        }
        case 'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z' -> {
          var sb = new StringBuilder();
          do readc(sb);
          while (Etc.isIdPart(c));
          tok = VAR;
          tokString = sb.toString();
        }
        case '\'' -> {
          lexQuote();
          if (tokString.length() == 0) throw err("empty word");
          tok = WORD;
        }
        case 'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z' -> {
          var sb = new StringBuilder();
          do readc(sb);
          while (Etc.isIdPart(c));
          tok = WORD;
          tokString = sb.toString();
        }
        case '~' -> {
          c = stream.read();
          switch (c) {
            case '&' -> {
              c = stream.read();
              tok = NAND;
            }
            case '|' -> {
              c = stream.read();
              tok = NOR;
            }
          }
        }
        default -> c = stream.read();
      }
      return;
    }
  }

  // parser
  private boolean eat(int k) throws IOException {
    if (tok == k) {
      lex();
      return true;
    }
    return false;
  }

  private void expect(int k) throws IOException {
    if (!eat(k)) throw err(String.format("expected %c", k));
  }

  // types
  private Type atomicType() throws IOException {
    var s = tokString;
    switch (tok) {
      case DEFINED_WORD -> {
        lex();
        return switch (s) {
          case "o" -> Type.BOOLEAN;
          case "i" -> Type.INDIVIDUAL;
          case "int" -> Type.INTEGER;
          case "rat" -> Type.RATIONAL;
          case "real" -> Type.REAL;
          case "tType" -> throw new InappropriateException();
          default -> throw err(String.format("'$%s': unknown type", s));
        };
      }
      case WORD -> {
        lex();
        var type = types.get(s);
        if (type == null) {
          type = new OpaqueType(s);
          types.put(s, type);
        }
        return type;
      }
    }
    throw err("expected type");
  }

  private Type topLevelType() throws IOException {
    if (eat('(')) {
      var v = new ArrayList<Type>();
      v.add(null);
      do v.add(atomicType());
      while (eat('*'));
      expect(')');
      expect('>');
      var returnType = atomicType();
      v.set(0, returnType);
      return Type.of(Kind.FUNC, v);
    }
    var type = atomicType();
    if (eat('>')) {
      var returnType = atomicType();
      return Type.of(Kind.FUNC, returnType, type);
    }
    return type;
  }

  // terms
  private void args(MapString bound, List<Term> v) throws IOException {
    expect('(');
    do v.add(atomicTerm(bound));
    while (eat(','));
    expect(')');
  }

  private void args(MapString bound, List<Term> v, int arity) throws IOException {
    int n = v.size();
    args(bound, v);
    n = v.size() - n;
    if (n != arity) throw err(String.format("arg count: %d != %d", n, arity));
  }

  private Term definedAtomicTerm(MapString bound, Tag tag, int arity) throws IOException {
    var r = new ArrayList<Term>();
    args(bound, r, arity);
    return Term.of(tag, r);
  }

  private Term atomicTerm(MapString bound) throws IOException {
    var k = tok;
    var s = tokString;
    lex();
    switch (k) {
      case '!':
      case '?':
      case '[':
        throw new InappropriateException();
      case DEFINED_WORD:
        switch (s) {
          case "ceiling":
            return definedAtomicTerm(bound, Tag.CEILING, 1);
          case "difference":
            return definedAtomicTerm(bound, Tag.SUBTRACT, 2);
          case "distinct":
            {
              var v = new ArrayList<Term>();
              args(bound, v);
              var inequalities = new ArrayList<Term>();
              for (var i = 0; i < v.size(); i++)
                for (var j = 0; j < v.size(); j++)
                  if (i != j)
                    inequalities.add(Term.of(Tag.NOT, Term.of(Tag.EQUALS, v.get(i), v.get(j))));
              return Term.of(Tag.AND, inequalities);
            }
          case "false":
            return Term.FALSE;
          case "floor":
            return definedAtomicTerm(bound, Tag.FLOOR, 1);
          case "greater":
            {
              var v = new ArrayList<Term>();
              args(bound, v, 2);
              return Term.of(Tag.LESS, v.get(1), v.get(0));
            }
          case "greatereq":
            {
              var v = new ArrayList<Term>();
              args(bound, v, 2);
              return Term.of(Tag.LESS_EQUALS, v.get(1), v.get(0));
            }
          case "is_int":
            return definedAtomicTerm(bound, Tag.IS_INTEGER, 1);
          case "is_rat":
            return definedAtomicTerm(bound, Tag.IS_RATIONAL, 1);
          case "less":
            return definedAtomicTerm(bound, Tag.LESS, 2);
          case "lesseq":
            return definedAtomicTerm(bound, Tag.LESS_EQUALS, 2);
          case "product":
            return definedAtomicTerm(bound, Tag.MULTIPLY, 2);
          case "quotient":
            return definedAtomicTerm(bound, Tag.DIVIDE, 2);
          case "quotient_e":
            return definedAtomicTerm(bound, Tag.DIVIDE_EUCLIDEAN, 2);
          case "quotient_f":
            return definedAtomicTerm(bound, Tag.DIVIDE_FLOOR, 2);
          case "quotient_t":
            return definedAtomicTerm(bound, Tag.DIVIDE_TRUNCATE, 2);
          case "remainder_e":
            return definedAtomicTerm(bound, Tag.REMAINDER_EUCLIDEAN, 2);
          case "remainder_f":
            return definedAtomicTerm(bound, Tag.REMAINDER_FLOOR, 2);
          case "remainder_t":
            return definedAtomicTerm(bound, Tag.REMAINDER_TRUNCATE, 2);
          case "round":
            return definedAtomicTerm(bound, Tag.ROUND, 1);
          case "sum":
            return definedAtomicTerm(bound, Tag.ADD, 2);
          case "to_int":
            {
              var v = new ArrayList<Term>();
              args(bound, v, 1);
              return Term.cast(Type.INTEGER, v.get(0));
            }
          case "to_rat":
            {
              var v = new ArrayList<Term>();
              args(bound, v, 1);
              return Term.cast(Type.RATIONAL, v.get(0));
            }
          case "to_real":
            {
              var v = new ArrayList<Term>();
              args(bound, v, 1);
              return Term.cast(Type.REAL, v.get(0));
            }
          case "true":
            return Term.TRUE;
          case "truncate":
            return definedAtomicTerm(bound, Tag.TRUNCATE, 1);
          case "uminus":
            return definedAtomicTerm(bound, Tag.NEGATE, 1);
          default:
            throw err(String.format("'$%s': unknown word", s));
        }
      case DISTINCT_OBJECT:
        {
          var a = distinctObjects.get(s);
          if (a == null) {
            a = new DistinctObject(s);
            distinctObjects.put(s, a);
          }
          return a;
        }
      case INTEGER:
        return Term.integer(new BigInteger(s));
      case RATIONAL:
        return Term.rational(Type.RATIONAL, BigRational.of(s));
      case REAL:
        return Term.rational(Type.REAL, BigRational.ofDecimal(s));
      case VAR:
        {
          if (bound == null) {
            var a = free.get(s);
            if (a == null) {
              a = new Var(Type.INDIVIDUAL);
              free.put(s, a);
            }
            return a;
          }
          var a = bound.get(s);
          if (a == null) throw err(String.format("'%s': unknown variable", s));
          return a;
        }
      case WORD:
        {
          if (tok == '(') {
            var r = new ArrayList<Term>();
            args(bound, r);
            var a = globals.get(s);
            if (a == null) {
              a = new Func(s);
              globals.put(s, a);
            }
            return a.call(r);
          }
          var a = globals.get(s);
          if (a == null) {
            a = new GlobalVar(s);
            globals.put(s, a);
          }
          return a;
        }
      default:
        throw err("expected term");
    }
  }

  // top level
  private TptpParser(String file, InputStream stream, Set<String> select, Problem problem)
      throws IOException {
    this.file = file;
    this.stream = stream;
    this.select = select;
    this.problem = problem;

    distinctObjects = problem.distinctObjects;
    formulas = problem.formulas;
    globals = problem.globals;
    types = problem.types;

    lex();
  }

  public static Formula parse(String file, InputStream stream, List<Formula> formulas)
      throws IOException {
    var problem = new Problem(formulas);
    new TptpParser(file, stream, null, problem);
    return problem.negatedConjecture;
  }
}
