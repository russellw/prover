package olivine;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  // File state
  private final String file;
  private final InputStream stream;
  private final Set<String> select;
  private final Problem problem;
  private int c;
  private int line = 1;
  private int tok;
  private String tokString;

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
    switch (tok) {
      case DEFINED_WORD -> {
        var s = tokString;
        lex();
        return switch (s) {
          case "o" -> Type.BOOLEAN;
          case "i" -> Type.INDIVIDUAL;
          case "int" -> Type.INTEGER;
          case "rat" -> Type.RATIONAL;
          case "real" -> Type.REAL;
          case "tType" -> throw new InappropriateException();
          default -> throw err("unknown type");
        };
      }
    }
    throw err("expected type");
  }

  // top level
  private TptpParser(String file, InputStream stream, Set<String> select, Problem problem)
      throws IOException {
    this.file = file;
    this.stream = stream;
    this.select = select;
    this.problem = problem;
    lex();
  }

  public static Formula parse(String file, InputStream stream, List<Formula> formulas)
      throws IOException {
    var problem = new Problem(formulas);
    new TptpParser(file, stream, null, problem);
    return problem.negatedConjecture;
  }
}
