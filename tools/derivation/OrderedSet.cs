using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace derivation
{
    public sealed class OrderedSet<T>
    {
        readonly List<T> v = new();

        public void Add(T a)
        {
            if (!v.Contains(a))
                v.Add(a);
        }
    }
}
