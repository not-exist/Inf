#!/usr/bin/env python3
"""Fail when two tracked paths differ only by case.

The repository is developed on Linux, where `Foo.java` and `foo.java` are two
files. Windows and macOS check out onto case-insensitive filesystems, where one
silently overwrites the other and the build differs from CI. Such a path pair
is legal in git and invisible until a Windows run breaks, so it is gated here.
"""

from __future__ import annotations

import collections
import subprocess
import sys


def main() -> int:
    out = subprocess.run(
        ["git", "ls-files"],
        check=True,
        capture_output=True,
        text=True,
    ).stdout

    by_lower: dict[str, list[str]] = collections.defaultdict(list)
    for path in out.splitlines():
        if not path:
            continue
        by_lower[path.lower()].append(path)

    collisions = {key: paths for key, paths in by_lower.items() if len(paths) > 1}

    if not collisions:
        print("case collisions: none")
        return 0

    print(f"case collisions: {len(collisions)} group(s)", file=sys.stderr)
    for key in sorted(collisions):
        print(f"  {key}", file=sys.stderr)
        for path in sorted(collisions[key]):
            print(f"    {path}", file=sys.stderr)
    print(
        "\nRename one path in each group so that the repository is checkout-safe on\n"
        "case-insensitive filesystems (Windows, default macOS volumes).",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())