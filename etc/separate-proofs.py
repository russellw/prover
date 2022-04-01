import argparse
import re

parser = argparse.ArgumentParser(
    description="Turn combined log file into separate proof files"
)
parser.add_argument("logfile")
args = parser.parse_args()

outf = None
f = open(args.logfile)
for s in f.readlines():
    m = re.match(r"% File\s*:\s*([A-Z][A-Z][A-Z]\d\d\d\S*)", s)
    if m:
        filename = m[1]
        continue
    if s.startswith("% SZS output start CNFRefutation"):
        outf = open(filename + "-proof.p", "wb")
        continue
    if s.startswith("% SZS output end CNFRefutation"):
        outf.close()
        outf = None
        filename = None
        continue
    if outf:
        outf.write(s.encode(encoding="UTF-8"))
