using System;
using System.Collections.Generic;
using System.Linq;
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
                    case Tag.FUNC:
                        return ((Func)this[0]).returnType!;
                    default:
                        throw new ArgumentException(ToString());
                }
            }
        }

        private class FalseTerm : Term
        {
            public override Tag Tag => Tag.FALSE;
        }

        public static readonly Term False = new FalseTerm();

        private class TrueTerm : Term
        {
            public override Tag Tag => Tag.TRUE;
        }

        public static readonly Term True = new TrueTerm();
    }

    public class Func : Term
    {
        public override Tag Tag => Tag.FUNC;

        public Type? returnType;
        public Type[]? parms;

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

    public class Var : Term
    {
        public override Tag Tag => Tag.VAR;

        readonly Type type;

        public override Type Type => type;

        public Var(Type type)
        {
            this.type = type;
        }
    }
}
