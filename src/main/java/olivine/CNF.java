package olivine;

import java.util.*;

public final class CNF {
  private static final int MANY = 50;
  public final List<Clause> clauses = new ArrayList<>();

  // How many clauses a term will expand into, for the purpose of deciding when subformulas need to
  // be renamed. The answer could
  // exceed the range of a fixed-size integer, but then we don't actually need the number, we only
  // need to know whether it went over
  // the threshold.
  private static int clauseCountAdd(boolean pol, Term a) {
    var n = 0;
    return n;
  }

  // Skolem functions replace existentially quantified variables, also formulas that are renamed to
  // avoid exponential expansion.
  private static Term skolem(Type type, Collection<Term> args) {
    if (args.isEmpty()) return new GlobalVar(null, type);
    var params = new Type[args.size()];
    var i = 0;
    for (var a : args) params[i++] = a.type();
    return new Func(null, type, params).call(args);
  }

  // For-all doesn't need much work to convert. Clauses contain variables with implied for-all. The
  // tricky part is that quantifier
  // binds variables to local scope, so the same variable name used in two for-all's corresponds to
  // two different logical
  // variables. So we rename each quantified variable to a new variable of the same type.
  private Map<Term, Term> all(Map<Term, Term> map, Term a) {
    map = new LinkedHashMap<>(map);
    var n = a.size();
    for (var i = 1; i < n; i++) {
      var x = a.get(i);
      assert x instanceof Var;
      var y = new Var(x.type());
      map.put(x, y);
    }
    return map;
  }

  // Each existentially quantified variable is replaced with a Skolem function whose parameters are
  // all the surrounding
  // universally quantified variables.
  private Map<Term, Term> exists(Map<Term, Term> map, Term a) {
    // Get the surrounding universally quantified variables that will be arguments to the Skolem
    // functions.
    var args = new ArrayList<Term>();
    for (var x : map.values()) if (x instanceof Var) args.add(x);

    // Make a replacement for each existentially quantified variable.
    map = new LinkedHashMap<>(map);
    var n = a.size();
    for (var i = 1; i < n; i++) {
      var x = a.get(i);
      assert x instanceof Var;
      var y = skolem(x.type(), args);
      map.put(x, y);
    }
    return map;
  }

  // Negation normal form consists of several transformations that are as easy to do at the same
  // time: Move NOTs inward to the
  // literal layer, flipping things around on the way, while simultaneously resolving quantifiers.
  private Term nnf(Map<Term, Term> map, boolean pol, Term a) {
    switch (a.tag()) {
        // Boolean constants and operators can be inverted by downward-sinking NOTs.
      case FALSE -> {
        return Term.of(!pol);
      }
      case TRUE -> {
        return Term.of(pol);
      }

      case NOT -> {
        return nnf(map, !pol, a);
      }

        // Variables are mapped to new variables or Skolem functions.
      case VAR -> {
        a = map.get(a);
        assert a != null;
        return a;
      }

        // According to whether they are bound by universal or existential quantifiers.
      case ALL -> {
        return nnf(pol ? all(map, a) : exists(map, a), pol, a.get(0));
      }
      case EXISTS -> {
        return nnf(pol ? exists(map, a) : all(map, a), pol, a.get(0));
      }

        // Equivalence is the most difficult operator to deal with.
      case EQV -> {
        var x = a.get(0);
        var y = a.get(1);
        var x0 = nnf(map, false, x);
        var x1 = nnf(map, true, x);
        var y0 = nnf(map, false, y);
        var y1 = nnf(map, true, y);

        // x <=> y means x => y & y => x. We could just construct the conjunction of implications
        // and recur on the NNF of that.
        // However, in case it needs to be negated first, that would turn into OR over AND, which
        // would go through distribute
        // generating four clauses, of which two would then be discarded as tautologies.
        // performance is improved by doing that translation in the code here
        if (pol) return Term.of(Tag.AND, Term.of(Tag.OR, x0, y1), Term.of(Tag.OR, x1, y0));
        else return Term.of(Tag.AND, Term.of(Tag.OR, x0, y0), Term.of(Tag.OR, x1, y1));
      }
    }

    // none of the above apply, so at first glance we are looking at a term that has no special
    // significance to NNF,
    // and it suffices to ground out the procedure by translating negative polarity to a NOT. but
    // there is one last thing to deal with:
    // this could be a nested expression that contains variables, perhaps several levels deep, that
    // need to be translated.
    // So we need to recur again, down to atomic terms, looking for variables to translate. at the
    // same time, we need to keep
    // the polarity at the predicate layer, to put a NOT (when necessary) in the right place. So the
    // recursive calls
    // are all made with positive polarity.
    a = a.map(b -> nnf(map, true, b));
    return pol ? a : Term.of(Tag.NOT, a);
  }

  // Distribute OR down into AND, completing the layering of the operators for CNF. This is the
  // second place where exponential
  // expansion would occur, had selected formulas not already been renamed.
  private static void flatten(Tag tag, Term a, List<Term> v) {
    if (a.tag() == tag) {
      var n = a.size();
      for (var i = 0; i < n; i++) flatten(tag, a.get(i), v);
      return;
    }
    v.add(a);
  }

  private static List<Term> flatten(Tag tag, Term a) {
    // optimize for the common special  case
    if (a.tag() != tag) return Collections.singletonList(a);

    // general case
    var v = new ArrayList<Term>();
    flatten(tag, a, v);
    return v;
  }

  private Term distribute(Term a) {
    switch (a.tag()) {
      case AND -> {
        return a.map(this::distribute);
      }
      case OR -> {
        // Flat layer of ANDs
        var ands = new ArrayList<List<Term>>(a.size());
        var n = a.size();
        for (var i = 0; i < n; i++) ands.add(flatten(Tag.AND, distribute(a.get(i))));

        // OR distributes over AND by Cartesian product.
        var ors = Etc.cartesianProduct(ands);
        var and = new Term[ors.size()];
        for (var i = 0; i < and.length; i++) and[i] = Term.of(Tag.OR, ors.get(i));
        return Term.of(Tag.AND, and);
      }
    }
    return a;
  }

  public void add(Formula formula) {}
}
