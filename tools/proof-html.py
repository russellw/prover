import argparse
import inspect
import re
import os
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


class Source:
    def __init__(self, name):
        self.name = name
        self.position = []


class Clause:
    def __init__(self, name):
        self.name = name
        self.literals = []
        self.sources = []


clauses = {}

######################################## parser


def read_tptp(filename):
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

        if eat("$false"):
            return False
        if eat("$true"):
            return True

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

    def literal():
        pol = True
        while eat("~"):
            pol = not pol
        x = atomic_term()
        y = True
        o = tok
        if o == "=":
            lex()
            y = atomic_term()
        if o == "!=":
            lex()
            pol = not Pol
            y = atomic_term()
        return pol, x, y

    # top level
    def ignore():
        if eat("("):
            while not eat(")"):
                ignore()
            return
        lex()

    lex()
    while tok:
        if tok in ("fof", "tff"):
            lex()
            ignore()
            expect(".")
            continue
        if tok not in ("cnf", "tcf"):
            err("unknown language")
        lex()
        expect("(")

        # name
        c = Clause(atomic_term())
        expect(",")

        # role
        atomic_term()
        expect(",")

        # literals
        if eat("!"):
            expect("[")
            while 1:
                atomic_term()
                if eat(":"):
                    lex()
                if not eat(","):
                    break
            expect("]")
            expect(":")
        parens = 0
        while eat("("):
            parens += 1
        if not eat("$false"):
            while 1:
                c.literals.append(literal())
                if not eat("|"):
                    break
        while parens:
            expect(")")
            parens -= 1
        expect(",")

        # source
        expect("inference")
        expect("(")
        c.rule = atomic_term()
        expect(",")
        expect("[")
        expect("status")
        expect("(")
        if c.rule == "cnf":
            expect("esa")
        else:
            expect("thm")
        expect(")")
        expect("]")
        expect(",")
        expect("[")
        while 1:
            if c.rule == "cnf":
                atomic_term()
            else:
                c.sources.append(Source(atomic_term()))
            if not eat(","):
                break
        expect("]")
        expect(")")

        # more info
        for source in c.sources:
            expect(",")
            source.i = int(tok)
            lex()
            expect(",")
            source.literal = literal()
        if c.rule != "cnf":
            source = c.sources[-1]
            while eat(","):
                source.position.append(int(tok))
                lex()
        expect(")")
        expect(".")

        clauses[c.name] = c


######################################## printing

outf = None


def htmlBegin(title):
    outf.write("<!DOCTYPE html>\n")
    outf.write('<html lang="en">\n')
    outf.write('<meta charset="utf-8"/>\n')
    outf.write("<title>%s</title>\n" % title)


def header(s):
    outf.write("<h3>%s</h3>\n" % s)


def headerlink(s):
    outf.write('<h2 id="%s">%s</h2>\n' % (s, s))


def prname(s):
    outf.write('<a href="#%s">%s</a>' % (s, s))


def ucode(n):
    pr("&#%d;" % n)


def pr(x):
    if x is True:
        ucode(0x22A4)
        return
    if type(x) is str:
        outf.write(x)
        return
    pr("(")
    for i in range(len(x)):
        if i:
            pr(" ")
        pr(x[i])
    pr(")")


def prliteral(a):
    pr(a[1])
    pr(" ")
    if a[0]:
        pr("=")
    else:
        ucode(0x2260)
    pr(" ")
    pr(a[2])
    pr("<br>\n")


######################################## main


parser = argparse.ArgumentParser(description="Print proofs in HTML")
parser.add_argument("infile")
args = parser.parse_args()


read_tptp(args.infile)

title = os.path.splitext(os.path.basename(args.infile))[0]
outfile = title + ".html"
if title.endswith("-proof"):
    title = title[:-6]
outf = open(outfile, "w")
htmlBegin(title)
pr("<code>\n")

for name in clauses:
    z = clauses[name]
    for i in range(len(z.sources)):
        if i:
            outf.write("&amp; ")
        prname(z.sources[i].name)
        pr(" ")
    outf.write(z.rule + "-&gt; ")
    prname(z.name)
    outf.write("<br>\n")

for name in clauses:
    z = clauses[name]
    outf.write("<hr>\n")
    for sr in z.sources:
        header(sr.name)
        c = clauses[sr.name]
        for a in c.literals:
            prliteral(a)
    headerlink(name)
    for a in z.literals:
        prliteral(a)
    if not z.literals:
        ucode(0x22A5)
        outf.write("<br>\n")
