package olivine;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Term implements Iterable<Term> {
  public abstract Tag tag();

  public int size() {
    return 0;
  }

  public Term get(int i) {
    throw new UnsupportedOperationException(tag().toString());
  }

  public void defaultType(Type type) {
    if (tag() == Tag.CALL) {
      var f = (Func) get(0);
      if (f.returnType == null) setType(type);
    }
  }

  public void setType(Type type) {
    // A statement about the return type of a function call, can directly imply the type of the
    // function. This generally does not
    // apply to basic operators; in most cases, they already have a definite type. That is not
    // entirely true of the arithmetic
    // operators, but we don't try to do global type inference to figure those out.
    if (tag() == Tag.CALL) {
      var v = new Type[size()];
      v[0] = type;
      for (var i = 1; i < v.length; i++) v[i] = get(i).type();
      get(0).setType(Type.of(Kind.FUNC, v));
    }
  }

  public final void check(Type expected) {
    var type = type();
    if (type == null) throw new TypeException(String.format("%s: null type", this));
    if (!type.equals(expected))
      throw new TypeException(String.format("%s: type error: %s != %s", this, type, expected));

    var n = size();
    switch (tag()) {
      case NOT, AND, OR, EQV -> {
        for (var i = 0; i < n; i++) get(i).check(Type.BOOLEAN);
      }
      case RATIONAL -> {
        switch (type.kind()) {
          case RATIONAL, REAL -> {}
          default -> throw new TypeException(String.format("%s: type error: %s", this, type));
        }
      }
      case GLOBAL_VAR -> {
        if (type.kind() == Kind.FUNC)
          throw new TypeException(String.format("%s: type error: %s", this, type));
      }
      case VAR -> {
        switch (type.kind()) {
          case BOOLEAN, FUNC -> throw new TypeException(
              String.format("%s: type error: %s", this, type));
        }
      }
      case INTEGER -> {
        assert type == Type.INTEGER;
      }
      case FALSE, TRUE -> {
        assert type == Type.BOOLEAN;
      }
      case DISTINCT_OBJECT -> {
        assert type == Type.INDIVIDUAL;
      }
      case EQUALS -> {
        type = get(0).type();
        switch (type.kind()) {
          case BOOLEAN, FUNC -> throw new TypeException(
              String.format("%s: type error: %s", this, type));
        }
        for (var i = 0; i < n; i++) get(i).check(type);
      }
      case ALL, EXISTS -> {
        for (var i = 1; i < n; i++) get(i).check(get(i).type());
        get(0).check(Type.BOOLEAN);
      }
      case CALL -> {
        type = get(0).type();
        if (type.kind() != Kind.FUNC)
          throw new TypeException(String.format("%s: type error: %s", this, type));
        assert type.get(0).equals(expected);
        if (n != type.size())
          throw new TypeException(
              String.format("%s: arity mismatch: %d != %d", this, n, type.size()));
        for (var i = 1; i < n; i++) get(i).check(type.get(i));
      }
      case FUNC -> {
        assert type.kind() == Kind.FUNC;
      }
      case LESS_EQUALS,
          LESS,
          CAST,
          TRUNCATE,
          NEGATE,
          IS_INTEGER,
          IS_RATIONAL,
          ADD,
          SUBTRACT,
          MULTIPLY,
          DIVIDE,
          DIVIDE_EUCLIDEAN,
          DIVIDE_FLOOR,
          DIVIDE_TRUNCATE,
          REMAINDER_EUCLIDEAN,
          REMAINDER_FLOOR,
          REMAINDER_TRUNCATE,
          ROUND,
          FLOOR,
          CEILING -> {
        type = get(0).type();
        if (!type.isNumeric())
          throw new TypeException(String.format("%s: type error: %s is not numeric", this, type));
        for (var i = 0; i < n; i++) get(i).check(type);
      }
    }
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
          CAST,
          RATIONAL,
          FUNC,
          FALSE,
          TRUE,
          DISTINCT_OBJECT,
          INTEGER,
          VAR -> throw new IllegalStateException(tag().toString());
      case CALL -> ((Func) get(0)).returnType;
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
      };

  public static Term of(Tag tag, Term a) {
    return new Term1(tag, a);
  }

  @Override
  public final Iterator<Term> iterator() {
    // TODO: check existing usage
    return new Iterator<>() {
      private int i;

      @Override
      public boolean hasNext() {
        return i < size();
      }

      @Override
      public Term next() {
        return get(i++);
      }
    };
  }

  public static Term of(Tag tag, Term a, Term b) {
    return new Term2(tag, a, b);
  }

  public static Term of(Tag tag, List<Term> v) {
    return switch (v.size()) {
      case 0 -> throw new IllegalArgumentException(tag.toString());
      case 1 -> new Term1(tag, v.get(0));
      case 2 -> new Term2(tag, v.get(0), v.get(1));
      default -> new Terms(tag, v.toArray(new Term[0]));
    };
  }

  public static Term of(Tag tag, Term... v) {
    return switch (v.length) {
      case 0 -> throw new IllegalArgumentException(tag.toString());
      case 1 -> new Term1(tag, v[0]);
      case 2 -> new Term2(tag, v[0], v[1]);
      default -> new Terms(tag, v);
    };
  }

  public static Term cast(Type type, Term a) {
    return new Cast(type, a);
  }

  public static Term integer(BigInteger value) {
    return new IntegerTerm(value);
  }

  public static Term rational(Type type, BigRational value) {
    return new RationalTerm(type, value);
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
    final Type type;
    final BigRational value;

    RationalTerm(Type type, BigRational value) {
      this.type = type;
      this.value = value;
    }

    @Override
    public Type type() {
      return type;
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
      return Tag.RATIONAL;
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

  private static final class Cast extends Term {
    final Type type;
    final Term a;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Cast cast = (Cast) o;
      return type.equals(cast.type) && a.equals(cast.a);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, a);
    }

    Cast(Type type, Term a) {
      this.type = type;
      this.a = a;
    }

    @Override
    public Type type() {
      return type;
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
    public Term remake(Term[] v) {
      assert v.length == 1;
      return new Cast(type, v[0]);
    }

    @Override
    public Tag tag() {
      return Tag.CAST;
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

  public static Term of(Tag tag, Term a, Collection<Term> v) {
    var w = new Term[1 + v.size()];
    w[0] = a;
    var i = 1;
    for (var b : v) w[i++] = b;
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

  public final Term call(Collection<Term> args) {
    assert args.size() > 0;
    return of(Tag.CALL, this, args);
  }

  public static Term of(boolean b) {
    return b ? TRUE : FALSE;
  }

  public void walk(Consumer<Term> f) {
    f.accept(this);
    var n = size();
    for (var i = 0; i < n; i++) get(i).walk(f);
  }

  public final Term mapLeaves(Function<Term, Term> f) {
    var n = size();
    if (n == 0) return f.apply(this);
    var v = new Term[n];
    for (var i = 0; i < n; i++) v[i] = get(i).mapLeaves(f);
    return remake(v);
  }

  public final Term map(Function<Term, Term> f) {
    var n = size();
    if (n == 0) return this;
    var v = new Term[n];
    for (var i = 0; i < n; i++) v[i] = f.apply(get(i));
    return remake(v);
  }

  // TODO: rename MapTerm
  public final Term replace(MapTerm map) {
    return mapLeaves(
        a -> {
          var b = map.get(a);
          assert !Objects.equals(a, b);
          if (b == null) return a;
          return b;
        });
  }

  // TODO: SetTerm might not be worth using
  private void freeVars(SetTerm bound, Set<Term> free) {
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
    freeVars(SetTerm.EMPTY, free);
    return free;
  }

  public final Term[] toArray() {
    var v = new Term[size()];
    for (var i = 0; i < v.length; i++) v[i] = get(i);
    return v;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append(tag());
    if (size() > 0) sb.append(Arrays.toString(toArray()));
    return sb.toString();
  }

  public final Term quantify() {
    var free = freeVars();
    if (free.isEmpty()) return this;
    return of(Tag.ALL, this, free.toArray(new Term[0]));
  }

  public final Term simplify() {
    return this;
  }

  // TODO: Should this be used in other cases?
  public Term remake(Term[] v) {
    return of(tag(), v);
  }

  public final Term splice(List<Integer> position, int i, Term b) {
    if (i == position.size()) return b;
    var v = toArray();
    var j = position.get(i);
    v[j] = v[j].splice(position, i + 1, b);
    return remake(v);
  }
}
