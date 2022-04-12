import logging
import inspect
import sys

logger = logging.getLogger()
logger.addHandler(logging.StreamHandler(sys.stdout))
logger.setLevel(logging.DEBUG)


def trace(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    logger.debug(f"{info.filename}:{info.function}:{info.lineno}: {repr(a)}")


def cyclic(g):
    for x in range(4):
        visited = set()
        result = 0

        def rec(y):
            nonlocal result
            if y in visited:
                if y == x:
                    result = 1
                return
            visited.add(y)
            for z in range(4):
                if (y, z) in g:
                    rec(z)

        rec(x)
        if result:
            return 1


def consistent(ords):
    es = {}
    for xy, c in ords.items():
        x, y = sorted(xy)
        if c == "e":
            es[y] = x

    g = set()
    for xy, c in ords.items():
        x, y = xy
        while x in es:
            x = es[x]
        while y in es:
            y = es[y]
        if c == "g":
            g.add((x, y))
        elif c == "l":
            g.add((y, x))
    if cyclic(g):
        return 0
    return 1


xys = []
for i in range(4 - 1):
    for j in range(i + 1, 4):
        xys.append((i, j))
assert len(xys) == 6

orders = ("e", "g", "l", "u")


class Possibility:
    def __init__(self, js):
        cs = [orders[j] for j in js]
        self.ords = {}
        for i in range(6):
            x, y = xys[i]
            self.ords[(x, y)] = cs[i]

    def __repr__(self):
        return str(self.ords)


ps = []
for j0 in range(4):
    for j1 in range(4):
        for j2 in range(4):
            for j3 in range(4):
                for j4 in range(4):
                    for j5 in range(4):
                        ps.append(Possibility((j0, j1, j2, j3, j4, j5)))
ps = [p for p in ps if consistent(p.ords)]


def larger(x, y, ords):
    c = ords[(x, y)]
    if c == "l":
        return y
    if c == "e" or c == "g":
        return x


def answer(ords):
    # equal equations
    if ords[(0, 2)] == "e" and ords[(1, 3)] == "e":
        return "e"
    if ords[(0, 3)] == "e" and ords[(1, 2)] == "e":
        return "e"

    # get larger term in the first equation
    x = larger(0, 1, ords)
    if x is None:
        return "u"

    # get larger term in the second equation
    y = larger(2, 3, ords)
    if y is None:
        return "u"

    # compare the larger terms
    c = ords[(x, y)]
    if c != "e":
        return c

    # compare the smaller terms
    return ords[(x ^ 1, y ^ 1)]


def answerNP(ords):
    # equal equations compare by polarity
    if ords[(0, 2)] == "e" and ords[(1, 3)] == "e":
        return "g"
    if ords[(0, 3)] == "e" and ords[(1, 2)] == "e":
        return "g"

    # get larger term in the first equation
    x = larger(0, 1, ords)
    if x is None:
        return "u"

    # get larger term in the second equation
    y = larger(2, 3, ords)
    if y is None:
        return "u"

    # compare the larger terms
    c = ords[(x, y)]
    if c != "e":
        return c

    # compare polarity
    return "g"


def discrimination(xy, ps):
    cs = set()
    for p in ps:
        cs.add(p.ords[xy])
    return len(cs)


def printvar(x):
    print(("a", "b")[x >> 1], end=".")
    print(("left", "right")[x & 1], end="")


def printorder(c):
    if c == "e":
        print("EQUALS", end="")
    if c == "g":
        print("GREATER", end="")
    if c == "l":
        print("LESS", end="")
    if c == "u":
        print("UNORDERED", end="")


def gen(ps):
    # if we are down to just one possible answer, return that answer
    cs = set()
    for p in ps:
        cs.add(p.answer)
    if len(cs) == 1:
        print("return PartialOrder.", end="")
        printorder(cs.pop())
        print(";")
        return

    # compare the pair of terms with most discriminating power
    x, y = sorted(xys, key=lambda xy: discrimination(xy, ps), reverse=True)[0]
    print("switch(order.compare(", end="")
    printvar(x)
    print(",", end="")
    printvar(y)
    print(")) {")

    # go through the answers that comparison could return
    cs = set()
    for p in ps:
        cs.add(p.ords[(x, y)])
    cs = sorted(list(cs))
    for c in cs:
        print("case ", end="")
        printorder(c)
        print("-> {")
        ps1 = []
        for p in ps:
            if p.ords[(x, y)] == c:
                ps1.append(p)
        gen(ps1)
        print("}")

    print("}")


print("// AUTO GENERATED CODE - DO NOT MODIFY")
print("package olivine;")
print("public final class EquationComparison {")
print("private EquationComparison() {}")

for p in ps:
    p.answer = answer(p.ords)
print(
    "public static PartialOrder compare(KnuthBendixOrder order, Equation a, Equation b) {"
)
gen(ps)
print("throw new IllegalStateException();")
print("}")

for p in ps:
    p.answer = answerNP(p.ords)
print(
    "public static PartialOrder compareNP(KnuthBendixOrder order, Equation a, Equation b) {"
)
gen(ps)
print("throw new IllegalStateException();")
print("}")

print("}")
