# Olivine

Olivine is an automated theorem prover for first-order logic with equality, based on the
[superposition calculus](https://en.wikipedia.org/wiki/Superposition_calculus) — the same family
of calculi used by state-of-the-art provers such as E and Vampire.

Given a set of axioms and a conjecture, it searches for a proof by contradiction: the conjecture
is negated, the problem is reduced to clause normal form, and saturation-based inference runs
until a contradiction is derived (the conjecture is a theorem) or the search space is exhausted
(the conjecture is not entailed).

## Features

- **Superposition calculus** with the standard inference rules: superposition, equality
  resolution, and equality factoring, restricted by literal selection based on a
  [Knuth–Bendix term ordering](https://en.wikipedia.org/wiki/Path_ordering_(term_rewriting))
- **Full first-order logic input**: reads the standard
  [TPTP](https://tptp.org/) formats `cnf`, `fof`, `tff`, and `tcf`, including typed problems
- **Clause normal form conversion** with variable renaming to avoid exponential blowup
- **Subsumption**: forward and backward, to keep the clause set small
- **Propositional reasoning**: DIMACS input is accepted, and purely propositional problems are
  dispatched to a built-in SAT solver (DPLL; a CDCL implementation is also included)
- **Unit tested throughout**, including randomized property tests that check the term ordering
  satisfies the axioms required for completeness

## Building

Requires Java 17+ and Maven:

```sh
mvn package
```

This produces `target/olivine-1.0-SNAPSHOT.jar` and runs the unit test suite along the way.

## Usage

```sh
java -cp target/olivine-1.0-SNAPSHOT.jar olivine.Prover problem.p
```

The input language is inferred from the file extension (`.p`/`.ax` for TPTP, `.cnf` for DIMACS)
or can be forced with `--tptp` or `--dimacs` (which also allows reading from standard input).
Use `-t seconds` to set a time limit; `-h` lists all options.

The prover prints `unsat` if it derives a contradiction — for a TPTP problem with a conjecture,
this means the conjecture is proved — or `sat` if the clause set is satisfiable.

## Example

`socrates.p`:

```
fof(all_men_mortal, axiom, ![X]: (man(X) => mortal(X))).
fof(socrates_man, axiom, man(socrates)).
fof(socrates_mortal, conjecture, mortal(socrates)).
```

```sh
$ java -cp target/olivine-1.0-SNAPSHOT.jar olivine.Prover socrates.p
unsat
```

The negated conjecture is unsatisfiable together with the axioms, so Socrates is mortal.

## Testing

Unit tests run with `mvn test`. There is also a batch test harness, `olivine.ProverTest`, that
runs the prover over the [TPTP problem library](https://tptp.org/) (expected in `$TPTP`) and
checks results against each problem's declared status:

```sh
java -cp target/olivine-1.0-SNAPSHOT.jar olivine.ProverTest -n 1000 -s
```

## Code map

| Class | Purpose |
| --- | --- |
| `Term`, `Type` | Terms and types, the core data structures |
| `TptpParser`, `DimacsParser` | Input parsing |
| `CNF` | Conversion to clause normal form |
| `KnuthBendixOrder` | Term ordering that orients equations |
| `Superposition` | Saturation loop and inference rules |
| `Subsumption` | Forward and backward subsumption |
| `Dpll`, `Cdcl` | Propositional SAT solvers |
| `Prover` | Command-line entry point |

## License

MIT
