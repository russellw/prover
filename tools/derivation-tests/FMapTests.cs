using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace derivation_tests
{
    [TestClass]
    public class FMapTests
    {
        [TestMethod]
        public void FMapTest()
        {
            var a = new GlobalVar("a",Type.INDIVIDUAL);
            var b = new GlobalVar("b",Type.INDIVIDUAL);
            var x=new Var(Type.INDIVIDUAL);
            var y=new Var(Type.INDIVIDUAL);
            FMap map;

            map = FMap.EMPTY;
            Assert.AreSame(null, map[x]);

            map = map.Add(x, a);
            Assert.AreSame(a, map[x]);
            Assert.AreSame(null, map[y]);

            map = map.Add(y, b);
            Assert.AreSame(a, map[x]);
            Assert.AreSame(b, map[y]);
        }
    }
}
