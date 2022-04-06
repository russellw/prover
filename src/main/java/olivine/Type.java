package olivine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Type {
  abstract Kind kind();

  public int size() {
    return 0;
  }

  public Type get(int i) {
    throw new UnsupportedOperationException(kind().toString());
  }

  public int compareTo(Type b) {
    // ordered comparison of types is not particularly meaningful, but that doesn't matter;
    // superposition calculus needs a total order on ground terms, which for some terms,
    // means ordering by type, so we need a total order on types
    return kind().compareTo(b.kind());
  }

  public static Type of(Kind kind, Type... v) {
    return new Types(kind, v);
  }

  public static Type of(Kind kind, List<Type> v) {
    return new Types(kind, v.toArray(new Type[0]));
  }

  public static final Type BOOLEAN =
      new Type() {
        @Override
        Kind kind() {
          return Kind.BOOLEAN;
        }
      };
  public static final Type INDIVIDUAL =
      new Type() {
        @Override
        Kind kind() {
          return Kind.INDIVIDUAL;
        }
      };
  public static final Type INTEGER =
      new Type() {
        @Override
        Kind kind() {
          return Kind.INTEGER;
        }
      };
  public static final Type RATIONAL =
      new Type() {
        @Override
        Kind kind() {
          return Kind.RATIONAL;
        }
      };
  public static final Type REAL =
      new Type() {
        @Override
        Kind kind() {
          return Kind.REAL;
        }
      };

  public final boolean isNumeric() {
    return switch (kind()) {
      case INTEGER, RATIONAL, REAL -> true;
      default -> false;
    };
  }

  @Override
  public String toString() {
    return kind().toString();
  }

  private static final class Types extends Type {
    final Kind kind;
    final Type[] v;

    private Types(Kind kind, Type[] v) {
      this.kind = kind;
      this.v = v;
    }

    @Override
    public int compareTo(Type b) {
      var c = kind.compareTo(b.kind());
      if (c != 0) return c;
      var b1 = (Types) b;
      if (v.length != b1.v.length) return v.length - b1.v.length;
      for (var i = 0; i < v.length; i++) {
        c = v[i].compareTo(b1.v[i]);
        if (c != 0) return c;
      }
      assert equals(b);
      return 0;
    }

    @Override
    public int size() {
      return v.length;
    }

    @Override
    public Type get(int i) {
      return v[i];
    }

    @Override
    public String toString() {
      return Arrays.toString(v);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Types types = (Types) o;
      return kind == types.kind && Arrays.equals(v, types.v);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(kind);
      result = 31 * result + Arrays.hashCode(v);
      return result;
    }

    @Override
    Kind kind() {
      return kind;
    }
  }
}
