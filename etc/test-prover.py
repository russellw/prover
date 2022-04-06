import argparse
import time
import inspect
import subprocess
import re
import os
import sys
import random
import logging

try:
    os.nice(20)
except AttributeError:
    # Python on Windows doesn't have 'nice'
    pass

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


def meaning(s):
    if s in ("sat", "unsat"):
        return s
    if s == "Satisfiable":
        return "sat"
    if s == "CounterSatisfiable":
        return "sat"
    if s == "Unsatisfiable":
        return "unsat"
    if s == "Theorem":
        return "unsat"
    if s == "ContradictoryAxioms":
        return "unsat"


def hasProof(xs):
    for x in xs:
        if "SZS output start CNFRefutation" in x:
            return 1


parser = argparse.ArgumentParser(description="Test prover")
parser.add_argument("prover")
parser.add_argument("problems")
parser.add_argument("-b", "--batch", help="batch size limit")
parser.add_argument(
    "-o", "--output-solved", help="output list of solved problems to a file"
)
parser.add_argument("-p", "--proof", help="extract proofs", action="store_true")
parser.add_argument("-s", "--shuffle", help="shuffle problem list", action="store_true")
parser.add_argument("-t", "--cpu-limit", help="time limit per problem")
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
else:
    problems = [problems]

if args.shuffle:
    random.seed(0)
    random.shuffle(problems)

timeout = 60.0
if args.cpu_limit:
    timeout = float(args.cpu_limit)

attempted = 0
solved = 0
for filename in problems:
    if "^" in filename:
        continue
    if args.batch and attempted == int(args.batch):
        break
    attempted += 1
    pname = os.path.basename(os.path.splitext(filename)[0])
    expected = getExpected(filename)
    print(pname, end="\t")
    print(expected, end="\t")
    sys.stdout.flush()
    cmd = prover + [filename]
    start = time.time()
    stderr = ""
    try:
        p = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        stdout, stderr = p.communicate(timeout=timeout)
        stdout = str(stdout, "utf-8")
        stderr = str(stderr, "utf-8")
        stdout = stdout.split("\n")

        result = "-"
        for x in stdout:
            if x in ("sat", "unsat"):
                result = x
                break
            m = re.match(r".*SZS status (\w+)", x)
            if m:
                result = m[1]
                break
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

        if meaning(result):
            solved += 1
            if args.output_solved:
                osf = open(args.output_solved, "a")
                osf.write(filename + "\n")
    except subprocess.TimeoutExpired:
        p.kill()
        print("Timeout", end="\t")
    print("%.3f" % (time.time() - start))
    if stderr:
        print(stderr, end="")
print("%d/%d" % (solved, attempted))
