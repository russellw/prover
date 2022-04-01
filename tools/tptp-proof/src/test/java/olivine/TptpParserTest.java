package olivine;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class TptpParserTest {
  @Test
  public void parse() throws IOException {
    String s;
    CNF cnf;

    s = "";
    cnf = new CNF();
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "%";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/**/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/***/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/****/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/*****/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/******/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/*******/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/********/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/*********/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/**********/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/***********/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);

    s = "/************/";
    TptpParser.parse(null, Etc.stringInputStream(s), cnf);
    assertEquals(cnf.clauses.size(), 0);
  }
}
