package olivine;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
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
  final CNF cnf;
  final Map<String, OpaqueType> types;
  final Map<String, DistinctObject> distinctObjects;
  final Map<String, Global> globals;

  // File state
  private final String file;
  private final InputStream stream;
  private final Set<String> select;
  private int c;
  private int line = 1;
  private int tok;
  private String tokString;
  private final Map<String, Var> free = new HashMap<>();

  private ParseException err(String s) {
    return new ParseException(String.format("%s:%d: %s", file, line, s));
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
          if (c != '*') throw err("expected '*'");
          c = stream.read();
          for (; ; ) {
            switch (c) {
              case -1 -> throw err("unclosed block comment");
              case '*' -> c = stream.read();
              default -> {
                continue;
              }
            }
            if (c == '/') break;
          }
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

  private String word() throws IOException {
    if (tok != WORD) throw err("expected word");
    var s = tokString;
    lex();
    return s;
  }

  // types
  private Type atomicType() throws IOException {
    var s = tokString;
    switch (tok) {
      case '!', '[' -> throw new InappropriateException();
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
  private Term global(String name, boolean func) {
    var a = globals.get(name);
    if (a == null) {
      a = func ? new Func(name) : new GlobalVar(name);
      globals.put(name, a);
    }
    return a;
  }

  private List<Term> args(Map<String, Var> bound) throws IOException {
    expect('(');
    var v = new ArrayList<Term>();
    do v.add(atomicTerm(bound));
    while (eat(','));
    expect(')');
    return v;
  }

  private Term definedAtomicTerm(Map<String, Var> bound, Tag tag) throws IOException {
    return Term.of(tag, args(bound));
  }

  private Term atomicTerm(Map<String, Var> bound) throws IOException {
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
          case "ceiling" -> {
            return definedAtomicTerm(bound, Tag.CEILING);
          }
          case "difference" -> {
            return definedAtomicTerm(bound, Tag.SUBTRACT);
          }
          case "distinct" -> {
            var v = args(bound);
            for (var a : v) a.defaultType(Type.INDIVIDUAL);
            var inequalities = new ArrayList<Term>();
            for (var i = 0; i < v.size(); i++)
              for (var j = 0; j < v.size(); j++)
                if (i != j)
                  inequalities.add(Term.of(Tag.NOT, Term.of(Tag.EQUALS, v.get(i), v.get(j))));
            return Term.of(Tag.AND, inequalities);
          }
          case "false" -> {
            return Term.FALSE;
          }
          case "floor" -> {
            return definedAtomicTerm(bound, Tag.FLOOR);
          }
          case "greater" -> {
            var v = args(bound);
            return Term.of(Tag.LESS, v.get(1), v.get(0));
          }
          case "greatereq" -> {
            var v = args(bound);
            return Term.of(Tag.LESS_EQUALS, v.get(1), v.get(0));
          }
          case "is_int" -> {
            return definedAtomicTerm(bound, Tag.IS_INTEGER);
          }
          case "is_rat" -> {
            return definedAtomicTerm(bound, Tag.IS_RATIONAL);
          }
          case "less" -> {
            return definedAtomicTerm(bound, Tag.LESS);
          }
          case "lesseq" -> {
            return definedAtomicTerm(bound, Tag.LESS_EQUALS);
          }
          case "product" -> {
            return definedAtomicTerm(bound, Tag.MULTIPLY);
          }
          case "quotient" -> {
            return definedAtomicTerm(bound, Tag.DIVIDE);
          }
          case "quotient_e" -> {
            return definedAtomicTerm(bound, Tag.DIVIDE_EUCLIDEAN);
          }
          case "quotient_f" -> {
            return definedAtomicTerm(bound, Tag.DIVIDE_FLOOR);
          }
          case "quotient_t" -> {
            return definedAtomicTerm(bound, Tag.DIVIDE_TRUNCATE);
          }
          case "remainder_e" -> {
            return definedAtomicTerm(bound, Tag.REMAINDER_EUCLIDEAN);
          }
          case "remainder_f" -> {
            return definedAtomicTerm(bound, Tag.REMAINDER_FLOOR);
          }
          case "remainder_t" -> {
            return definedAtomicTerm(bound, Tag.REMAINDER_TRUNCATE);
          }
          case "round" -> {
            return definedAtomicTerm(bound, Tag.ROUND);
          }
          case "sum" -> {
            return definedAtomicTerm(bound, Tag.ADD);
          }
          case "to_int" -> {
            var v = args(bound);
            return Term.cast(Type.INTEGER, v.get(0));
          }
          case "to_rat" -> {
            var v = args(bound);
            return Term.cast(Type.RATIONAL, v.get(0));
          }
          case "to_real" -> {
            var v = args(bound);
            return Term.cast(Type.REAL, v.get(0));
          }
          case "true" -> {
            return Term.TRUE;
          }
          case "truncate" -> {
            return definedAtomicTerm(bound, Tag.TRUNCATE);
          }
          case "uminus" -> {
            return definedAtomicTerm(bound, Tag.NEGATE);
          }
          case "ite" -> throw new InappropriateException();
          default -> throw err(String.format("'$%s': unknown word", s));
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
        {
          return Term.of(new BigInteger(s));
        }
      case RATIONAL:
        {
          return Term.of(Type.RATIONAL, BigRational.of(s));
        }
      case REAL:
        {
          return Term.of(Type.REAL, BigRational.ofDecimal(s));
        }
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
          var a = global(s, tok == '(');
          if (eat('(')) {
            var v = new ArrayList<Term>();
            v.add(a);
            do {
              var b = atomicTerm(bound);
              b.defaultType(Type.INDIVIDUAL);
              v.add(b);
            } while (eat(','));
            expect(')');
            return Term.of(Tag.CALL, v);
          }
          return a;
        }
      default:
        throw err("expected term");
    }
  }

  private Term infixEquals(Map<String, Var> bound, Term a) throws IOException {
    lex();
    var b = atomicTerm(bound);
    a.defaultType(Type.INDIVIDUAL);
    b.defaultType(Type.INDIVIDUAL);
    return Term.of(Tag.EQUALS, a, b);
  }

  private Term infixUnary(Map<String, Var> bound) throws IOException {
    var a = atomicTerm(bound);
    return switch (tok) {
      case '=' -> infixEquals(bound, a);
      case NOT_EQUALS -> Term.of(Tag.NOT, infixEquals(bound, a));
      default -> a;
    };
  }

  private Term quant(Map<String, Var> bound, Tag tag) throws IOException {
    if (bound == null) throw err("quantifier in cnf");
    lex();
    expect('[');
    var v = new ArrayList<Term>();
    bound = new HashMap<>(bound);
    do {
      if (tok != VAR) throw err("expected variable");
      var name = tokString;
      lex();
      var type = Type.INDIVIDUAL;
      if (eat(':')) type = atomicType();
      var x = new Var(type);
      v.add(x);
      bound.put(name, x);
    } while (eat(','));
    expect(']');
    expect(':');
    return Term.of(tag, unary(bound), v);
  }

  private Term unary(Map<String, Var> bound) throws IOException {
    switch (tok) {
      case '(' -> {
        lex();
        var a = logicFormula(bound);
        expect(')');
        return a;
      }
      case '~' -> {
        lex();
        return Term.of(Tag.NOT, unary(bound));
      }
      case '!' -> {
        return quant(bound, Tag.ALL);
      }
      case '?' -> {
        return quant(bound, Tag.EXISTS);
      }
      default -> {
        var a = infixUnary(bound);
        a.setType(Type.BOOLEAN);
        return a;
      }
    }
  }

  private Term logicFormula1(Map<String, Var> bound, Tag tag, Term a) throws IOException {
    var k = tok;
    var v = new ArrayList<Term>();
    v.add(a);
    while (eat(k)) v.add(unary(bound));
    return Term.of(tag, v);
  }

  private Term logicFormula(Map<String, Var> bound) throws IOException {
    var a = unary(bound);
    switch (tok) {
      case '&' -> {
        return logicFormula1(bound, Tag.AND, a);
      }
      case '|' -> {
        return logicFormula1(bound, Tag.OR, a);
      }
      case EQV -> {
        lex();
        return Term.of(Tag.EQV, a, unary(bound));
      }
      case IMPLIES -> {
        lex();
        return a.implies(unary(bound));
      }
      case IMPLIESR -> {
        lex();
        return unary(bound).implies(a);
      }
      case NAND -> {
        lex();
        return Term.of(Tag.NOT, Term.of(Tag.AND, a, unary(bound)));
      }
      case NOR -> {
        lex();
        return Term.of(Tag.NOT, Term.of(Tag.OR, a, unary(bound)));
      }
      case XOR -> {
        lex();
        return Term.of(Tag.NOT, Term.of(Tag.EQV, a, unary(bound)));
      }
      default -> {
        return a;
      }
    }
  }

  // top level
  private String formulaName() throws IOException {
    switch (tok) {
      case WORD, INTEGER -> {
        var s = tokString;
        lex();
        return s;
      }
      default -> throw err("expected formula name");
    }
  }

  private boolean selecting(String name) {
    if (select == null) return true;
    return select.contains(name);
  }

  private void collect(String name, boolean negatedConjecture, Term a) {
    if (!selecting(name)) return;
    a.check(Type.BOOLEAN);
    cnf.add(new Formula(name, negatedConjecture, a, file));
  }

  private void skip() throws IOException {
    switch (tok) {
      case '(' -> {
        lex();
        while (!eat(')')) skip();
      }
      case -1 -> throw err("unclosed '('");
      default -> lex();
    }
  }

  private TptpParser(
      String file,
      InputStream stream,
      CNF cnf,
      Map<String, OpaqueType> types,
      Map<String, DistinctObject> distinctObjects,
      Map<String, Global> globals,
      Set<String> select)
      throws IOException {
    this.file = file;
    this.stream = stream;
    this.cnf = cnf;
    this.types = types;
    this.distinctObjects = distinctObjects;
    this.globals = globals;
    this.select = select;
    c = stream.read();
    lex();
    try {
      while (tok != -1) {
        var s = word();
        expect('(');
        var name = formulaName();
        switch (s) {
          case "cnf" -> {
            expect(',');

            word();
            expect(',');

            // we could treat CNF input specially as clauses, but it is equally correct and simpler
            // to just treat it as formulas
            var a = logicFormula(null).quantify();
            collect(name, false, a);
          }
          case "fof", "tff", "tcf" -> {
            expect(',');

            var role = word();
            expect(',');

            if (role.equals("type")) {
              // either naming a type, or typing a global
              var parens = 0;
              while (eat('(')) parens++;

              name = word();
              expect(':');

              if (tok == DEFINED_WORD && tokString.equals("tType")) {
                lex();
                if (tok == '>')
                  // this is some higher-order construct that Olivine doesn't understand
                  throw new InappropriateException();
                // Otherwise, the symbol will be simply used as the name of a type. No particular
                // action is
                // required at this point, so accept this and move on.
              } else {
                // The symbol is the name of a global  with the specified type.
                var type = topLevelType();
                var a = global(name, type.kind() == Kind.FUNC);
                a.setType(type);
              }

              while (parens-- > 0) expect(')');
              break;
            }

            // formula
            var negatedConjecture = false;
            var a = logicFormula(Map.of());
            assert a.freeVars().equals(Set.of());
            if (selecting(name)) {
              if (role.equals("conjecture")) {
                negatedConjecture = true;
                a = Term.of(Tag.NOT, a);
                cnf.conjecture = true;
              }
              collect(name, negatedConjecture, a);
            }
          }
          case "thf" -> throw new InappropriateException();
          case "include" -> {
            var dir = System.getenv("TPTP");
            if (dir == null) throw err("TPTP environment variable not set");
            var file1 = Path.of(dir, name).toString();
            var select1 = select;
            if (eat(',')) {
              if (tok == WORD && tokString.equals("all")) {
                lex();
              } else {
                expect('[');
                select1 = new HashSet<>();
                do {
                  var name1 = formulaName();
                  if (selecting(name1)) select1.add(name1);
                } while (eat(','));
                expect(']');
              }
            }
            try (var stream1 = new BufferedInputStream(new FileInputStream(file1))) {
              new TptpParser(file1, stream1, cnf, types, distinctObjects, globals, select1);
            }
          }
          default -> throw err(String.format("'%s': unknown language", s));
        }
        if (tok == ',') do skip(); while (tok != ')');
        expect(')');
        expect('.');
      }
    } catch (TypeException e) {
      throw err(e.getMessage());
    }
  }

  public static void parse(String file, InputStream stream, CNF cnf) throws IOException {
    new TptpParser(file, stream, cnf, new HashMap<>(), new HashMap<>(), new HashMap<>(), null);
  }
}
