using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace derivation
{
    public sealed class FMap
    {
        public static readonly FMap Empty = new FMap(null, null, null);
        readonly FMap parent;
        readonly Term key;
        readonly Term value;

        FMap() { }

        FMap(FMap parent, Term key, Term value)
        {
            this.parent = parent;
            this.key = key;
            this.value = value;
        }

        public FMap Add(Term key, Term value)
        {
            return new FMap(this, key, value);
        }

        public Term this[Term key]
        {
            get
            {
                for (var map = this; map != Empty; map = map.parent)
                    if (map.key == key)
                        return map.value;
                return null;
            }
        }
    }
}
