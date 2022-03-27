package olivine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public final class Image {
  private final Graphics2D g2d;
  private final Font smallFont;
  private final Font bigFont;
  private final int indentWidth;
  private final Map<Term, Integer> vars = new HashMap<>();
  private int width, height;

  public Image(String file, List<Clause> clauses) throws IOException {
    var width = 1800;
    var height = 900;
    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    g2d = image.createGraphics();
    g2d.setRenderingHint(
        RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(
        RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    g2d.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2d.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    smallFont = new Font("Arial", Font.PLAIN, 12);
    g2d.setFont(smallFont);
    indentWidth = fontMetrics().charWidth(' ' * 2);

    bigFont = new Font("Arial", Font.PLAIN, 16);

    print(0, 0, clauses.get(0));

    g2d.dispose();

    file = Etc.addExtension(new File(file).getName(), "png");
    ImageIO.write(image, "png", new File(file));
  }

  private void print(int x, int y, Clause c) {
    g2d.setColor(Color.WHITE);
    width = x;
    for (var i = 0; i < c.literals.length; i++) {
      print(width, y, i < c.negativeSize ? "\u2260" : "=", new Equation(c.literals[i]));
      if (i + 1 < c.literals.length) {
        g2d.setFont(bigFont);
        print(width, y + fontMetrics().getAscent(), "\u2228");
      }
    }
  }

  private void print(int x, int y, String op, Equation e) {
    g2d.setFont(smallFont);
    print(x, y + fontMetrics().getAscent(), e.left);

    g2d.setFont(bigFont);
    print(width, y + fontMetrics().getAscent(), " " + op + " ");

    g2d.setFont(smallFont);
    print(width, y + fontMetrics().getAscent(), e.right);
  }

  private void print(int x, int y, String s) {
    var maxLength = 20;
    if (s.length() > maxLength) s = s.substring(0, maxLength) + "...";
    g2d.drawString(s, x, y);
    width = Math.max(width, x + fontMetrics().stringWidth(s));
    height = Math.max(height, y + fontMetrics().getHeight());
  }

  private void definedAtomicTerm(int x, int y, String op, Term a) {
    print(x, y, op);
    y += fontMetrics().getHeight();
    x += indentWidth;
    for (var b : a) {
      print(x, y, b);
      y += fontMetrics().getHeight();
    }
  }

  private void print(int x, int y, Term a) {
    switch (a.tag()) {
      case CAST -> {
        switch (a.type().kind()) {
          case INTEGER -> definedAtomicTerm(x, y, "$to_int", a);
          case RATIONAL -> definedAtomicTerm(x, y, "$to_rat", a);
          case REAL -> definedAtomicTerm(x, y, "$to_real", a);
          default -> throw new IllegalArgumentException(a.toString());
        }
      }
      case TRUE -> print(x, y, "$true");
      case FALSE -> print(x, y, "$false");
      case INTEGER, GLOBAL_VAR, FUNC -> print(x, y, a.toString());
      case RATIONAL -> {
        if (a.type() == Type.REAL) print(x, y, "$to_real(" + a + ')');
        else print(x, y, a.toString());
      }
      case NEGATE -> definedAtomicTerm(x, y, "$uminus", a);
      case FLOOR -> definedAtomicTerm(x, y, "$floor", a);
      case CEILING -> definedAtomicTerm(x, y, "$ceiling", a);
      case TRUNCATE -> definedAtomicTerm(x, y, "$truncate", a);
      case ROUND -> definedAtomicTerm(x, y, "$round", a);
      case IS_INTEGER -> definedAtomicTerm(x, y, "$is_int", a);
      case IS_RATIONAL -> definedAtomicTerm(x, y, "$is_rat", a);
      case LESS -> definedAtomicTerm(x, y, "$less", a);
      case LESS_EQUALS -> definedAtomicTerm(x, y, "$lesseq", a);
      case ADD -> definedAtomicTerm(x, y, "$sum", a);
      case SUBTRACT -> definedAtomicTerm(x, y, "$difference", a);
      case MULTIPLY -> definedAtomicTerm(x, y, "$product", a);
      case DIVIDE -> definedAtomicTerm(x, y, "$quotient", a);
      case DIVIDE_EUCLIDEAN -> definedAtomicTerm(x, y, "$quotient_e", a);
      case DIVIDE_FLOOR -> definedAtomicTerm(x, y, "$quotient_f", a);
      case DIVIDE_TRUNCATE -> definedAtomicTerm(x, y, "$quotient_t", a);
      case REMAINDER_EUCLIDEAN -> definedAtomicTerm(x, y, "$remainder_e", a);
      case REMAINDER_FLOOR -> definedAtomicTerm(x, y, "$remainder_f", a);
      case REMAINDER_TRUNCATE -> definedAtomicTerm(x, y, "$remainder_t", a);
      case DISTINCT_OBJECT -> print(x, y, '"' + a.toString() + '"');
      case VAR -> {
        var i = vars.get(a);
        if (i == null) {
          i = vars.size();
          vars.put(a, i);
        }
        if (i < 26) {
          print(x, y, Character.toString((char) ('A' + i)));
          return;
        }
        print(x, y, "Z" + (i - 25));
      }
      case CALL -> {
        print(x, y, a.get(0));
        y += fontMetrics().getHeight();
        x += indentWidth;
        var n = a.size();
        for (var i = 1; i < n; i++) {
          print(x, y, a.get(i));
          y += fontMetrics().getHeight();
        }
      }
      default -> throw new IllegalArgumentException(a.toString());
    }
  }

  private FontMetrics fontMetrics() {
    return g2d.getFontMetrics();
  }
}
