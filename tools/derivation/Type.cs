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

    private class BoolType : Type
    {
        public override Kind Kind { get { return Kind.Bool; } }
    }

    public static readonly Type Bool = new BoolType();

    private class IndividualType : Type
    {
        public override Kind Kind { get { return Kind.Individual; } }
    }

    public static readonly Type Individual = new IndividualType();

    private class IntegerType : Type
    {
        public override Kind Kind { get { return Kind.Integer; } }
    }

    public static readonly Type Integer = new IntegerType();

    private class RationalType : Type
    {
        public override Kind Kind { get { return Kind.Rational; } }
    }

    public static readonly Type Rational = new RationalType();

    private class RealType : Type
    {
        public override Kind Kind { get { return Kind.Real; } }
    }

    public static readonly Type Real = new RealType();
}
