using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text;
using System.Threading.Tasks;

namespace derivation
{
    public static class Etc
   {
        public static bool isDigit(int c)
        {
            return '0' <= c && c <= '9';
        }

        public static bool isUpper(int c)
        {
            return 'A' <= c && c <= 'Z';
        }

        public static bool isAlpha(int c)
        {
            return isLower(c) || isUpper(c);
        }

        public static bool isIdPart(int c)
        {
            return isAlnum(c) || c == '_';
        }

        public static bool isAlnum(int c)
        {
            return isAlpha(c) || isDigit(c);
        }

        public static bool isLower(int c)
        {
            return 'a' <= c && c <= 'z';
        }

        public static  List<List<T>> cartesianProduct<T>(List<List<T>> qs)
        {
            var js = new int[qs.Count];
            var rs = new List<List<T>>();
            cartesianProduct(qs, 0, js, rs);
            return rs;
        }

        private static  void cartesianProduct<T>(List<List<T>> qs, int i, int[] js, List<List<T>> rs)
        {
            if (i == js.Length)
            {
                var ys = new List<T>();
                for (i = 0; i < js.Length; i++) ys.Add(qs[i][js[i]]);
                rs.Add(ys);
                return;
            }
            for (js[i] = 0; js[i] < qs[i].Count; js[i]++) cartesianProduct(qs, i + 1, js, rs);
        }

        public static BigInteger divideEuclidean(BigInteger a, BigInteger b)
        {
            var q = a / b;
            if (a < 0 && q * b != a) q -= b.Sign;
            return q;
        }

        public static BigInteger divideFloor(BigInteger a, BigInteger b)
        {
            BigInteger r;
            var q = BigInteger.DivRem(a, b,out r);
            if (a < 0 != b < 0 && r != 0) q--;
            return q;
        }

        public static BigInteger remainderEuclidean(BigInteger a, BigInteger b)
        {
            var r = a%b;
            if (r < 0) r +=BigInteger.Abs(b);
            return r;
        }

        public static BigInteger remainderFloor(BigInteger a, BigInteger b)
        {
            return a-(divideFloor(a, b)*b);
        }
    }
}
