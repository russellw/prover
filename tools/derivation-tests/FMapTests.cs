﻿using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace derivation_tests
{
    [TestClass]
    public class FMapTests
    {
        [TestMethod]
        public void TestMethod1()
        {
            var a = new GlobalVar("a",Type.Individual);
            var b = new GlobalVar("b",Type.Individual);
            var x=new Var(Type.Individual);
            var y=new Var(Type.Individual);
            FMap map;

            map = FMap.Empty;
            Assert.AreSame(null, map[x]);
            //Assert.AreEqual(null, map[x]);
        }
    }
}
