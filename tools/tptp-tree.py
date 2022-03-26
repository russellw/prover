import argparse
import inspect
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


def depth(x):
    if type(x) in (list, tuple):
        n = 0
        for y in x:
            n = max(n, depth(y))
        return n + 1
    return 0


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


######################################## parser


def read_tptp(filename, select=True):
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
        # sublanguage
        sublanguage = tok
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
            # if role == "conjecture":
            #    x = "~", x

        # annotations
        if tok == ",":
            while tok != ")":
                ignore()

        # end
        expect(")")
        expect(".")

        if role == "type":
            return

        print("%s %s %s" % (sublanguage, name, role))
        pr(x)
        print()
        print()

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
        read_tptp(tptp + "/" + filename1, select1)

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


def pr(x, dent=0):
    if type(x) not in (list, tuple):
        print(x, end="")
        return
    assert x
    if depth(x) <= 6:
        pr("(")
        for i in range(len(x)):
            if i:
                pr(" ")
            pr(x[i])
        pr(")")
        return
    pr(x[0])
    print()
    dent += 1
    for i in range(1, len(x)):
        pr(" " * dent)
        pr(x[i], dent)
        if i < len(x) - 1:
            print()


######################################## main


parser = argparse.ArgumentParser(description="Print TPTP in tree form")
parser.add_argument("infile")
args = parser.parse_args()


read_tptp(args.infile)
