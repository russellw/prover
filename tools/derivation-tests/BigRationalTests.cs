using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;

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
        }
    }
}
