import argparse
import subprocess
import sys


stdout = ""
stderr = ""


def prove(prover):
    global stdout, stderr
    cmd = prover + [args.infile]
    print(cmd)
    try:
        p = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        stdout, stderr = p.communicate(timeout=3)
        stdout = str(stdout, "utf-8")
        stderr = str(stderr, "utf-8")
        if stderr:
            print(stderr, end="")
            raise Exception(str(p.returncode))
        ys = stdout.splitlines()
        for y in ys:
            if y == "sat":
                print(y)
                return 1
            if "SZS status Satisfiable" in y:
                print(y)
                return 1
            if "SZS status CounterSatisfiable" in y:
                print(y)
                return 1
            if y == "unsat":
                print(y)
                return 0
            if "SZS status Unsatisfiable" in y:
                print(y)
                return 0
            if "SZS status Theorem" in y:
                print(y)
                return 0
        return -1
    except subprocess.TimeoutExpired:
        return -1


parser = argparse.ArgumentParser(
    description="Check that a problem causes two provers to give different answers"
)
parser.add_argument("prover1")
parser.add_argument("prover2")
parser.add_argument("infile")
args = parser.parse_args()

prover1 = args.prover1.split()
prover2 = args.prover2.split()

r1 = prove(prover1)
if r1 < 0:
    print(stdout)
r2 = prove(prover2)
if r2 < 0:
    print(stdout)
if r1 < 0 or r2 < 0:
    print("did not get two answers")
    exit(1)
if r1 == r2:
    print("answers are not different")
    exit(1)
print("ok")
