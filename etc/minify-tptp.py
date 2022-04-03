# aid debugging of theorem prover by minifying a test case
# that causes it to produce a different answer to another prover
# commandline arguments are the commands for the two provers,
# the filename of the test case,
# and an output filename for the minified version

import argparse
import inspect
import subprocess
import re
import sys
import logging

logger = logging.getLogger()
logger.addHandler(logging.StreamHandler(sys.stdout))
logger.setLevel(logging.DEBUG)

# numbers larger than 2000 silently fail
sys.setrecursionlimit(2000)


def trace(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    logger.debug(f"{info.filename}:{info.function}:{info.lineno}: {repr(a)}")


def check_tuples(x):
    if isinstance(x, tuple):
        for y in x:
            check_tuples(y)
        return
    if isinstance(x, list):
        raise ValueError(x)


def imp(x, y):
    return "|", ("~", x), y


def size(x):
    if type(x) in (list, tuple):
        n = 0
        for y in x:
            n += size(y)
        return n
    return 1


def walk(f, x):
    f(x)
    if isinstance(x, tuple):
        for a in x:
            walk(f, a)


def fns(x):
    r = {}

    def f(a):
        if isinstance(a, tuple):
            if isFn(a[0]):
                r[a[0]] = len(a) - 1
            for b in a[1:]:
                f(b)
        if isFn(a):
            r[a] = 0

    f(x)
    return r


def isFn(a):
    if type(a) != str:
        return
    return a[0].islower()


def isVar(a):
    if type(a) != str:
        return
    return a[0].isupper()


def freeVars(a):
    free = []

    def rec(a, bound):
        if isinstance(a, tuple):
            if a[0] in ("!", "?"):
                bound = bound.copy()
                for x in a[1]:
                    bound.add(x)
                rec(a[2], bound)
                return
            for b in a[1:]:
                rec(b, bound)
            return
        if isVar(a):
            if a not in bound and a not in free:
                free.append(a)

    rec(a, set())
    return free


assert fns(("&", ("&", "a", "b", ("p", "X", "Y")), "a")) == {"a": 0, "b": 0, "p": 2}

######################################## parser


def read_tptp(filename, xs, select=True):
    text = open(filename).read()
    if text and text[-1] != "\n":
        text += "\n"

    # tokenizer
    ti = 0
    tok = ""

    def err(msg):
        line = 1
        for i in range(ti):
            if text[i] == "\n":
                line += 1
        raise ValueError(f"{filename}:{line}: {repr(tok)}: {msg}")

    def lex():
        nonlocal ti
        nonlocal tok
        while ti < len(text):
            c = text[ti]

            # space
            if c.isspace():
                ti += 1
                continue

            # line comment
            if c in ("%", "#"):
                i = ti
                while text[ti] != "\n":
                    ti += 1
                continue

            # block comment
            if text[ti : ti + 2] == "/*":
                ti += 2
                while text[ti : ti + 2] != "*/":
                    ti += 1
                ti += 2
                continue

            # word
            if c.isalpha() or c == "$":
                i = ti
                ti += 1
                while text[ti].isalnum() or text[ti] == "_":
                    ti += 1
                tok = text[i:ti]
                return

            # quote
            if c in ("'", '"'):
                i = ti
                ti += 1
                while text[ti] != c:
                    if text[ti] == "\\":
                        ti += 1
                    ti += 1
                ti += 1
                tok = text[i:ti]
                return

            # number
            if c.isdigit() or (c == "-" and text[ti + 1].isdigit()):
                # integer part
                i = ti
                ti += 1
                while text[ti].isalnum():
                    ti += 1

                # rational
                if text[ti] == "/":
                    ti += 1
                    while text[ti].isdigit():
                        ti += 1

                # real
                else:
                    if text[ti] == ".":
                        ti += 1
                        while text[ti].isalnum():
                            ti += 1
                    if text[ti - 1] in ("e", "E") and text[ti] in ("+", "-"):
                        ti += 1
                        while text[ti].isdigit():
                            ti += 1

                tok = text[i:ti]
                return

            # punctuation
            if text[ti : ti + 3] in ("<=>", "<~>"):
                tok = text[ti : ti + 3]
                ti += 3
                return
            if text[ti : ti + 2] in ("!=", "=>", "<=", "~&", "~|"):
                tok = text[ti : ti + 2]
                ti += 2
                return
            tok = c
            ti += 1
            return

        # end of file
        tok = None

    def eat(o):
        if tok == o:
            lex()
            return True

    def expect(o):
        if tok != o:
            err(f"expected '{o}'")
        lex()

    # terms
    def args():
        expect("(")
        r = []
        if tok != ")":
            r.append(atomic_term())
            while tok == ",":
                lex()
                r.append(atomic_term())
        expect(")")
        return tuple(r)

    def atomic_term():
        o = tok

        # higher-order terms
        if tok == "!":
            raise "Inappropriate"

        # syntax sugar
        if eat("$greater"):
            s = args()
            return "$less", s[1], s[0]
        if eat("$greatereq"):
            s = args()
            return "$lesseq", s[1], s[0]

        lex()
        if tok == "(":
            s = args()
            return (o,) + s

        return o

    def infix_unary():
        x = atomic_term()
        o = tok
        if o == "=":
            lex()
            return "=", x, atomic_term()
        if o == "!=":
            lex()
            return "~", ("=", x, atomic_term())
        return x

    def unitary_formula():
        o = tok
        if o == "(":
            lex()
            x = logic_formula()
            expect(")")
            return x
        if o == "~":
            lex()
            return "~", unitary_formula()
        if o in ("!", "?"):
            lex()

            # variables
            expect("[")
            v = []
            v.append(atomic_term())
            while tok == ",":
                lex()
                v.append(atomic_term())
            expect("]")

            # body
            expect(":")
            x = o, tuple(v), unitary_formula()
            return x
        return infix_unary()

    def logic_formula():
        x = unitary_formula()
        o = tok
        if o in ("&", "|"):
            v = [o, x]
            while eat(o):
                v.append(unitary_formula())
            return tuple(v)
        if o == "<=>":
            lex()
            return o, x, unitary_formula()
        if o == "=>":
            lex()
            return imp(x, unitary_formula())
        if o == "<=":
            lex()
            return imp(unitary_formula(), x)
        if o == "<~>":
            lex()
            return "~", ("<=>", x, unitary_formula())
        if o == "~&":
            lex()
            return "~", ("&", x, unitary_formula())
        if o == "~|":
            lex()
            return "~", ("|", x, unitary_formula())
        return x

    # top level
    def ignore():
        if eat("("):
            while not eat(")"):
                ignore()
            return
        lex()

    def selecting(name):
        return select is True or name in select

    def annotated_formula():
        lex()
        expect("(")

        # name
        name = atomic_term()
        expect(",")

        # role
        role = atomic_term()
        expect(",")

        if role == "type":
            while tok != ")":
                ignore()
        else:
            x = logic_formula()
            if selecting(name):
                if role == "conjecture":
                    x = "~", x
                xs.append(x)

        # annotations
        if tok == ",":
            while tok != ")":
                ignore()

        # end
        expect(")")
        expect(".")

    def include():
        lex()
        expect("(")

        # tptp
        tptp = os.getenv("TPTP")
        if not tptp:
            err("TPTP environment variable not set")

        # file
        filename1 = atomic_term()

        # select
        select1 = select
        if eat(","):
            expect("[")
            select1 = []
            while True:
                name = atomic_term()
                if selecting(name):
                    select1.append(name)
                if not eat(","):
                    break
            expect("]")

        # include
        read_tptp(tptp + "/" + filename1, xs, select1)

        # end
        expect(")")
        expect(".")

    lex()
    header = False
    while tok:
        if tok in ("cnf", "fof", "tff", "tcf"):
            annotated_formula()
            continue
        if tok == "include":
            include()
            continue
        err("unknown language")


######################################## printing

outf = None


def pr(x):
    if type(x) is not str:
        x = str(x)
    outf.write(x)


def prargs(x):
    pr("(")
    for i in range(1, len(x)):
        if i > 1:
            pr(",")
        prterm(x[i])
    pr(")")


def need_parens(x, parent):
    if not parent:
        return
    if x[0] in ("&", "<=>", "|"):
        return parent[0] in ("&", "<=>", "?", "!", "~", "|")


def prterm(x, parent=None):
    if isinstance(x, tuple):
        o = x[0]
        # infix
        if o == "=":
            prterm(x[1])
            pr("=")
            prterm(x[2])
            return
        if o in ("&", "<=>", "|"):
            if need_parens(x, parent):
                pr("(")
            for i in range(1, len(x)):
                if i > 1:
                    pr(f" {o} ")
                prterm(x[i], x)
            if need_parens(x, parent):
                pr(")")
            return

        # prefix/infix
        if o == "~":
            pr("~")
            prterm(x[1], x)
            return

        # prefix
        if o in ("?", "!"):
            pr(o)
            pr("[")
            v = x[1]
            for i in range(len(v)):
                if i:
                    pr(",")
                y = v[i]
                pr(y)
            pr("]:")
            prterm(x[2], x)
            return
        pr(o)
        prargs(x)
        return
    pr(x)


formnames = 0


def prformula(x):
    global formnames
    formnames += 1
    if freeVars(x):
        pr("cnf")
    else:
        pr("fof")
    pr("(f")

    # name
    pr(formnames)
    pr(", ")

    # role
    pr("plain")
    pr(", ")

    # content
    prterm(x)

    # end
    pr(").\n")


def write_tmp(xs):
    global formnames
    global outf
    formnames = 0
    outf = open(args.outfile, "w")
    pr("% Minified version of:\n")
    pr("% " + args.infile + "\n")
    pr("% Different answers from:\n")
    pr("% " + args.prover1 + "\n")
    pr("% " + args.prover2 + "\n")
    for x in xs:
        prformula(x)
    outf.close()


######################################## test


def prove(prover, x):
    if isinstance(x, tuple) and x[0] == "&":
        xs = x[1:]
    else:
        xs = [x]
    write_tmp(xs)
    cmd = prover + [args.outfile]
    p = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    stdout, stderr = p.communicate()
    stdout = str(stdout, "utf-8")
    stderr = str(stderr, "utf-8")
    if stderr:
        print(stderr, end="")
        raise Exception(str(p.returncode))
    ys = stdout.splitlines()
    for y in ys:
        if y == "sat":
            return 1
        if "SZS status Satisfiable" in y:
            return 1
        if "SZS status CounterSatisfiable" in y:
            return 1
        if y == "unsat":
            return 0
        if "SZS status Unsatisfiable" in y:
            return 0
        if "SZS status Theorem" in y:
            return 0
    return -1


def test(x):
    a1 = prove(prover1, x)
    if a1 < 0:
        return
    a2 = prove(prover2, x)
    if a2 < 0:
        return
    return a1 != a2


######################################## shrink


def simplify(x):
    if isinstance(x, tuple):
        y = []
        for a in x:
            y.append(simplify(a))
        x = tuple(y)
        if len(x) == 1 and isFn(x[0]):
            return x[0]
        if x[0] in ("&", "|"):
            if len(x) == 2:
                return x[1]
        if x[0] == "~":
            a = x[1]
            if isinstance(a, tuple) and a[0] == "~":
                return a[1]
    return x


assert simplify(("&", ("&", "a"))) == "a"


def delete(v, x):
    assert isinstance(x, tuple)
    y = list(x)
    if len(v) == 1:
        del y[v[0]]
    else:
        y[v[0]] = delete(v[1:], y[v[0]])
    return tuple(y)


assert delete([1, 1], ("&", ("&", "a", "b"), "c")) == ("&", ("&", "b"), "c")


def subst_bool(v, b, x):
    if not v:
        return b
    assert isinstance(x, tuple)
    y = list(x)
    y[v[0]] = subst_bool(v[1:], b, y[v[0]])
    return tuple(y)


assert subst_bool([1, 1], "$true", ("&", ("&", "a", "b"), "c")) == (
    "&",
    ("&", "$true", "b"),
    "c",
)


def remove_param(fn, i, x):
    if not isinstance(x, tuple):
        return x
    y = list(x)
    if x[0] == fn:
        del y[i]
    for j in range(1, len(y)):
        y[j] = remove_param(fn, i, y[j])
    return tuple(y)


def apply_shrink(s, x):
    if s[0] == "delete":
        v = s[1]
        return simplify(delete(v, x))
    if s[0] == "subst_bool":
        v = s[1]
        b = s[2]
        return simplify(subst_bool(v, b, x))
    if s[0] == "remove_param":
        fn = s[1]
        i = s[2]
        return simplify(remove_param(fn, i, x))
    raise Exception(str(s))


shrinks = []


def find_deletes(v, x):
    if isinstance(x, tuple):
        if x[0] in ("&", "|"):
            for i in range(1, len(x)):
                v1 = v + [i]
                shrinks.append(("delete", v1))
        for i in range(1, len(x)):
            v1 = v + [i]
            find_deletes(v1, x[i])


def find_subst_bools(v, x):
    if isinstance(x, tuple):
        if x[0] in ("!", "?"):
            i = 2
            v1 = v + [i]
            shrinks.append(("subst_bool", v1, "$true"))
            shrinks.append(("subst_bool", v1, "$false"))
        for i in range(1, len(x)):
            v1 = v + [i]
            find_subst_bools(v1, x[i])


def find_remove_params(x):
    fs = fns(x)
    for fn in fs:
        if fs[fn]:
            for i in range(1, fs[fn] + 1):
                shrinks.append(("remove_param", fn, i))


def find_shrinks(x):
    global shrinks
    shrinks = []
    find_deletes([], x)
    find_subst_bools([], x)
    find_remove_params(x)


######################################## main


parser = argparse.ArgumentParser(description="Minify TPTP test case")
parser.add_argument("prover1")
parser.add_argument("prover2")
parser.add_argument("infile")
parser.add_argument("outfile")
args = parser.parse_args()

prover1 = args.prover1.split()
prover2 = args.prover2.split()

xs = []
read_tptp(args.infile, xs)
x = tuple(["&"] + xs)
if not test(x):
    print(prove(prover1, x))
    print(prove(prover2, x))
    raise Exception("Initial test failed")
print(size(x))

more = 1
while more:
    more = 0
    find_shrinks(x)
    for s in shrinks:
        y = apply_shrink(s, x)
        if test(y):
            print(size(y))
            x = y
            more = 1
            break

if not test(x):
    raise Exception("Final test failed")

print(open(args.outfile).read(), end="")
