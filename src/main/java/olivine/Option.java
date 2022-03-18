package olivine;

public final class Option {
  private final String s;
  public final String option;
  public final String arg;

  public Option(String s) {
    this.s = s;
    while (s.startsWith("-")) s = s.substring(1);

    // -o123
    if (s.length() > 1 && Etc.isDigit(s.charAt(1))) {
      option = s.substring(0, 1);
      arg = s.substring(1);
      return;
    }

    // find end of option
    var j = 0;
    while (j < s.length() && (Etc.isIdPart(s.charAt(j)) || s.charAt(j) == '-')) j++;

    // -opt
    if (j == s.length()) {
      option = s;
      arg = null;
      return;
    }

    // -opt=arg
    switch (s.charAt(j)) {
      case ':':
      case '=':
        option = s.substring(0, j);
        arg = s.substring(j + 1);
        return;
    }

    // -opt?
    option = s;
    arg = null;
  }

  public String getArg() {
    if (arg == null) {
      System.err.printf("%s: expected arg\n", s);
      System.exit(1);
    }
    return arg;
  }
}
