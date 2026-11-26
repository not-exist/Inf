#!/usr/bin/env python3
"""Print a free X display number.

A CI image may already have a server on :99, and a leftover /tmp/.X99-lock makes
Xvfb exit immediately, which looks like "Xvfb did not come up".
"""

from __future__ import annotations

import os
import sys


def main() -> int:
    for number in range(90, 130):
        if os.path.exists(f"/tmp/.X11-unix/X{number}") or os.path.exists(f"/tmp/.X{number}-lock"):
            continue
        print(number)
        return 0
    print("no free display number in 90..129", file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())