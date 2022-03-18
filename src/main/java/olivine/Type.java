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

        @Override
        public String toString() {
          return "$o";
        }
      };
  public static final Type INDIVIDUAL =
      new Type() {
        @Override
        Kind kind() {
          return Kind.INDIVIDUAL;
        }

        @Override
        public String toString() {
          return "$i";
        }
      };
  public static final Type INTEGER =
      new Type() {
        @Override
        Kind kind() {
          return Kind.INTEGER;
        }

        @Override
        public String toString() {
          return "$int";
        }
      };
  public static final Type RATIONAL =
      new Type() {
        @Override
        Kind kind() {
          return Kind.RATIONAL;
        }

        @Override
        public String toString() {
          return "$rat";
        }
      };
  public static final Type REAL =
      new Type() {
        @Override
        Kind kind() {
          return Kind.REAL;
        }

        @Override
        public String toString() {
          return "$real";
        }
      };

  public final boolean isNumeric() {
    return switch (kind()) {
      case INTEGER, RATIONAL, REAL -> true;
      default -> false;
    };
  }

  private static final class Types extends Type {
    final Kind kind;
    final Type[] v;

    private Types(Kind kind, Type[] v) {
      this.kind = kind;
      this.v = v;
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
