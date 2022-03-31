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
                    default:
                        throw new ArgumentException(ToString());
                }
            }
        }

        sealed class FalseTerm : Term
        {
            public override Tag Tag => Tag.FALSE;
        }

        public static readonly Term False = new FalseTerm();

        sealed class TrueTerm : Term
        {
            public override Tag Tag => Tag.TRUE;
        }

        public static readonly Term True = new TrueTerm();
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
            if (obj is not IntegerTerm)
                return false;
            return Equals((IntegerTerm)obj);
        }

        public override int GetHashCode()
        {
            return value.GetHashCode();
        }

        public bool Equals(IntegerTerm other)
        {
            return value.Equals(other.value);
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
            if (obj is not RationalTerm)
                return false;
            return Equals((RationalTerm)obj);
        }

        public override int GetHashCode()
        {
            return value.GetHashCode();
        }

        public bool Equals(RationalTerm other)
        {
            return value.Equals(other.value);
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
        public Type[] parms;

        public Func(string name) : base(name)
        {
        }

        public override Type Type
        {
            get
            {
                var v = new Type[1 + parms!.Length];
                v[0] = returnType!;
                Array.Copy(parms!, 0, v, 1, parms!.Length);
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
