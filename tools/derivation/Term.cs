using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text;
using System.Threading.Tasks;

namespace derivation
{
    public enum Tag
    {
        FALSE,
        TRUE,
        DISTINCT_OBJECT,
        VAR,
        GLOBAL_VAR,
        CAST,
        CALL,
        INTEGER,
        RATIONAL,
        ALL,
        FUNC,
        EXISTS,
        AND,
        OR,
        NOT,
        EQV,
        CEILING,
        FLOOR,
        ROUND,
        TRUNCATE,
        NEGATE,
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
        IS_INTEGER,
        IS_RATIONAL,
        EQUALS,
        LESS,
        LESS_EQUALS,
    }

    public abstract class Term : IReadOnlyList<Term>
    {
        public virtual bool contains(Var b)
        {
            foreach (var a in this)
                if (a.contains(b))
                    return true;
            return false;
        }

        public virtual bool contains(FMap map, Var b)
        {
            foreach (var a in this)
                if (a.contains(map, b))
                    return true;
            return false;
        }

        public virtual FMap match(FMap map, Term b)
        {
            if (map == null) throw new ArgumentNullException(nameof(map));
            if (Equals(b)) return map;
            if (!Type.Equals(b.Type)) return null;

            if (Tag != b.Tag) return null;
            var n = Count;
            if (n == 0) return null;

            if (n != b.Count) return null;
            for (var i = 0; i < n; i++)
            {
                map = this[i].match(map, b[i]);
                if (map == null) break;
            }
            return map;
        }

        public virtual FMap unify(FMap map, Term b)
        {
            if (map == null) throw new ArgumentNullException(nameof(map));
            if (Equals(b)) return map;
            if (!Type.Equals(b.Type)) return null;

            if (b is Var b1) return b1.unify(map, this);

            if (Tag != b.Tag) return null;
            var n = Count;
            if (n == 0) return null;

            if (n != b.Count) return null;
            for (var i = 0; i < n; i++)
            {
                map = this[i].unify(map, b[i]);
                if (map == null) break;
            }
            return map;
        }

        public abstract Tag Tag { get; }

        public virtual Term this[int i]
        {
            get => throw new NotImplementedException(ToString());
        }

        public virtual Type Type
        {
            get
            {
                switch (Tag)
                {
                    case Tag.NOT:
                    case Tag.LESS:
                    case Tag.LESS_EQUALS:
                    case Tag.OR:
                    case Tag.AND:
                    case Tag.EQUALS:
                    case Tag.EQV:
                    case Tag.EXISTS:
                    case Tag.ALL:
                    case Tag.IS_INTEGER:
                    case Tag.IS_RATIONAL:
                        return Type.Bool;
                    case Tag.ADD:
                    case Tag.SUBTRACT:
                    case Tag.MULTIPLY:
                    case Tag.DIVIDE:
                    case Tag.CEILING:
                    case Tag.FLOOR:
                    case Tag.ROUND:
                    case Tag.TRUNCATE:
                    case Tag.NEGATE:
                    case Tag.DIVIDE_EUCLIDEAN:
                    case Tag.DIVIDE_FLOOR:
                    case Tag.DIVIDE_TRUNCATE:
                    case Tag.REMAINDER_EUCLIDEAN:
                    case Tag.REMAINDER_FLOOR:
                    case Tag.REMAINDER_TRUNCATE:
                        return this[0].Type;
                    case Tag.CALL:
                        return ((Function)this[0]).returnType!;
                    case Tag.FALSE:
                    case Tag.TRUE:
                    case Tag.DISTINCT_OBJECT:
                    case Tag.VAR:
                    case Tag.GLOBAL_VAR:
                    case Tag.CAST:
                    case Tag.INTEGER:
                    case Tag.RATIONAL:
                    case Tag.FUNC:
                        break;
                }
                throw new ArgumentException(ToString());
            }
        }

        public virtual int Count => 0;

        sealed class FalseTerm : Term
        {
            public override Tag Tag => Tag.FALSE;

            public override Type Type => Type.Bool;
        }

        public static readonly Term False = new FalseTerm();

        sealed class TrueTerm : Term
        {
            public override Tag Tag => Tag.TRUE;

            public override Type Type => Type.Bool;
        }

        public static readonly Term True = new TrueTerm();

        sealed class Term1 : Term
        {
            readonly Tag tag;
            readonly Term a;

            public Term1(Tag tag, Term a)
            {
                this.tag = tag;
                this.a = a;
            }

            public override Tag Tag => tag;

            public override int Count => 1;

            public override Term this[int i]
            {
                get
                {
                    if (i == 0) return a;
                    throw new ArgumentOutOfRangeException(i.ToString());
                }
            }

            public override IEnumerator<Term> GetEnumerator()
            {
                yield return a;
            }

            public override bool Equals(object obj)
            {
                if (obj is Term1 o)
                    return tag.Equals(o.tag) && a.Equals(o.a);
                return false;
            }

            public override int GetHashCode()
            {
                return HashCode.Combine(tag.GetHashCode(), a.GetHashCode());
            }
        }

        sealed class Term2 : Term
        {
            readonly Tag tag;
            readonly Term a, b;

            public Term2(Tag tag, Term a, Term b)
            {
                this.tag = tag;
                this.a = a;
                this.b = b;
            }

            public override Tag Tag => tag;

            public override int Count => 2;

            public override Term this[int i]
            {
                get
                {
                    return i switch
                    {
                        0 => a,
                        1 => b,
                        _ => throw new ArgumentOutOfRangeException(i.ToString()),
                    };
                }
            }

            public override IEnumerator<Term> GetEnumerator()
            {
                yield return a;
                yield return b;
            }

            public override bool Equals(object obj)
            {
                if (obj is Term2 o)
                    return tag.Equals(o.tag) && a.Equals(o.a) && b.Equals(o.b);
                return false;
            }

            public override int GetHashCode()
            {
                return HashCode.Combine(tag.GetHashCode(), a.GetHashCode(), b.GetHashCode());
            }
        }

        sealed class Term3 : Term
        {
            readonly Tag tag;
            readonly Term a, b, c;

            public Term3(Tag tag, Term a, Term b, Term c)
            {
                this.tag = tag;
                this.a = a;
                this.b = b;
                this.c = c;
            }

            public override Tag Tag => tag;

            public override int Count => 3;

            public override Term this[int i]
            {
                get
                {
                    return i switch
                    {
                        0 => a,
                        1 => b,
                        2 => c,
                        _ => throw new ArgumentOutOfRangeException(i.ToString()),
                    };
                }
            }

            public override IEnumerator<Term> GetEnumerator()
            {
                yield return a;
                yield return b;
                yield return c;
            }

            public override bool Equals(object obj)
            {
                if (obj is Term3 o)
                    return tag.Equals(o.tag) && a.Equals(o.a) && b.Equals(o.b) && c.Equals(o.c);
                return false;
            }

            public override int GetHashCode()
            {
                return HashCode.Combine(tag.GetHashCode(), a.GetHashCode(), b.GetHashCode(), c.GetHashCode());
            }
        }

        sealed class Terms : Term
        {
            readonly Tag tag;
            readonly Term[] v;

            public Terms(Tag tag, Term[] v)
            {
                this.tag = tag;
                this.v = v;
            }

            public override Tag Tag => tag;

            public override int Count => v.Length;

            public override Term this[int i] => v[i];

            public override IEnumerator<Term> GetEnumerator()
            {
                return ((IEnumerable<Term>)v).GetEnumerator();
            }

            public override bool Equals(object obj)
            {
                if (obj is Terms o)
                    return tag.Equals(o.tag) && v.SequenceEqual(o.v);
                return false;
            }

            public override int GetHashCode()
            {
                return HashCode.Combine(tag.GetHashCode(), v.GetHashCode());
            }
        }

        public static Term Of(Tag tag, Term a)
        {
            return new Term1(tag, a);
        }

        public static Term Of(Tag tag, Term a, Term b)
        {
            return new Term2(tag, a, b);
        }

        public static Term Of(Tag tag, Term a, Term b, Term c)
        {
            return new Term3(tag, a, b, c);
        }

        public static Term Of(Tag tag, Term[] v)
        {
            return v.Length switch
            {
                0 => throw new ArgumentException(tag.ToString()),
                1 => new Term1(tag, v[0]),
                2 => new Term2(tag, v[0], v[1]),
                3 => new Term3(tag, v[0], v[1], v[2]),
                _ => new Terms(tag, v),
            };
        }

        public static Term Of(Tag tag, Term a, Term[] v)
        {
            var w = new Term[1 + v.Length];
            w[0] = a;
            Array.Copy(v, 0, w, 1, v.Length);
            return Of(tag, w);
        }

        public virtual Term remake(Term[] v)
        {
            return Of(Tag, v);
        }

        public Term mapLeaves(Func<Term, Term> f)
        {
            var n = Count;
            if (n == 0) return f(this);
            var v = new Term[n];
            for (var i = 0; i < n; i++)
                v[i] = this[i].mapLeaves(f);
            return remake(v);
        }

        public Term replace(FMap map)
        {
            return mapLeaves((a) =>
            {
                var b = map[a];
                if (a.Equals(b)) throw new ArgumentException(nameof(map));
                if (b == null) return a;
                return b.replace(map);
            });
        }

        public virtual IEnumerator<Term> GetEnumerator()
        {
            yield break;
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            throw new NotImplementedException();
        }
    }

    public sealed class IntegerTerm : Term
    {
        public override Tag Tag => Tag.INTEGER;

        public override Type Type => Type.Integer;

        readonly BigInteger value;

        public IntegerTerm(BigInteger value)
        {
            this.value = value;
        }

        public override bool Equals(object obj)
        {
            if (obj is IntegerTerm o)
                return value.Equals(o.value);
            return false;
        }

        public override int GetHashCode()
        {
            return value.GetHashCode();
        }

        public override string ToString()
        {
            return value.ToString();
        }
    }

    public sealed class RationalTerm : Term
    {
        public override Tag Tag => Tag.RATIONAL;

        public override Type Type => type;

        readonly Type type;
        readonly BigRational value;

        public RationalTerm(Type type, BigRational value)
        {
            this.type = type;
            this.value = value;
        }

        public override bool Equals(object obj)
        {
            if (obj is RationalTerm o)
                return value.Equals(o.value);
            return false;
        }

        public override int GetHashCode()
        {
            return value.GetHashCode();
        }

        public override string ToString()
        {
            return value.ToString();
        }
    }

    public abstract class Global : Term
    {
        string name;

        protected Global(string name)
        {
            this.name = name;
        }
    }

    public sealed class GlobalVar : Global
    {
        Type type;

        public GlobalVar(string name) : base(name)
        {
        }

        public GlobalVar(string name, Type type) : base(name)
        {
            this.type = type;
        }

        public override Tag Tag => Tag.GLOBAL_VAR;

        public override Type Type => type;
    }

    public sealed class Function : Global
    {
        public override Tag Tag => Tag.FUNC;

        public Type returnType;
        public Type[] prms;

        public Function(string name) : base(name)
        {
        }

        public Function(string name, Type returnType, params Type[] prms) : base(name)
        {
            this.returnType = returnType;
            this.prms = prms;
        }

        public override Type Type
        {
            get
            {
                var v = new Type[1 + prms.Length];
                v[0] = returnType;
                Array.Copy(prms, 0, v, 1, prms.Length);
                return Type.Of(Kind.Func, v);
            }
        }

        public Term call(params Term[] args)
        {
            return Of(Tag.CALL, this, args);
        }
    }

    public sealed class Var : Term
    {
        public override FMap match(FMap map, Term b)
        {
            if (map == null) throw new ArgumentNullException(nameof(map));
            if (this == b) return map;
            if (!Type.Equals(b.Type)) return null;

            var a1 = map[this];
            if (a1 != null) return a1.Equals(b) ? map : null;
            return map.Add(this, b);
        }

        public override FMap unify(FMap map, Term b)
        {
            if (map == null) throw new ArgumentNullException(nameof(map));
            if (this == b) return map;
            if (!Type.Equals(b.Type)) return null;

            var a1 = map[this];
            if (a1 != null) return a1.unify(map, b);

            var b1 = map[b];
            if (b1 != null) return unify(map, b1);

            if (b.contains(map, this)) return null;
            return map.Add(this, b);
        }

        public override Tag Tag => Tag.VAR;

        readonly Type type;

        public override Type Type => type;

        public Var(Type type)
        {
            this.type = type;
        }

        public override bool contains(FMap map, Var b)
        {
            if (this == b) return true;
            var a = map[this];
            return a != null && a.contains(map, b);
        }

        public override bool contains(Var b)
        {
            return this == b;
        }
    }

    public sealed class DistinctObject : Term
    {
        public override Tag Tag => Tag.DISTINCT_OBJECT;

        public override Type Type => Type.INDIVIDUAL;

        readonly string name;

        public DistinctObject(string name)
        {
            this.name = name;
        }

        public override string ToString()
        {
            return '"' + name + '"';
        }
    }
}
