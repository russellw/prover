using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace derivation
{
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

    public abstract class Type : IReadOnlyList<Type>
    {
        public abstract Kind Kind { get; }

        public virtual int Count => 0;

        public virtual Type this[int i] => throw new NotImplementedException();

        sealed class BoolType : Type
        {
            public override Kind Kind => Kind.Bool;
        }

        public static readonly Type Bool = new BoolType();

        sealed class IndividualType : Type
        {
            public override Kind Kind => Kind.Individual;
        }

        public static readonly Type INDIVIDUAL = new IndividualType();

        sealed class IntegerType : Type
        {
            public override Kind Kind => Kind.Integer;
        }

        public static readonly Type Integer = new IntegerType();

        sealed class RationalType : Type
        {
            public override Kind Kind => Kind.Rational;
        }

        public static readonly Type Rational = new RationalType();

        sealed class RealType : Type
        {
            public override Kind Kind => Kind.Real;
        }

        public static readonly Type Real = new RealType();

        public static Type Of(Kind kind, params Type[] v)
        {
            return new Types(kind, v);
        }

        public virtual IEnumerator<Type> GetEnumerator()
        {
            yield break;
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            throw new NotImplementedException();
        }

        sealed class Types : Type
        {
            readonly Kind kind;
            readonly Type[] v;

            public override Kind Kind => kind;

            public override int Count => v.Length;

            public override IEnumerator<Type> GetEnumerator()
            {
                return ((IEnumerable<Type>)v).GetEnumerator();
            }

            public Types(Kind kind, Type[] v)
            {
                this.kind = kind;
                this.v = v;
            }

            public override Type this[int i] => v[i];

            public override bool Equals(object obj)
            {
                if (obj is Types o)
                    return kind.Equals(o.kind) && v.SequenceEqual(o.v);
                return false;
            }

            public override int GetHashCode()
            {
                return v.GetHashCode();
            }
        }
    }
}
