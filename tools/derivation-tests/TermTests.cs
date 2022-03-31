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
            Assert.AreEqual(Type.INDIVIDUAL, new Var(Type.INDIVIDUAL).Type);
            Assert.AreEqual(Type.INDIVIDUAL, new DistinctObject("abc").Type);
            Assert.AreEqual(Type.INDIVIDUAL, new GlobalVar("abc", Type.INDIVIDUAL).Type);
            Assert.AreEqual(Type.Integer, new IntegerTerm(5).Type);
            Assert.AreEqual(Type.Rational, new RationalTerm(Type.Rational, new BigRational(1, 5)).Type);
            Assert.AreEqual(Type.Real, new RationalTerm(Type.Real, new BigRational(1, 5)).Type);
            Assert.AreEqual(Type.Of(Kind.Func, Type.Bool, Type.Real), new Function("p", Type.Bool, Type.Real).Type);
            Assert.AreEqual(Type.Integer, Term.Of(Tag.ADD, new IntegerTerm(5), new IntegerTerm(5)).Type);

            var x = new Var(Type.INDIVIDUAL);
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

        [TestMethod]
        public void Match()
        {
            // Subset of unify.
            // Gives different results in several cases;
            // in particular, has no notion of an occurs check.
            // Assumes the inputs have disjoint variables
            var a = new GlobalVar("a", Type.INDIVIDUAL);
            var b = new GlobalVar("b", Type.INDIVIDUAL);
            var f1 = new Function("f1", Type.INDIVIDUAL, Type.INDIVIDUAL);
            var f2 = new Function("f2", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);
            var g1 = new Function("g1", Type.INDIVIDUAL, Type.INDIVIDUAL);
            var x = new Var(Type.INDIVIDUAL);
            var y = new Var(Type.INDIVIDUAL);
            var z = new Var(Type.INDIVIDUAL);
            FMap map;

            // Succeeds. (tautology)
            map = a.match(FMap.EMPTY, a);
            Assert.IsNotNull(map);
            Assert.AreSame(map, FMap.EMPTY);

            // a and b do not match
            map = a.match(FMap.EMPTY, b);
            Assert.IsNull(map);

            // Succeeds. (tautology)
            map = x.match(FMap.EMPTY, x);
            Assert.IsNotNull(map);
            Assert.AreSame(map, FMap.EMPTY);

            // a and x do not match
            map = a.match(FMap.EMPTY, x);
            Assert.IsNull(map);

            // x and y are aliased
            map = x.match(FMap.EMPTY, y);
            Assert.IsNotNull(map);
            Assert.AreNotSame(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), y);

            // Function and constant symbols match, x is unified with the constant b
            map = f2.call(a, x).match(FMap.EMPTY, f2.call(a, b));
            Assert.IsNotNull(map);
            Assert.AreNotSame(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), b);

            // f and g do not match
            map = f1.call(a).match(FMap.EMPTY, g1.call(a));
            Assert.IsNull(map);

            // x and y are aliased
            map = f1.call(x).match(FMap.EMPTY, f1.call(y));
            Assert.IsNotNull(map);
            Assert.AreNotSame(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), y);

            // f and g do not match
            map = f1.call(x).match(FMap.EMPTY, g1.call(y));
            Assert.IsNull(map);

            // Fails. The f function symbols have different arity
            map = f1.call(x).match(FMap.EMPTY, f2.call(y, z));
            Assert.IsNull(map);

            // g(x) and y do not match
            map = f1.call(g1.call(x)).match(FMap.EMPTY, f1.call(y));
            Assert.IsNull(map);

            // g(x) and y do not match
            map = f2.call(g1.call(x), x).match(FMap.EMPTY, f2.call(y, a));
            Assert.IsNull(map);
        }

        [TestMethod]
        public void Unify()
        {
            // https://en.wikipedia.org/wiki/Unification_(computer_science)#Examples_of_syntactic_unification_of_first-order_terms
            var a = new GlobalVar("a", Type.INDIVIDUAL);
            var b = new GlobalVar("b", Type.INDIVIDUAL);
            var f1 = new Function("f1", Type.INDIVIDUAL, Type.INDIVIDUAL);
            var f2 = new Function("f2", Type.INDIVIDUAL, Type.INDIVIDUAL, Type.INDIVIDUAL);
            var g1 = new Function("g1", Type.INDIVIDUAL, Type.INDIVIDUAL);
            var x = new Var(Type.INDIVIDUAL);
            var y = new Var(Type.INDIVIDUAL);
            var z = new Var(Type.INDIVIDUAL);
            FMap map;

            // Succeeds. (tautology)
            map = a.unify(FMap.EMPTY, a);
            Assert.IsNotNull(map);
            Assert.AreEqual(map, FMap.EMPTY);

            // a and b do not match
            map = a.unify(FMap.EMPTY, b);
            Assert.IsNull(map);

            // Succeeds. (tautology)
            map = x.unify(FMap.EMPTY, x);
            Assert.IsNotNull(map);
            Assert.AreEqual(map, FMap.EMPTY);

            // x is unified with the constant a
            map = a.unify(FMap.EMPTY, x);
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), a);

            // x and y are aliased
            map = x.unify(FMap.EMPTY, y);
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), y.replace(map));

            // Function and constant symbols match, x is unified with the constant b
            map = f2.call(a, x).unify(FMap.EMPTY, f2.call(a, b));
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), b);

            // f and g1 do not match
            map = f1.call(a).unify(FMap.EMPTY, g1.call(a));
            Assert.IsNull(map);

            // x and y are aliased
            map = f1.call(x).unify(FMap.EMPTY, f1.call(y));
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), y.replace(map));

            // f and g1 do not match
            map = f1.call(x).unify(FMap.EMPTY, g1.call(y));
            Assert.IsNull(map);

            // Fails. The f function symbols have different arity
            map = f1.call(x).unify(FMap.EMPTY, f2.call(y, z));
            Assert.IsNull(map);

            // Unifies y with the term g1(x)
            map = f1.call(g1.call(x)).unify(FMap.EMPTY, f1.call(y));
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(y.replace(map), g1.call(x));

            // Unifies x with constant a, and y with the term g1(a)
            map = f2.call(g1.call(x), x).unify(FMap.EMPTY, f2.call(y, a));
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), a);
            Assert.AreEqual(y.replace(map), g1.call(a));

            // Returns false in first-order logic and many modern Prolog dialects (enforced by the occurs
            // check).
            map = x.unify(FMap.EMPTY, f1.call(x));
            Assert.IsNull(map);

            // Both x and y are unified with the constant a
            map = x.unify(FMap.EMPTY, y);
            map = y.unify(map, a);
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), a);
            Assert.AreEqual(y.replace(map), a);

            // As above (order of equations in set doesn't matter)
            map = a.unify(FMap.EMPTY, y);
            map = x.unify(map, y);
            Assert.IsNotNull(map);
            Assert.AreNotEqual(map, FMap.EMPTY);
            Assert.AreEqual(x.replace(map), a);
            Assert.AreEqual(y.replace(map), a);

            // Fails. a and b do not match, so x can't be unified with both
            map = x.unify(FMap.EMPTY, a);
            Assert.IsNotNull(map);
            map = b.unify(map, x);
            Assert.IsNull(map);
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
