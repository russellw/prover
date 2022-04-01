using derivation;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace derivation_tests
{
    [TestClass]
    public class TypeTests
    {
        [TestMethod]
        public void TypeTest()
        {
            Assert.AreEqual(Type.Bool.Kind, Kind.Bool);
            CheckSize(Type.Bool, 0);
            CheckSize(Type.Of(Kind.Func, Type.Bool, Type.Real), 2);
        }

    static void CheckSize(Type type, int n)
    {
        Assert.AreEqual(n, type.Count);

        var m = 0;
        foreach (var t in type)
            m++;
        Assert.AreEqual(n, m);
    }
    }
}
