package olivine;

import java.io.IOException;

public final class ParseException extends IOException {
  public ParseException(String message) {
    super(message);
  }
}
