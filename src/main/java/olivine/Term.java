package olivine;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public abstract class Term {
  public abstract Tag tag();

  public int size() {
    return 0;
  }

  public Term get(int i) {
    throw new UnsupportedOperationException(tag().toString());
  }

  public Type type() {
    return switch (tag()) {
      case NOT,
          LESS,
          LESS_EQUALS,
          OR,
          AND,
          EQUALS,
          EQV,
          EXISTS,
          ALL,
          IS_INTEGER,
          IS_RATIONAL -> Type.BOOLEAN;
      case ADD,
          SUBTRACT,
          MULTIPLY,
          DIVIDE,
          CEILING,
          FLOOR,
          ROUND,
          TRUNCATE,
          NEGATE,
          DIVIDE_EUCLIDEAN,
          DIVIDE_FLOOR,
          DIVIDE_TRUNCATE,
          REMAINDER_EUCLIDEAN,
          REMAINDER_FLOOR,
          REMAINDER_TRUNCATE -> get(0).type();
      case GLOBAL_VAR,
          FALSE,
          TRUE,
          DISTINCT_OBJECT,
          INTEGER,
          VAR -> throw new IllegalStateException(tag().toString());
      case CALL -> ((Func) get(0)).returnType;
      case RATIONAL -> null;
      case REAL -> null;
    };
  }

  public static final Term FALSE =
      new Term() {
        @Override
        public Tag tag() {
          return Tag.FALSE;
        }

        @Override
        public Type type() {
          return Type.BOOLEAN;
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
        public Type type() {
          return Type.BOOLEAN;
        }

        @Override
        public String toString() {
          return "$true";
        }
      };

  public static Term of(Tag tag, Term a) {
    return new Term1(tag, a);
  }

  public static Term of(Tag tag, Term a, Term b) {
    return new Term2(tag, a, b);
  }

  public static Term of(Tag tag, Term... v) {
    return switch (v.length) {
      case 0 -> throw new IllegalArgumentException(tag.toString());
      case 1 -> new Term1(tag, v[0]);
      case 2 -> new Term2(tag, v[0], v[1]);
      default -> new Terms(tag, v);
    };
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
    public Type type() {
      return Type.INTEGER;
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

  private static final class Term1 extends Term {
    final Tag tag;
    final Term a;

    Term1(Tag tag, Term a) {
      this.tag = tag;
      this.a = a;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Term1 term1 = (Term1) o;
      return tag == term1.tag && a.equals(term1.a);
    }

    @Override
    public int hashCode() {
      return Objects.hash(tag, a);
    }

    @Override
    public Term get(int i) {
      assert 0 <= i && i < size();
      return a;
    }

    @Override
    public int size() {
      return 1;
    }

    @Override
    public Tag tag() {
      return tag;
    }
  }

  private static final class Term2 extends Term {
    final Tag tag;
    final Term a, b;

    Term2(Tag tag, Term a, Term b) {
      this.tag = tag;
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Term2 term2 = (Term2) o;
      return tag == term2.tag && a.equals(term2.a) && b.equals(term2.b);
    }

    @Override
    public int hashCode() {
      return Objects.hash(tag, a, b);
    }

    @Override
    public Term get(int i) {
      assert 0 <= i && i < size();
      return i == 0 ? a : b;
    }

    @Override
    public int size() {
      return 2;
    }

    @Override
    public Tag tag() {
      return tag;
    }
  }

  private static final class Terms extends Term {
    final Tag tag;
    final Term[] v;

    Terms(Tag tag, Term[] v) {
      this.tag = tag;
      this.v = v;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Terms terms = (Terms) o;
      return tag == terms.tag && Arrays.equals(v, terms.v);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(tag);
      result = 31 * result + Arrays.hashCode(v);
      return result;
    }

    @Override
    public int size() {
      return v.length;
    }

    @Override
    public Term get(int i) {
      return v[i];
    }

    @Override
    public Tag tag() {
      return tag;
    }
  }

  public final Term implies(Term b) {
    return of(Tag.OR, of(Tag.NOT, this), b);
  }

  public static Term of(Tag tag, Term a, Term[] v) {
    var w = new Term[1 + v.length];
    w[0] = a;
    System.arraycopy(v, 0, w, 1, v.length);
    return of(tag, w);
  }

  public static Term[] remove(Term[] v, int i) {
    var w = new Term[v.length - 1];
    System.arraycopy(v, 0, w, 0, i);
    System.arraycopy(v, i + 1, w, i, w.length - i);
    return w;
  }

  public final Term call(Term... args) {
    assert args.length > 0;
    return of(Tag.CALL, this, args);
  }

  public final Term map(Function<Term, Term> f) {
    int n = size();
    if (n == 0) return this;
    var v = new Term[n];
    for (var i = 0; i < n; i++) v[i] = f.apply(get(i));
    return of(tag(), v);
  }

  public final Term replace(FMap map) {
    if (size() == 0) {
      var a = map.get(this);
      assert !Objects.equals(a, this);
      if (a == null) return this;
      return a.replace(map);
    }
    return map(a -> a.replace(map));
  }

  private void freeVars(FSet bound, Set<Term> free) {
    switch (tag()) {
      case VAR -> {
        if (!bound.contains(this)) free.add(this);
        return;
      }
      case ALL, EXISTS -> {
        var n = size();
        for (var i = 1; i < n; i++) bound = bound.add(get(i));
        get(0).freeVars(bound, free);
        return;
      }
    }
    var n = size();
    for (var i = 0; i < n; i++) get(i).freeVars(bound, free);
  }

  public final Set<Term> freeVars() {
    var free = new LinkedHashSet<Term>();
    freeVars(FSet.EMPTY, free);
    return free;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append(tag());
    var n = size();
    if (n > 0) {
      sb.append('[');
      for (var i = 0; i < n; i++) {
        if (i > 0) sb.append(',');
        sb.append(get(i));
      }
    }
    return sb.toString();
  }

  public final Term quantify() {
    var free = freeVars();
    if (free.isEmpty()) return this;
    return of(Tag.ALL, this, free.toArray(new Term[0]));
  }
}
