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
        }
    }
}
