package olivine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class DimacsParser {
  // Problem state
  private final Map<String, GlobalVar> variables = new HashMap<>();

  // File state
  private final String file;
  private final InputStream stream;
  private int c;
  private int line = 1;
  private int tok;
  private String tokString;

  private ParseError err(String s) {
    return new ParseError(String.format("%s:%d: %s", file, line, s));
  }

  // Tokenizer
  private void readc(StringBuilder sb) throws IOException {
    sb.append((char) c);
    c = stream.read();
  }

  private void lex() throws IOException {
    for (; ; ) {
      tok = c;
      switch (c) {
        case 'c' -> {
          do c = stream.read();
          while (c != '\n' && c >= 0);
          continue;
        }
        case '\n' -> {
          line++;
          c = stream.read();
          continue;
        }
        case ' ', '\f', '\r', '\t' -> {
          c = stream.read();
          continue;
        }
        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
          var sb = new StringBuilder();
          do readc(sb);
          while (Etc.isDigit(c));
          tok = '9';
          tokString = sb.toString();
        }
        default -> c = stream.read();
      }
      return;
    }
  }

  // parser
  GlobalVar variable() throws IOException {
    var a = variables.get(tokString);
    if (a == null) {
      a = new GlobalVar(tokString, Type.BOOLEAN);
      variables.put(tokString, a);
    }
    lex();
    return a;
  }

  // top level
  private DimacsParser(String file, InputStream stream, CNF cnf) throws IOException {
    this.file = file;
    this.stream = stream;
    c = stream.read();
    lex();

    // problem statistics
    if (tok == 'p') {
      while (0 <= c && c <= ' ') c = stream.read();

      // cnf
      if (c != 'c') throw err("expected 'cnf'");
      c = stream.read();
      if (c != 'n') throw err("expected 'cnf'");
      c = stream.read();
      if (c != 'f') throw err("expected 'cnf'");
      c = stream.read();
      lex();

      // variables
      if (tok != '9') throw err("expected integer");
      lex();

      // clauses
      if (tok != '9') throw err("expected integer");
      lex();
    }

    // clauses
    var literals = new ArrayList<Term>();
    for (; ; )
      switch (tok) {
        case -1 -> {
          if (!literals.isEmpty()) cnf.add(Term.of(Tag.OR, literals));
          return;
        }
        case '-' -> {
          lex();
          if (tok != '9') throw err("expected variable");
          literals.add(Term.of(Tag.NOT, variable()));
        }
        case '9' -> literals.add(variable());
        case '0' -> {
          lex();
          cnf.add(Term.of(Tag.OR, literals));
          literals.clear();
        }
        default -> throw err("syntax error");
      }
  }

  public static void parse(String file, InputStream stream, CNF cnf) throws IOException {
    new DimacsParser(file, stream, cnf);
  }
}
