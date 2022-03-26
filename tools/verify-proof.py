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
                    if ":" in x:
                        x = x.split(":")[0]
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


def quantify(a):
    xs = freeVars(a)
    if not xs:
        return a
    return "!", xs, a


assert fns(("&", ("&", "a", "b", ("p", "X", "Y")), "a")) == {"a": 0, "b": 0, "p": 2}


class Formula:
    def __init__(self, name, term):
        self.name = name
        self.term = quantify(term)
        self.derived_from = []


types = {}

######################################## parser


def read_tptp(filename, formulas, select=True):
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

    # types
    def atomic_type():
        o = tok
        lex()
        return o

    def top_level_type():
        if eat("("):
            v = []
            v.append(atomic_type())
            while eat("*"):
                v.append(atomic_type())
            expect(")")
            expect(">")
            v.append(atomic_type())
            return tuple(v)
        t = atomic_type()
        if eat(">"):
            return t, atomic_type()
        return t

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

    def variable():
        x = atomic_term()
        if eat(":"):
            x += ":"
            x += atomic_term()
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
            v.append(variable())
            while tok == ",":
                lex()
                v.append(variable())
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

    def source_record(derived_from):
        if tok == "file":
            return
        expect("inference")
        expect("(")
        lex()
        expect(",")
        expect("[")
        expect("status")
        expect("(")
        if not eat("thm"):
            return
        expect(")")
        expect("]")
        expect(",")
        expect("[")
        if tok == "inference":
            source_record(derived_from)
            return
        derived_from.append(atomic_term())
        while eat(","):
            derived_from.append(atomic_term())

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
            a = atomic_term()
            expect(":")
            t = top_level_type()
            types[a] = t
        elif role == "conjecture":
            while tok != ")":
                ignore()
        else:
            if selecting(name):
                x = logic_formula()
                f = Formula(name, x)
                formulas[name] = f
                if eat(","):
                    source_record(f.derived_from)

        # annotations
        while tok != ".":
            ignore()

        # end
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


def prtype(t):
    if type(t) is str:
        pr(t)
        return
    if len(t) == 2:
        prtype(t[0])
        pr(" > ")
        prtype(t[1])
        return
    pr("(")
    prtype(t[0])
    for i in range(1, len(t) - 1):
        pr(" * ")
        prtype(t[i])
    pr(") > ")
    prtype(t[-1])


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
    pr("tff")
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
    for name in types:
        pr("tff(t, type, ")
        pr(name)
        pr(": ")
        prtype(types[name])
        pr(").\n")
    for x in xs:
        prformula(x)
    outf.close()


######################################## test


def prove(xs):
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
    if stderr or p.returncode:
        print(stderr, end="")
        raise Exception(str(p.returncode))
    ys = stdout.splitlines()
    for y in ys:
        if "SZS status Unsatisfiable" in y:
            return 1
    raise Exception(xs)


######################################## main


parser = argparse.ArgumentParser(description="Verify proof")
parser.add_argument("prover")
parser.add_argument("problem")
parser.add_argument("proof")
parser.add_argument("outfile")
args = parser.parse_args()

prover = args.prover.split()

formulas = {}
read_tptp(args.problem, formulas)

formulas = {}
read_tptp(args.proof, formulas)

for name in formulas:
    f = formulas[name]
    if not f.derived_from:
        continue
    x = f.term
    xs = []
    for name1 in f.derived_from:
        f1 = formulas[name1]
        y = f1.term
        xs.append(y)
    xs.append(("~", x))
    prove(xs)
