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
            Assert.AreEqual(Type.Integer, Term.Of(Tag.ADD, new IntegerTerm(5), new IntegerTerm(5)).Type);

            var x = new Var(Type.Individual);
            Assert.AreEqual(Type.Bool, Term.Of(Tag.EQUALS, x, x).Type);

            Assert.AreEqual(Term.Of(Tag.EQUALS, x, x), Term.Of(Tag.EQUALS, x, x));
            Assert.AreEqual(Term.Of(Tag.NEGATE, new IntegerTerm(5)), Term.Of(Tag.NEGATE, new IntegerTerm(5)));

            CheckSize(x, 0);
            CheckSize(Term.Of(Tag.NEGATE, new IntegerTerm(5)), 1);
            CheckSize(Term.Of(Tag.EQUALS, x, x), 2);
            CheckSize(Term.Of(Tag.AND, Term.False, Term.False, Term.False), 3);
            CheckSize(Term.Of(Tag.AND, new Term[] { Term.False, Term.False, Term.False }), 3);
            CheckSize(Term.Of(Tag.AND, new Term[] { Term.False, Term.False, Term.False, Term.False }), 4);
        }

        static void CheckSize(Term a, int n)
        {
            Assert.AreEqual(n, a.Count);

            var m = 0;
            foreach (var b in a)
                m++;
            Assert.AreEqual(n, m);
        }
    }
}
