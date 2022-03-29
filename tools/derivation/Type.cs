using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public enum Kind
{
    Bool,
    Individual,
    Integer,
    Rational,
    Real,
    Func,
    Opaque,
}

public abstract class Type
{
    public abstract Kind Kind { get; }

    public virtual Type this[int i]=>throw new NotImplementedException();

    class BoolType : Type
    {
        public override Kind Kind => Kind.Bool;
    }

    public static readonly Type Bool = new BoolType();

    class IndividualType : Type
    {
        public override Kind Kind => Kind.Individual;
    }

    public static readonly Type Individual = new IndividualType();

    class IntegerType : Type
    {
        public override Kind Kind => Kind.Integer;
    }

    public static readonly Type Integer = new IntegerType();

    class RationalType : Type
    {
        public override Kind Kind => Kind.Rational;
    }

    public static readonly Type Rational = new RationalType();

    class RealType : Type
    {
        public override Kind Kind => Kind.Real;
    }

    public static readonly Type Real = new RealType();

    public static Type Of(Kind kind,params Type[] v)
    {
        return new Types(kind, v);
    }

    class Types : Type
    {
        readonly Kind kind;
        readonly Type[] v;

        public override Kind Kind => kind;

        public Types(Kind kind, Type[] v)
        {
            this.kind = kind;
            this.v = v;
        }

        public override Type this[int i] => v[i];
    }
}
