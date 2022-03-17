package olivine;

import java.math.BigInteger;
import java.util.Objects;

public abstract class Term {
  public abstract Tag tag();

  public static final Term FALSE =
      new Term() {
        @Override
        public Tag tag() {
          return Tag.FALSE;
        }

        @Override
        public String toString() {
          return "$false";
        }
      };

  public static final Term TRUE =
      new Term() {
        @Override
        public Tag tag() {
          return Tag.TRUE;
        }

        @Override
        public String toString() {
          return "$true";
        }
      };

  public static Term distinctObject(String name) {
    return new DistinctObject(name);
  }

  public static Term integer(BigInteger value) {
    return new IntegerTerm(value);
  }

  public static Term rational(Tag tag, BigRational value) {
    return new RationalTerm(tag, value);
  }

  public BigInteger integerValue() {
    throw new UnsupportedOperationException(tag().toString());
  }

  public BigRational rationalValue() {
    throw new UnsupportedOperationException(tag().toString());
  }

  private static final class IntegerTerm extends Term {
    final BigInteger value;

    private IntegerTerm(BigInteger value) {
      this.value = value;
    }

    @Override
    public BigInteger integerValue() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      IntegerTerm that = (IntegerTerm) o;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public String toString() {
      return value.toString();
    }

    @Override
    public Tag tag() {
      return Tag.INTEGER;
    }
  }

  private static final class RationalTerm extends Term {
    final Tag tag;
    final BigRational value;

    RationalTerm(Tag tag, BigRational value) {
      this.tag = tag;
      this.value = value;
    }

    @Override
    public String toString() {
      return value.toString();
    }

    @Override
    public BigRational rationalValue() {
      return value;
    }

    @Override
    public Tag tag() {
      return tag;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RationalTerm that = (RationalTerm) o;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }

  private static final class DistinctObject extends Term {
    final String name;

    DistinctObject(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DistinctObject that = (DistinctObject) o;
      return name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }

    @Override
    public Tag tag() {
      return Tag.DISTINCT_OBJECT;
    }
  }
}
