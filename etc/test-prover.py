import argparse
import time
import inspect
import subprocess
import re
import os
import sys
import random
import logging

logger = logging.getLogger()
logger.addHandler(logging.StreamHandler(sys.stdout))
logger.setLevel(logging.DEBUG)


def trace(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    logger.debug(f"{info.filename}:{info.function}:{info.lineno}: {repr(a)}")


def getExpected(filename):
    for s in open(filename):
        m = re.match(r"%\s*Status\s*:\s*(\w+)", s)
        if m:
            return m[1]
    return "-"


def meaning(szs):
    if szs == "Satisfiable":
        return "sat"
    if szs == "CounterSatisfiable":
        return "sat"
    if szs == "Unsatisfiable":
        return "unsat"
    if szs == "Theorem":
        return "unsat"
    if szs == "ContradictoryAxioms":
        return "unsat"


def hasProof(xs):
    for x in xs:
        if "SZS output start CNFRefutation" in x:
            return 1


parser = argparse.ArgumentParser(description="Test prover")
parser.add_argument("prover")
parser.add_argument("problems")
parser.add_argument("-b", "--batch", help="batch size limit")
parser.add_argument("-p", "--proof", help="extract proofs", action="store_true")
parser.add_argument("-q", "--quiet", help="suppress errors", action="store_true")
parser.add_argument("-s", "--shuffle", help="shuffle problem list", action="store_true")
args = parser.parse_args()

prover = args.prover.split()
problems = args.problems

if problems.casefold() == "tptp".casefold():
    tptp = os.getenv("TPTP")
    problems = []
    for root, dirs, files in os.walk(tptp):
        for filename in files:
            if os.path.splitext(filename)[1] == ".p":
                problems.append(os.path.join(root, filename))
elif problems.endswith(".lst"):
    problems = [s.rstrip() for s in open(problems)]


if args.shuffle:
    random.seed(0)
    random.shuffle(problems)

attempted = 0
solved = 0
for filename in problems:
    if "^" in filename:
        continue
    if args.batch and attempted == args.batch:
        break
    attempted += 1
    pname = os.path.basename(os.path.splitext(filename)[0])
    expected = getExpected(filename)
    print(pname, end="\t")
    print(expected, end="\t")
    sys.stdout.flush()
    cmd = prover + [filename]
    start = time.time()
    try:
        p = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        stdout, stderr = p.communicate(timeout=60)
        stdout = str(stdout, "utf-8")
        stderr = str(stderr, "utf-8")
        if not args.quiet and stderr:
            print(stderr, end="")
            raise Exception(str(p.returncode))
        stdout = stdout.split("\n")

        result = "-"
        for x in stdout:
            m = re.match(r".*SZS status (\w+)", x)
            if m:
                result = m[1]
        print(result, end="\t")

        if (
            meaning(expected)
            and meaning(result)
            and meaning(expected) != meaning(result)
        ):
            raise Exception(result)

        if args.proof and hasProof(stdout):
            print("*", end="")
            with open(pname + "-proof.p", "w") as f:
                for x in stdout:
                    f.write(x + "\n")
    except subprocess.TimeoutExpired:
        print("Timeout", end="\t")
    print("%.3f" % (time.time() - start))
print("%d/%d" % (solved, attempted))
