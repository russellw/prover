def larger(x, y, ords):
    c = ords[(x, y)]
    if c == "l":
        return y
    if c == "e" or c == "g":
        return x
    return None


def cyclic(g):
    for x in range(4):
        visited = set()
        result = 0

        def rec(x):
            nonlocal result
            if x in visited:
                result = 1
                return
            visited.add(x)
            for y in range(4):
                if (x, y) in g:
                    rec(y)

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


class Possibility:
    def __init__(self, ords):
        self.ords = ords

        # get larger term in the first equation
        x = larger(0, 1, ords)
        if x is None:
            self.answer = "u"
            return

        # get larger term in the second equation
        y = larger(2, 3, ords)
        if y is None:
            self.answer = "u"
            return

        # compare the larger terms
        c = ords[(x, y)]
        if c != "e":
            self.answer = c
            return

        # compare the smaller terms
        self.answer = ords[(x ^ 1, y ^ 1)]

    def __repr__(self):
        return str(self.ords)


xys = []
for i in range(4 - 1):
    for j in range(i + 1, 4):
        xys.append((i, j))
assert len(xys) == 6

orders = ("e", "g", "l", "u")
ps = []
for i0 in range(4):
    for i1 in range(4):
        for i2 in range(4):
            for i3 in range(4):
                for i4 in range(4):
                    for i5 in range(4):
                        cs = [orders[i] for i in (i0, i1, i2, i3, i4, i5)]
                        ords = {}
                        for i in range(6):
                            x, y = xys[i]
                            ords[(x, y)] = cs[i]
                        ps.append(Possibility(ords))
ps = [p for p in ps if consistent(p.ords)]


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
print(
    "public static PartialOrder compare(KnuthBendixOrder order, Equation a, Equation b) {"
)
gen(ps)
print("}")
print("}")
