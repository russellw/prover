def larger(x, y, ords):
    c = ords[(x, y)]
    if c == "<":
        return y
    if c == "=" or c == ">":
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
    g = set()
    for xy, c in ords.items():
        x, y = xy
        if c == ">":
            g.add((x, y))
        elif c == "<":
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
            self.answer = "?"
            return

        # get larger term in the second equation
        y = larger(2, 3, ords)
        if y is None:
            self.answer = "?"
            return

        # compare the larger terms
        c = ords[(x, y)]
        if c != "=":
            self.answer = c
            return

        # compare the smaller terms
        self.answer = ords[(x ^ 1, y ^ 1)]


xys = []
for i in range(4 - 1):
    for j in range(i + 1, 4):
        xys.append((i, j))
assert len(xys) == 6

orders = ("=", "<", ">", "?")
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
    if c == "=":
        print("EQUALS", end="")
    if c == ">":
        print("GREATER", end="")
    if c == "<":
        print("LESS", end="")
    if c == "?":
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
    x, y = sorted(xys, key=lambda xy: discrimination(xy, ps))[-1]
    print("switch(compare(", end="")
    printvar(x)
    print(",", end="")
    printvar(y)
    print(")) {")

    # go through the answers that comparison could return
    cs = set()
    for p in ps:
        cs.add(p.ords[(x, y)])
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
print("public static PartialOrder compare(Equation a, Equation b) {")
gen(ps)
print("}")
print("}")
