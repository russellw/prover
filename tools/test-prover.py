import argparse
import inspect
import subprocess
import re
import os
import sys
import logging

logger = logging.getLogger()
logger.addHandler(logging.StreamHandler(sys.stdout))
logger.setLevel(logging.DEBUG)


def trace(a):
    info = inspect.getframeinfo(inspect.currentframe().f_back)
    logger.debug(f"{info.filename}:{info.function}:{info.lineno}: {repr(a)}")


def hasProof(xs):
    for x in xs:
        if "SZS output start CNFRefutation" in x:
            return 1


parser = argparse.ArgumentParser(description="Test prover")
parser.add_argument("prover")
parser.add_argument("problems")
args = parser.parse_args()

prover = args.prover.split()
problems = args.problems
if problems.endswith(".lst"):
    problems = [s.rstrip() for s in open(problems)]
for filename in problems:
    pname = os.path.basename(os.path.splitext(filename)[0])
    print(pname, end=" ")
    sys.stdout.flush()
    cmd = prover + [filename]
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
    stdout = stdout.split("\n")
    for x in stdout:
        m = re.match(r".*SZS status (\w+)", x)
        if m:
            print(m[1], end=" ")
    if hasProof(stdout):
        print("*", end="")
        with open(pname + "-proof.p", "w") as f:
            for x in stdout:
                f.write(x + "\n")
    print()
