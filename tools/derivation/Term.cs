using System;
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

    public abstract class Term
    {
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
                        return ((Func)this[0]).returnType!;
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

            public override Term this[int i]
            {
                get
                {
                    if (i == 0) return a;
                    throw new ArgumentOutOfRangeException(i.ToString());
                }
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

            public override Term this[int i] => v[i];

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

    public sealed class Func : Global
    {
        public override Tag Tag => Tag.FUNC;

        public Type returnType;
        public Type[] prms;

        public Func(string name) : base(name)
        {
        }

        public Func(string name, Type returnType, params Type[] prms) : base(name)
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
    }

    public sealed class Var : Term
    {
        public override Tag Tag => Tag.VAR;

        readonly Type type;

        public override Type Type => type;

        public Var(Type type)
        {
            this.type = type;
        }
    }

    public sealed class DistinctObject : Term
    {
        public override Tag Tag => Tag.DISTINCT_OBJECT;

        public override Type Type => Type.Individual;

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
