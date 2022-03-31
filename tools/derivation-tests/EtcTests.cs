using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;

namespace derivation_tests
{
    [TestClass]
    public class EtcTests
    {
        [TestMethod]
        public void DivideEuclidean()
        {
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(0), new BigInteger(1)), new BigInteger(0));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(0), new BigInteger(10)), new BigInteger(0));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(0), new BigInteger(-1)), new BigInteger(0));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(0), new BigInteger(-10)), new BigInteger(0));

            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(1), new BigInteger(1)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(10), new BigInteger(10)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(-1), new BigInteger(1)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(-10), new BigInteger(10)),
                new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(1), new BigInteger(-1)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(10), new BigInteger(-10)),
                new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(-1), new BigInteger(-1)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(-10), new BigInteger(-10)),
                new BigInteger(1));

            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(7), new BigInteger(3)), new BigInteger(2));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(7), new BigInteger(-3)), new BigInteger(-2));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(-7), new BigInteger(3)), new BigInteger(-3));
            Assert.AreEqual(
                Etc.divideEuclidean(new BigInteger(-7), new BigInteger(-3)), new BigInteger(3));
        }

        [TestMethod]
        public void RemainderEuclidean()
        {
            Assert.AreEqual(
                Etc.remainderEuclidean(new BigInteger(1), new BigInteger(1)),
                new BigInteger(0));
            Assert.AreEqual(
                Etc.remainderEuclidean(new BigInteger(10), new BigInteger(10)),
                new BigInteger(0));

            Assert.AreEqual(
                Etc.remainderEuclidean(new BigInteger(7), new BigInteger(3)),
                new BigInteger(1));
            Assert.AreEqual(
                Etc.remainderEuclidean(new BigInteger(7), new BigInteger(-3)),
                new BigInteger(1));
            Assert.AreEqual(
                Etc.remainderEuclidean(new BigInteger(-7), new BigInteger(3)),
                new BigInteger(2));
            Assert.AreEqual(
                Etc.remainderEuclidean(new BigInteger(-7), new BigInteger(-3)),
                new BigInteger(2));
        }

        [TestMethod]
        public void DivideFloor()
        {
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(0), new BigInteger(1)), new BigInteger(0));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(0), new BigInteger(10)), new BigInteger(0));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(0), new BigInteger(-1)), new BigInteger(0));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(0), new BigInteger(-10)), new BigInteger(0));

            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(1), new BigInteger(1)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(10), new BigInteger(10)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(-1), new BigInteger(1)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(-10), new BigInteger(10)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(1), new BigInteger(-1)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(10), new BigInteger(-10)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(-1), new BigInteger(-1)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(-10), new BigInteger(-10)), new BigInteger(1));

            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(5), new BigInteger(3)), new BigInteger(1));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(5), new BigInteger(-3)), new BigInteger(-2));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(-5), new BigInteger(3)), new BigInteger(-2));
            Assert.AreEqual(
                Etc.divideFloor(new BigInteger(-5), new BigInteger(-3)), new BigInteger(1));
        }

        [TestMethod]
        public void RemainderFloor()
        {
            Assert.AreEqual(
                Etc.remainderFloor(new BigInteger(1), new BigInteger(1)), new BigInteger(0));
            Assert.AreEqual(
                Etc.remainderFloor(new BigInteger(10), new BigInteger(10)), new BigInteger(0));

            Assert.AreEqual(
                Etc.remainderFloor(new BigInteger(5), new BigInteger(3)), new BigInteger(2));
            Assert.AreEqual(
                Etc.remainderFloor(new BigInteger(5), new BigInteger(-3)), new BigInteger(-1));
            Assert.AreEqual(
                Etc.remainderFloor(new BigInteger(-5), new BigInteger(3)), new BigInteger(1));
            Assert.AreEqual(
                Etc.remainderFloor(new BigInteger(-5), new BigInteger(-3)), new BigInteger(-2));
        }

        [TestMethod]
        public void CartesianProduct()
        {
            List<List<string>> qs = new();
            List<string> q;
            q = new();
            q.Add("a0");
            q.Add("a1");
            qs.Add(q);
            q = new();
            q.Add("b0");
            q.Add("b1");
            q.Add("b2");
            qs.Add(q);
            q = new();
            q.Add("c0");
            q.Add("c1");
            q.Add("c2");
            q.Add("c3");
            qs.Add(q);
            var rs = Etc.cartesianProduct(qs);
            var i = 0;
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b0", "c0" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b0", "c1" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b0", "c2" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b0", "c3" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b1", "c0" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b1", "c1" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b1", "c2" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b1", "c3" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b2", "c0" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b2", "c1" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b2", "c2" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a0", "b2", "c3" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b0", "c0" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b0", "c1" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b0", "c2" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b0", "c3" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b1", "c0" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b1", "c1" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b1", "c2" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b1", "c3" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b2", "c0" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b2", "c1" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b2", "c2" })));
            Assert.IsTrue(rs[i++].SequenceEqual(new List<string>(new string[] { "a1", "b2", "c3" })));
        }
    }
}
