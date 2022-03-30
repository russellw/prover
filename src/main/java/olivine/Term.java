package olivine;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Term implements Iterable<Term> {
  public FMap match(FMap map, Term b) {
    assert map != null;
    if (equals(b)) return map;
    if (!type().equals(b.type())) return null;
    if (this instanceof Var) {
      var a1 = map.get(this);
      if (a1 != null) return a1.equals(b) ? map : null;
      return map.add(this, b);
    }
    if (tag() != b.tag()) return null;
    var n = size();
    if (n == 0) return null;
    if (n != b.size()) return null;
    for (var i = 0; i < n; i++) {
      map = get(i).match(map, b.get(i));
      if (map == null) break;
    }
    return map;
  }

  public final List<Term> flatten(Tag tag) {
    // optimize for the common special  case
    if (tag() != tag) return Collections.singletonList(this);

    // general case
    var v = new ArrayList<Term>();
    flatten(tag, v);
    return v;
  }

  private void flatten(Tag tag, List<Term> v) {
    if (tag() == tag) {
      var n = size();
      for (var a : this) a.flatten(tag, v);
      return;
    }
    v.add(this);
  }

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

  private void checkArity(int expected) {
    if (size() != expected)
      throw new TypeException(String.format("%s: args count: %d != %d", this, size(), expected));
  }

  public final void check(Type expected) {
    var type = type();
    if (type == null) throw new TypeException(String.format("%s: null type", this));
    if (!type.equals(expected))
      throw new TypeException(String.format("%s: type error: %s != %s", this, type, expected));

    var n = size();
    switch (tag()) {
      case NOT -> {
        checkArity(1);
        get(0).check(Type.BOOLEAN);
      }
      case EQV -> {
        checkArity(2);
        for (var a : this) a.check(Type.BOOLEAN);
      }
      case AND, OR -> {
        for (var a : this) a.check(Type.BOOLEAN);
      }
      case RATIONAL -> {
        assert n == 0;
        switch (type.kind()) {
          case RATIONAL, REAL -> {}
          default -> throw new TypeException(String.format("%s: type error: %s", this, type));
        }
      }
      case GLOBAL_VAR -> {
        assert n == 0;
        if (type.kind() == Kind.FUNC)
          throw new TypeException(String.format("%s: type error: %s", this, type));
      }
      case VAR -> {
        assert n == 0;
        switch (type.kind()) {
          case BOOLEAN, FUNC -> throw new TypeException(
              String.format("%s: type error: %s", this, type));
        }
      }
      case INTEGER -> {
        assert n == 0;
        assert type == Type.INTEGER;
      }
      case FALSE, TRUE -> {
        assert n == 0;
        assert type == Type.BOOLEAN;
      }
      case DISTINCT_OBJECT -> {
        assert n == 0;
        assert type == Type.INDIVIDUAL;
      }
      case EQUALS -> {
        checkArity(2);
        type = get(0).type();
        switch (type.kind()) {
          case BOOLEAN, FUNC -> throw new TypeException(
              String.format("%s: type error: %s", this, type));
        }
        for (var a : this) a.check(type);
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
        assert n == 0;
        assert type.kind() == Kind.FUNC;
      }
      case CAST, TRUNCATE, NEGATE, IS_INTEGER, IS_RATIONAL, ROUND, FLOOR, CEILING -> {
        checkArity(1);
        type = get(0).type();
        if (!type.isNumeric())
          throw new TypeException(String.format("%s: type error: %s is not numeric", this, type));
        get(0).check(type);
      }
      case LESS_EQUALS,
          LESS,
          ADD,
          SUBTRACT,
          MULTIPLY,
          DIVIDE_EUCLIDEAN,
          DIVIDE_FLOOR,
          DIVIDE_TRUNCATE,
          REMAINDER_EUCLIDEAN,
          REMAINDER_FLOOR,
          REMAINDER_TRUNCATE -> {
        checkArity(2);
        type = get(0).type();
        if (!type.isNumeric())
          throw new TypeException(String.format("%s: type error: %s is not numeric", this, type));
        for (var a : this) a.check(type);
      }
      case DIVIDE -> {
        checkArity(2);
        type = get(0).type();
        if (type != Type.RATIONAL && type != Type.REAL)
          throw new TypeException(String.format("%s: type error: %s", this, type));
        for (var a : this) a.check(type);
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

  public static Term of(long value) {
    return new IntegerTerm(BigInteger.valueOf(value));
  }

  public static Term of(BigInteger value) {
    return new IntegerTerm(value);
  }

  public static Term of(Type type, BigRational value) {
    return new RationalTerm(type, value);
  }

  public BigInteger integerValue() {
    return null;
  }

  public BigRational rationalValue() {
    return null;
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
      assert type == Type.RATIONAL || type == Type.REAL;
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

  public final long symbolCount() {
    var n = size();
    if (n == 0) return 1;
    var r = 1L;
    for (var i = tag() == Tag.CALL ? 1 : 0; i < n; i++) r += get(i).symbolCount();
    return r;
  }

  public final void walkLeaves(Consumer<Term> f) {
    if (size() == 0) {
      f.accept(this);
      return;
    }
    for (var a : this) a.walkLeaves(f);
  }

  public void walkGlobals(Consumer<Global> f) {
    for (var a : this) a.walkGlobals(f);
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

  public final Term replace(FMap map) {
    return mapLeaves(
        a -> {
          var b = map.get(a);
          assert !Objects.equals(a, b);
          if (b == null) return a;
          return b.replace(map);
        });
  }

  public final void freeVars(Set<Term> bound, Set<Term> free) {
    switch (tag()) {
      case VAR -> {
        if (!bound.contains(this)) free.add(this);
        return;
      }
      case ALL, EXISTS -> {
        bound = new HashSet<>(bound);
        var n = size();
        for (var i = 1; i < n; i++) bound.add(get(i));
        get(0).freeVars(bound, free);
        return;
      }
    }
    var n = size();
    for (var i = 0; i < n; i++) get(i).freeVars(bound, free);
  }

  public final Set<Term> freeVars() {
    var free = new LinkedHashSet<Term>();
    freeVars(Set.of(), free);
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

  public final boolean isConst() {
    return switch (tag()) {
      case DISTINCT_OBJECT, INTEGER, RATIONAL, TRUE, FALSE -> true;
      default -> false;
    };
  }

  public boolean contains(Var b) {
    for (var a : this) if (a.contains(b)) return true;
    return false;
  }

  public final Term simplify() {
    var a = map(Term::simplify);
    switch (a.tag()) {
      case EQUALS -> {
        var x = a.get(0);
        var y = a.get(1);
        if (x.equals(y)) return TRUE;
        if (x.isConst() && y.isConst()) return FALSE;
      }
      case LESS -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null) return of(xi.compareTo(yi) < 0);
        }

        var xr = x.rationalValue();
        if (xr != null) {
          var yr = y.rationalValue();
          if (yr != null) return of(xr.compareTo(yr) < 0);
        }
      }
      case LESS_EQUALS -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null) return of(xi.compareTo(yi) <= 0);
        }

        var xr = x.rationalValue();
        if (xr != null) {
          var yr = y.rationalValue();
          if (yr != null) return of(xr.compareTo(yr) <= 0);
        }
      }
      case NEGATE -> {
        var x = a.get(0);

        var xi = x.integerValue();
        if (xi != null) return of(xi.negate());

        var xr = x.rationalValue();
        if (xr != null) return of(x.type(), xr.negate());
      }
      case CAST -> {
        var x = a.get(0);
        var type = type();
        if (type == x.type()) return x;
        switch (type.kind()) {
          case RATIONAL, REAL -> {
            BigRational r;
            var xi = x.integerValue();
            if (xi != null) r = BigRational.of(xi);
            else r = x.rationalValue();
            if (r != null) return of(type, r);
          }
          case INTEGER -> {
            var xr = x.rationalValue();
            // Different languages have different conventions on the default rounding mode for
            // converting fractions to integers. TPTP
            // defines it as floor, so that is used here. To use a different rounding mode,
            // explicity round the rational number first,
            // and then convert to integer.
            if (xr != null) return of(Etc.divideFloor(xr.num, xr.den));
          }
        }
      }
      case CEILING -> {
        var x = a.get(0);

        var xr = x.rationalValue();
        if (xr != null) return of(x.type(), BigRational.of(xr.ceiling()));
      }
      case FLOOR -> {
        var x = a.get(0);

        var xr = x.rationalValue();
        if (xr != null) return of(x.type(), BigRational.of(xr.floor()));
      }
      case ROUND -> {
        var x = a.get(0);

        var xr = x.rationalValue();
        if (xr != null) return of(x.type(), BigRational.of(xr.round()));
      }
      case TRUNCATE -> {
        var x = a.get(0);

        var xr = x.rationalValue();
        if (xr != null) return of(x.type(), BigRational.of(xr.truncate()));
      }
      case IS_INTEGER -> {
        var x = a.get(0);
        if (x.type() == Type.INTEGER) return TRUE;

        var xr = x.rationalValue();
        if (xr != null) return of(xr.den.equals(BigInteger.ONE));
      }
      case IS_RATIONAL -> {
        var x = a.get(0);
        var xtype = x.type();
        if (xtype == Type.INTEGER || xtype == Type.RATIONAL) return TRUE;

        var xr = x.rationalValue();
        if (xr != null) return TRUE;
      }
      case ADD -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null) return of(xi.add(yi));
        }

        var xr = x.rationalValue();
        if (xr != null) {
          var yr = y.rationalValue();
          if (yr != null) return of(x.type(), xr.add(yr));
        }
      }
      case SUBTRACT -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null) return of(xi.subtract(yi));
        }

        var xr = x.rationalValue();
        if (xr != null) {
          var yr = y.rationalValue();
          if (yr != null) return of(x.type(), xr.subtract(yr));
        }
      }
      case MULTIPLY -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null) return of(xi.multiply(yi));
        }

        var xr = x.rationalValue();
        if (xr != null) {
          var yr = y.rationalValue();
          if (yr != null) return of(x.type(), xr.multiply(yr));
        }
      }
      case DIVIDE -> {
        var x = a.get(0);
        var y = a.get(1);

        var xr = x.rationalValue();
        if (xr != null) {
          var yr = y.rationalValue();
          if (yr != null && yr.signum() != 0) return of(x.type(), xr.divide(yr));
        }
      }
      case DIVIDE_EUCLIDEAN -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null && yi.signum() != 0) return of(Etc.divideEuclidean(xi, yi));
        }
      }
      case DIVIDE_FLOOR -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null && yi.signum() != 0) return of(Etc.divideFloor(xi, yi));
        }
      }
      case DIVIDE_TRUNCATE -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null && yi.signum() != 0) return of(xi.divide(yi));
        }
      }
      case REMAINDER_EUCLIDEAN -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null && yi.signum() != 0) return of(Etc.remainderEuclidean(xi, yi));
        }
      }
      case REMAINDER_FLOOR -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null && yi.signum() != 0) return of(Etc.remainderFloor(xi, yi));
        }
      }
      case REMAINDER_TRUNCATE -> {
        var x = a.get(0);
        var y = a.get(1);

        var xi = x.integerValue();
        if (xi != null) {
          var yi = y.integerValue();
          if (yi != null && yi.signum() != 0) return of(xi.remainder(yi));
        }
      }
    }
    return a;
  }

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
