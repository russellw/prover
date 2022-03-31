using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace derivation
{
    public struct BigRational : IComparable, IComparable<BigRational>, IEquatable<BigRational>
    {
        public static readonly BigRational Zero = new(BigInteger.Zero);
        public static readonly BigRational One = new(BigInteger.One);
        public readonly BigInteger num, den;

        public BigRational(BigInteger num)
        {
            this.num = num;
            den = BigInteger.One;
        }

        public BigRational(BigInteger num, BigInteger den)
        {
            switch (den.Sign)
            {
                case -1:
                    num = -num;
                    den = -den;
                    break;
                case 0: throw new DivideByZeroException();
            }
            var g = BigInteger.GreatestCommonDivisor(num, den);
            this.num = num / g;
            this.den = den / g;
        }

        public static BigRational operator+(BigRational a, BigRational b)
        {
            return new BigRational(a.num * b.den + b.num * a.den,a. den * b.den);
        }

        public BigInteger Ceiling()
        {
            return Etc.divideFloor(num + den - 1, den);
        }

        public static BigRational operator/(BigRational a, BigRational b)
        {
            return new BigRational(a.num * b.den, a.den * b.num);
        }

        public BigInteger Floor()
        {
            return Etc.divideFloor(num, den);
        }

        public static BigRational operator*(BigRational a, BigRational b)
        {
            return new BigRational(a.num * b.num,a. den * b.den);
        }

        public static BigRational operator-(BigRational a)
        {
            return new BigRational(-a.num, a.den);
        }

        public static BigRational ParseDecimal(String s)
        {
            Match m;

            // Integer
            m = Regex.Match(s, @"^([+-]?\d+)$");
            if (m.Success)
            {
                var num = BigInteger.Parse(m.Groups[1].Value);
                return new BigRational(num);
            }

            // Decimal
            m = Regex.Match(s, @"^([+-]?\d+)\.(\d+)$");
            if (m.Success)
            {
                var den = BigInteger.Pow(10, m.Groups[2].Value.Length);
                var whole = BigInteger.Parse(m.Groups[1].Value) * den;
                var fraction = BigInteger.Parse(m.Groups[2].Value);
                var num = whole >= 0 ? whole + fraction : whole - fraction;
                return new BigRational(num, den);
            }

            // Exponent
            m = Regex.Match(s, @"^([+-]?\d+)[eE]([+-]?\d+)$");
            if (m.Success)
            {
                var num = BigInteger.Parse(m.Groups[1].Value);
                var den = BigInteger.One;
                var exponent = int.Parse(m.Groups[2].Value);
                if (exponent > 0)
                    num *= BigInteger.Pow(10, exponent);
                else
                    den *= BigInteger.Pow(10, -exponent);
                return new BigRational(num, den);
            }

            // Decimal exponent
            m = Regex.Match(s, @"^([+-]?\d+)\.(\d+)[eE]([+-]?\d+)$");
            if (m.Success)
            {
                var den = BigInteger.Pow(10, m.Groups[2].Value.Length);
                var whole = BigInteger.Parse(m.Groups[1].Value) * den;
                var fraction = BigInteger.Parse(m.Groups[2].Value);
                var num = whole.Sign >= 0 ? whole + fraction : whole - fraction;
                var exponent = int.Parse(m.Groups[3].Value);
                if (exponent > 0)
                    num *= BigInteger.Pow(10, exponent);
                else
                    den *= BigInteger.Pow(10, -exponent);
                return new BigRational(num, den);
            }

            // None of the above
            throw new FormatException(s);
        }

        public static BigRational Parse(String s)
        {
            BigInteger num, den;
            var i = s.IndexOf('/');
            if (i < 0)
            {
                num = BigInteger.Parse(s);
                den = 1;
            }
            else
            {
                num = BigInteger.Parse(s.Substring(0, i));
                den = BigInteger.Parse(s.Substring(i + 1));
            }
            return new(num, den);
        }

        public int Sign => num.Sign;

        public BigInteger Round()
        {
            var n = num * 2 + den;
            var d = den * 2;
            n = Etc.divideFloor(n, d);
            if (!num.IsEven && den == 2 && !n.IsEven)
                n--;
            return n;
        }

        public static BigRational operator-(BigRational a, BigRational b)
        {
            return new BigRational(a.num * b.den - b.num *a. den,a. den * b.den);
        }

        public override string ToString()
        {
            return num.ToString() + '/' + den;
        }

        public BigInteger Truncate()
        {
            return num / den;
        }

        public override bool Equals(object obj)
        {
            if (obj is not BigRational)
                return false;
            return Equals((BigRational)obj);
        }

        public override int GetHashCode()
        {
            return HashCode.Combine(num.GetHashCode(), den.GetHashCode());
        }

        public bool Equals(BigRational other)
        {
            return num.Equals(other.num) && den.Equals(other.den);
        }

        public int CompareTo(object obj)
        {
            if (obj == null)
                return 1;
            if (obj is not BigRational)
                throw new ArgumentException(obj.ToString());
            return CompareTo((BigRational)obj);
        }

        public int CompareTo(BigRational other)
        {
            return (num * other.den).CompareTo(other.num * den);
        }

        public static bool operator ==(BigRational a,BigRational b)
        {
            return a.Equals(b);
        }

        public static bool operator !=(BigRational a, BigRational b)
        {
            return !a.Equals(b);
        }

        public static bool operator <(BigRational a, BigRational b)
        {
            return a.CompareTo(b)<0;
        }

        public static bool operator <=(BigRational a, BigRational b)
        {
            return a.CompareTo(b) <= 0;
        }

        public static bool operator >(BigRational a, BigRational b)
        {
            return a.CompareTo(b) > 0;
        }

        public static bool operator >=(BigRational a, BigRational b)
        {
            return a.CompareTo(b) >= 0;
        }
    }
}
