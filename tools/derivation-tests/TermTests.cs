using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections.Generic;

namespace derivation_tests
{
    [TestClass]
    public class TermTests
    {
        [TestMethod]
        public void TermTest()
        {
            Assert.AreEqual(Type.Bool, Term.True.Type);
            Assert.AreEqual(Type.Individual, new Var(Type.Individual).Type);
            Assert.AreEqual(Type.Individual, new DistinctObject("abc").Type);
            Assert.AreEqual(Type.Individual, new GlobalVar("abc", Type.Individual).Type);
            Assert.AreEqual(Type.Integer, new IntegerTerm(5).Type);
            Assert.AreEqual(Type.Rational, new RationalTerm(Type.Rational, new BigRational(1, 5)).Type);
            Assert.AreEqual(Type.Real, new RationalTerm(Type.Real, new BigRational(1, 5)).Type);
            Assert.AreEqual(Type.Of(Kind.Func, Type.Bool, Type.Real), new Func("p", Type.Bool, Type.Real).Type);
        }
    }
}
