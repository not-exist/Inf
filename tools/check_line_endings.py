#!/usr/bin/env python3
"""Fail when a tracked text file is stored with CRLF in the repository.

A CRLF blob makes every path-, text- and hash-based check platform dependent:
scripts break on Linux, Gradle properties get a trailing \\r, and archive
hashes differ between operating systems. Git normalises on checkout via
.gitattributes, but the *stored* blob is what CI reads, so it is checked here.
"""

from __future__ import annotations

import subprocess
import sys

BATCH_SUFFIXES = (".bat", ".cmd")


def main() -> int:
    out = subprocess.run(
        ["git", "ls-files", "--eol"],
        check=True,
        capture_output=True,
        text=True,
    ).stdout

    offenders: list[tuple[str, str]] = []
    for line in out.splitlines():
        parts = line.split("\t", 1)
        if len(parts) != 2:
            continue
        meta, path = parts
        tokens = meta.split()
        if not tokens:
            continue
        index_eol = tokens[0]
        if index_eol != "i/crlf":
            continue
        if path.lower().endswith(BATCH_SUFFIXES):
            continue
        offenders.append((path, index_eol))

    if not offenders:
        print("line endings: all tracked text files are LF in the repository")
        return 0

    print(f"line endings: {len(offenders)} file(s) stored with CRLF:", file=sys.stderr)
    for path, eol in offenders:
        print(f"  {eol}  {path}", file=sys.stderr)
    print(
        "\nFix with:\n"
        "  git add --renormalize .\n"
        "  git commit -m 'chore: normalise line endings'\n"
        "and make sure .gitattributes keeps the rule that applies to these paths.",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())