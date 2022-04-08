package olivine;

import java.util.ArrayList;
import java.util.List;

public abstract class Option {
  private final char shortName;
  private final String longName;
  private final String argName;
  private final String description;

  private static boolean parsingOptions = true;

  public static boolean readStdin = false;
  public static final List<String> positionalArgs = new ArrayList<>();

  protected Option(char shortName, String longName, String argName, String description) {
    this.shortName = shortName;
    this.longName = longName;
    this.argName = argName;
    this.description = description;
  }

  private static int help(Option[] options, boolean live, int width) {
    var width1 = 0;
    for (var option : options) {
      // short name
      var n = 4;
      if (live)
        if (option.shortName == '\0') System.out.print("    ");
        else {
          System.out.print('-');
          System.out.print(option.shortName);
          System.out.print(", ");
        }

      // long name
      n += 2 + option.longName.length();
      if (live) {
        System.out.print("--");
        System.out.print(option.longName);
      }

      // arg name
      if (option.argName != null) {
        n += 1 + option.argName.length();
        if (live) {
          System.out.print(' ');
          System.out.print(option.argName);
        }
      }

      // description
      width1 = Math.max(width1, n);
      if (live) {
        for (var i = n; i < width; i++) System.out.print(' ');
        System.out.print("  ");
        System.out.println(option.description);
      }
    }
    return width1;
  }

  public static void help(Option[] options) {
    var width = help(options, false, 0);
    help(options, true, width);
    System.exit(0);
  }

  public abstract void accept(String arg);

  private static boolean isSeparator(char c) {
    return switch (c) {
      case ':', '=' -> true;
      default -> false;
    };
  }

  private static int separator(String s, int i) {
    while (i < s.length() && !isSeparator(s.charAt(i))) i++;
    return i;
  }

  private static Option getOption(Option[] options, char shortName) {
    for (var option : options) if (option.shortName == shortName) return option;
    return null;
  }

  private static Option getOption(Option[] options, String longName) {
    for (var option : options) if (option.longName.equals(longName)) return option;
    return null;
  }

  public static void parse(Option[] options, String[] args) {
    for (var i = 0; i < args.length; i++) {
      var s = args[i];
      if (parsingOptions) {
        if (s.charAt(0) == '-') {
          // -
          if (s.length() == 1) {
            readStdin = true;
            continue;
          }

          // option, maybe with option arg
          Option option;
          int argBegin;
          if (s.charAt(1) == '-') {
            // --
            if (s.length() == 2) {
              parsingOptions = false;
              continue;
            }

            // --opt[=arg]
            argBegin = separator(s, 2);
            option = getOption(options, s.substring(2, argBegin));
            argBegin++;
          } else if (s.length() == 2) {
            // -o
            option = getOption(options, s.charAt(1));
            argBegin = s.length();
          } else if (isSeparator(s.charAt(2))) {
            // -o=arg
            option = getOption(options, s.charAt(1));
            argBegin = 3;
          } else {
            // -oarg
            option = getOption(options, s.charAt(1));
            argBegin = 2;
          }

          // the option was not found
          if (option == null) {
            System.err.printf("%s: unknown option\n", s);
            System.exit(1);
          }

          // not expecting arg
          if (option.argName == null) {
            if (argBegin < s.length()) {
              System.err.printf("%s: unexpected arg\n", s);
              System.exit(1);
            }
            option.accept(null);
            continue;
          }

          // expecting arg
          String arg = null;
          if (argBegin < s.length()) arg = s.substring(argBegin);
          else if (i + 1 == args.length) {
            System.err.printf("%s: expected arg\n", s);
            System.exit(1);
          } else arg = args[++i];
          option.accept(arg);
          continue;
        }
      }
      positionalArgs.add(s);
    }
  }
}
