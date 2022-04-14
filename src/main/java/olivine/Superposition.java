package olivine;

import java.util.*;

public final class Superposition {
  private final KnuthBendixOrder order;
  private PriorityQueue<Clause> passive =
      new PriorityQueue<>(Comparator.comparingLong(Superposition::volume));

  // if we run out of inferences, unless one of the reasons applies for the proof search to be
  // incomplete,
  // the problem is satisfiable. In practice, this is rare but can occasionally happen,
  // and is a good way to detect some kinds of incompleteness errors in the prover
  private boolean complete = true;
  private final boolean result;

  private Clause c, d;
  private int ci, cj, di;
  private Term c0, c1, c2, c3, d0, d1;
  private final List<Integer> position = new ArrayList<>();

  private static long volume(Clause c) {
    var n = c.literals.length * 2L;
    for (var a : c.literals) n += a.symbolCount();
    return n;
  }

  private void clause(Clause c) {
    if (c.isTrue()) return;
    // TODO: is the clause limit useful?
    var clauseLimit = 10000000;
    if (passive.size() >= clauseLimit) {
      var passive1 = new PriorityQueue<>(Comparator.comparingLong(Superposition::volume));
      for (var i = 0; i < clauseLimit / 2; i++) passive1.add(passive.poll());
      passive = passive1;

      // If we had to discard clauses to save memory, completeness was lost,
      // so running out of inferences doesn't prove anything
      complete = false;
    }
    passive.add(c);
  }

  private boolean notMaximal(Term[] literals, int negativeSize, int i, Equation e) {
    var pol = i >= negativeSize;
    for (var j = 0; j < literals.length; j++) {
      if (j == i) continue;
      var pol1 = j >= negativeSize;
      var e1 = new Equation(literals[j]);
      if (order.compare(pol, e, pol1, e1) == PartialOrder.LESS) return true;
    }
    return false;
  }

  private boolean notStrictlyMaximal(Term[] literals, int negativeSize, int i, Equation e) {
    var pol = i >= negativeSize;
    for (var j = 0; j < literals.length; j++) {
      if (j == i) continue;
      var pol1 = j >= negativeSize;
      var e1 = new Equation(literals[j]);
      switch (order.compare(pol, e, pol1, e1)) {
        case LESS, EQUALS -> {
          return true;
        }
      }
    }
    return false;
  }

  private boolean notModeMaximal(Term[] literals, int negativeSize, int i, Equation e) {
    return i < negativeSize
        ? notMaximal(literals, negativeSize, i, e)
        : notStrictlyMaximal(literals, negativeSize, i, e);
  }

  /*
  Equality resolution
    c | c0 != c1
  and
    map = unify(c0, c1)
  ->
    c/map
  */

  // unify, substitute and make new clause
  private void resolvem() {
    var map = c0.unify(FMap.EMPTY, c1);
    if (map == null) return;

    c0 = c0.replace(map);
    c1 = c1.replace(map);

    var cliterals = new Term[c.literals.length];
    for (var i = 0; i < c.literals.length; i++) cliterals[i] = c.literals[i].replace(map);
    if (notMaximal(cliterals, c.negativeSize, ci, new Equation(c0, c1))) return;

    // Negative literals
    var negative = new ArrayList<Term>(c.negativeSize - 1);
    for (var i = 0; i < c.negativeSize; i++) if (i != ci) negative.add(cliterals[i]);

    // Positive literals
    var positive = new ArrayList<Term>(c.positiveSize());
    //noinspection ManualArrayToCollectionCopy
    for (var i = c.negativeSize; i < cliterals.length; i++) positive.add(cliterals[i]);

    // Make new clause
    clause(new Clause(negative, positive));
  }

  // For each negative equation
  private void resolve() {
    for (ci = 0; ci < c.negativeSize; ci++) {
      var e = new Equation(c.literals[ci]);
      if (notMaximal(c.literals, c.negativeSize, ci, e)) continue;

      c0 = e.left;
      c1 = e.right;
      resolvem();
    }
  }

  /*
  Equality factoring
    c | c0 = c1 | c2 = c3
  and
    map = unify(c0, c2)
  ->
    (c | c0 = c1 | c1 != c3)/map
  */

  // unify, substitute and make new clause
  private void factorm() {
    // in tests, the unification check failed more often than the equatable
    // check,  so putting it first may save a little time
    var map = c0.unify(FMap.EMPTY, c2);
    if (map == null) return;

    // the test for c1=c3 being a valid equation, would strictly speaking fail if c1=True and c3
    // is some other predicate.
    // (That would be a valid equation, just the wrong way round; in superposition calculus,
    // that means the equation should be turned  the right way round, not discarded.)
    // But if c1=True, c0 is a predicate
    // that is not True (or the clause would have been a tautology, filtered out earlier), so c2 is
    // a
    // predicate that is not True (or unification would have failed; there is no such thing as a
    // Boolean
    // variable in first-order logic), so c3
    // can only be True (because an equation with a predicate other than True on one side,
    // can only have True on the other)
    assert !(c1 == Term.TRUE && c3 != Term.TRUE);

    // If these two terms are not equatable (for which the types must match, and predicates can only
    // be equated with True),
    // substituting terms for variables would not make them become so.
    if (!Equation.equatable(c1, c3)) return;

    c0 = c0.replace(map);
    c1 = c1.replace(map);

    // the superposition calculus condition on the orienting of equations,
    // actually applies after the map. We already applied it before, to avoid spending time
    // on equations that are definitely the wrong orientation to begin with,
    // but in some cases, equations that were unordered, become ordered after substitution,
    // and ordered the wrong way, so rechecking the orientation,
    // suppresses some unnecessary inferences
    if (order.compare(c0, c1) == PartialOrder.LESS) return;

    // ditto for the condition on equation being maximal within clause
    var cliterals = new Term[c.literals.length];
    for (var i = 0; i < c.literals.length; i++) cliterals[i] = c.literals[i].replace(map);
    if (notMaximal(cliterals, c.negativeSize, ci, new Equation(c0, c1))) return;

    // Negative literals
    var negative = new ArrayList<Term>(c.negativeSize + 1);
    //noinspection ManualArrayToCollectionCopy
    for (var i = 0; i < c.negativeSize; i++) negative.add(cliterals[i]);
    negative.add(new Equation(c1, c3.replace(map)).term());

    // Positive literals
    var positive = new ArrayList<Term>(c.positiveSize() - 1);
    for (var i = c.negativeSize; i < cliterals.length; i++) if (i != cj) positive.add(cliterals[i]);

    // Make new clause
    clause(new Clause(negative, positive));
  }

  // For each positive equation (both directions) again
  private void factorj() {
    for (cj = c.negativeSize; cj < c.literals.length; cj++) {
      if (cj == ci) continue;
      var e = new Equation(c.literals[cj]);

      c2 = e.left;
      c3 = e.right;
      factorm();

      c2 = e.right;
      c3 = e.left;
      factorm();
    }
  }

  // For each positive equation (both directions)
  private void factor() {
    for (ci = c.negativeSize; ci < c.literals.length; ci++) {
      var e = new Equation(c.literals[ci]);
      if (notMaximal(c.literals, c.negativeSize, ci, e)) continue;

      assert order.compare(e.left, e.right) != PartialOrder.LESS;
      c0 = e.left;
      c1 = e.right;
      factorj();

      if (order.compare(e.right, e.left) != PartialOrder.LESS) {
        c0 = e.right;
        c1 = e.left;
        factorj();
      }
    }
  }

  /*
  Superposition
    c | c0 = c1, d | d0(a) ?= d1
  and
    map = unify(c0, a)
    a not variable
  ->
    (c | d | d0(c1) ?= d1)/map
  */

  // unify, substitute and make new clause
  private void spm(Term a) {
    var map = c0.unify(FMap.EMPTY, a);
    if (map == null) return;

    var d0c1 = d0.splice(position, 0, c1);

    // the test for d0c1=d1 being a valid equation, would strictly speaking fail if d0c1=True and d1
    // is some other predicate. But if d0c1=True, c1=True, c0 is a predicate
    // that is not True (or the clause would have been a tautology, filtered out earlier), so d0(a)
    // is a
    // predicate that is not True (or unification would have failed; there is no such thing as a
    // Boolean
    // variable in first-order logic), so d1
    // can only be True (because an equation with a predicate other than True on one side,
    // can only have True on the other)
    assert !(d0c1 == Term.TRUE && d1 != Term.TRUE);
    if (!Equation.equatable(d0c1, d1)) return;

    c0 = c0.replace(map);
    c1 = c1.replace(map);
    d0 = d0.replace(map);
    d1 = d1.replace(map);

    if (order.compare(c0, c1) == PartialOrder.LESS) return;
    if (order.compare(d0, d1) == PartialOrder.LESS) return;

    var cliterals = new Term[c.literals.length];
    for (var i = 0; i < c.literals.length; i++) cliterals[i] = c.literals[i].replace(map);
    if (notStrictlyMaximal(cliterals, c.negativeSize, ci, new Equation(c0, c1))) return;

    var dliterals = new Term[d.literals.length];
    for (var i = 0; i < d.literals.length; i++) dliterals[i] = d.literals[i].replace(map);
    if (notModeMaximal(dliterals, d.negativeSize, di, new Equation(d0, d1))) return;

    // Negative literals
    var negative = new ArrayList<Term>(c.negativeSize + d.negativeSize);
    //noinspection ManualArrayToCollectionCopy
    for (var i = 0; i < c.negativeSize; i++) negative.add(cliterals[i]);
    for (var i = 0; i < d.negativeSize; i++) if (i != di) negative.add(dliterals[i]);

    // Positive literals
    var positive = new ArrayList<Term>(c.positiveSize() + d.positiveSize() - 1);
    for (var i = c.negativeSize; i < cliterals.length; i++) if (i != ci) positive.add(cliterals[i]);
    for (var i = d.negativeSize; i < dliterals.length; i++) if (i != di) positive.add(dliterals[i]);

    // Negative and positive superposition
    (di < d.negativeSize ? negative : positive).add(new Equation(d0c1, d1).term().replace(map));

    // Make new clause
    clause(new Clause(negative, positive));
  }

  // recur into subterms
  private void spr(Term a) {
    if (a instanceof Var) return;
    spm(a);
    var n = a.size();
    for (var i = 0; i < n; i++) {
      position.add(i);
      spr(a.get(i));
      position.remove(position.size() - 1);
    }
  }

  // For each equation in d (both directions)
  private void spd() {
    assert position.isEmpty();
    for (di = 0; di < d.literals.length; di++) {
      var e = new Equation(d.literals[di]);
      if (notModeMaximal(d.literals, d.negativeSize, di, e)) continue;

      assert order.compare(e.left, e.right) != PartialOrder.LESS;
      d0 = e.left;
      d1 = e.right;
      spr(d0);

      if (order.compare(e.right, e.left) != PartialOrder.LESS) {
        d0 = e.right;
        d1 = e.left;
        spr(d0);
      }
    }
  }

  // For each positive equation in c (both directions)
  private void sp() {
    for (ci = c.negativeSize; ci < c.literals.length; ci++) {
      var e = new Equation(c.literals[ci]);
      if (notStrictlyMaximal(c.literals, c.negativeSize, ci, e)) continue;

      assert order.compare(e.left, e.right) != PartialOrder.LESS;
      c0 = e.left;
      c1 = e.right;
      spd();

      if (order.compare(e.right, e.left) != PartialOrder.LESS) {
        c0 = e.right;
        c1 = e.left;
        spd();
      }
    }
  }

  private Superposition(List<Clause> clauses, long steps) {
    order = new KnuthBendixOrder(clauses);
    List<Clause> active = new ArrayList<>();
    var subsumption = new Subsumption();
    for (var c : clauses) {
      // add the initial clauses to the passive queue
      clause(c);

      // first-order logic is not complete on arithmetic, so check whether this problem uses
      // arithmetic;
      // if it does, running out of inferences will not prove anything
      for (var a : c.literals)
        a.walkLeaves(
            b -> {
              if (b.type().isNumeric()) complete = false;
            });
    }
    while (!passive.isEmpty()) {
      if (steps-- == 0) throw new Fail();

      // Given clause.
      var g = passive.poll();

      // Solved
      if (g.isFalse()) {
        result = false;
        return;
      }

      // the time to put all the equations in this clause in the right order is now,
      // late enough that we know we are actually going to use this clause,
      // but early enough that we can do it just once
      for (var i = 0; i < g.literals.length; i++) {
        var a = g.literals[i];
        if (a.tag() != Tag.EQUALS) continue;
        var x = a.get(0);
        var y = a.get(1);
        if (order.compare(x, y) == PartialOrder.LESS) g.literals[i] = Term.of(Tag.EQUALS, y, x);
      }

      // Rename variables, because subsumption and superposition both assume
      // clauses have disjoint variable names
      var g1 = g.renameVars();

      // Discount loop, which only subsumes against active clauses, performed slightly better in
      // tests.
      // The alternative Otter loop would also subsume against passive clauses
      if (subsumption.subsumesForward(active, g1)) continue;
      active = subsumption.subsumeBackward(g1, active);

      // Infer from one clause
      c = g;
      resolve();
      factor();

      // Sometimes need to match g with itself
      active.add(g);

      // Infer from two clauses
      for (var ac : active) {
        c = ac;
        d = g1;
        sp();

        c = g1;
        d = ac;
        sp();
      }
    }
    if (!complete) throw new Fail();
    result = true;
  }

  public static boolean sat(List<Clause> clauses, long steps) {
    return new Superposition(clauses, steps).result;
  }
}
