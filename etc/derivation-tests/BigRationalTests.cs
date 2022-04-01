using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Numerics;

namespace derivation_tests
{
    [TestClass]
    public class BigRationalTests
    {
        [TestMethod]
        public void BigRationalTest()
        {
            Assert.AreEqual(BigRational.Parse("123/1"), new BigRational(123, 1));
            Assert.AreEqual(BigRational.Parse("+123/1"), new BigRational(123, 1));
            Assert.AreEqual(BigRational.Parse("-123/1"), new BigRational(-123, 1));
            Assert.AreEqual(BigRational.Parse("123/456"), new BigRational(123, 456));

            Assert.AreEqual(new BigRational(5, 10), new BigRational(1, 2));
            Assert.AreEqual(new BigRational(-5, 10), new BigRational(-1, 2));
            Assert.AreEqual(new BigRational(5, -10), new BigRational(-1, 2));
            Assert.AreEqual(new BigRational(-5, -10), new BigRational(1, 2));

            Assert.IsTrue(new BigRational(1, 3).Equals(new BigRational(1, 3)));
            Assert.IsTrue(new BigRational(1, 3) == new BigRational(1, 3));
            Assert.IsFalse(new BigRational(1, 3).Equals(new BigRational(1, 4)));
            Assert.IsFalse(new BigRational(1, 3).Equals("foo"));
            Assert.IsTrue(new BigRational(1, 3) != new BigRational(1, 4));

            Assert.IsTrue(new BigRational(9, 10).CompareTo(new BigRational(10, 10)) < 0);
            Assert.IsTrue(new BigRational(10, 10).CompareTo(new BigRational(10, 10)) == 0);
            Assert.IsTrue(new BigRational(11, 10).CompareTo(new BigRational(10, 10)) > 0);

            Assert.IsTrue(new BigRational(40) < new BigRational(50));
            Assert.IsFalse(new BigRational(50) < new BigRational(50));
            Assert.IsTrue(new BigRational(40) <= new BigRational(50));
            Assert.IsTrue(new BigRational(50) <= new BigRational(50));
            Assert.IsTrue(new BigRational(60) > new BigRational(50));
            Assert.IsFalse(new BigRational(50) > new BigRational(50));
            Assert.IsTrue(new BigRational(60) >= new BigRational(50));
            Assert.IsTrue(new BigRational(50) >= new BigRational(50));

            Assert.IsTrue(new BigRational(1, 3).Sign > 0);
            Assert.IsTrue(new BigRational(0, 3).Sign == 0);
            Assert.IsTrue(new BigRational(-1, 3).Sign < 0);

            Assert.AreEqual(-new BigRational(1, 2), new BigRational(-1, 2));
            Assert.AreEqual(new BigRational(5, 10) + new BigRational(1, 2), new BigRational(1));
            Assert.AreEqual(new BigRational(1) - new BigRational(3, 1000), new BigRational(997, 1000));
            Assert.AreEqual(new BigRational(1, 3) * new BigRational(1, 4), new BigRational(1, 12));
            Assert.AreEqual(new BigRational(1, 3) / new BigRational(1, 3), new BigRational(1));

            Assert.AreEqual(BigRational.ParseDecimal("0"), BigRational.Zero);
            Assert.AreEqual(BigRational.ParseDecimal("1"), BigRational.One);
            Assert.AreEqual(BigRational.ParseDecimal("10"), new BigRational(10));
            Assert.AreEqual(BigRational.ParseDecimal("100"), new BigRational(100));
            Assert.AreEqual(BigRational.ParseDecimal("100.0"), new BigRational(100));
            Assert.AreEqual(BigRational.ParseDecimal("100.00"), new BigRational(100));
            Assert.AreEqual(BigRational.ParseDecimal("100.01"), new BigRational(10001, 100));
            Assert.AreEqual(BigRational.ParseDecimal("100.10"), new BigRational(10010, 100));
            Assert.AreEqual(BigRational.ParseDecimal("101.00"), new BigRational(10100, 100));
            Assert.AreEqual(BigRational.ParseDecimal("110.00"), new BigRational(11000, 100));
            Assert.AreEqual(BigRational.ParseDecimal("200.00"), new BigRational(20000, 100));

            Assert.AreEqual(BigRational.ParseDecimal("0.0"), new BigRational(0));
            Assert.AreEqual(BigRational.ParseDecimal("1.0"), new BigRational(1));
            Assert.AreEqual(BigRational.ParseDecimal("10.0"), new BigRational(10));
            Assert.AreEqual(BigRational.ParseDecimal("10.5"), new BigRational(105, 10));
            Assert.AreEqual(BigRational.ParseDecimal("10.25"), new BigRational(1025, 100));

            Assert.AreEqual(BigRational.ParseDecimal("123"), new BigRational(123, 1));
            Assert.AreEqual(BigRational.ParseDecimal("+123"), new BigRational(123, 1));
            Assert.AreEqual(BigRational.ParseDecimal("-123"), new BigRational(-123, 1));
            Assert.AreEqual(BigRational.ParseDecimal("123.456"), new BigRational(123456, 1000));
            Assert.AreEqual(BigRational.ParseDecimal("123.456e3"), new BigRational(123456, 1));
            Assert.AreEqual(BigRational.ParseDecimal("123.456e-3"), new BigRational(123456, 1000000));
            Assert.AreEqual(BigRational.ParseDecimal("1e100"), new BigRational(BigInteger.Pow(10, 100)));
            Assert.AreEqual(BigRational.ParseDecimal("5e-1"), new BigRational(1, 2));
        }
    }
}
