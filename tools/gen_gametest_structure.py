#!/usr/bin/env python3
"""Generate the GameTest platform structure used by src/gametest.

The structure is a 5x5 stone floor at y=0 with air above (5x5x5 total). It is
written as gzipped NBT with DataVersion 3955 (1.21.1).

Run from the repository root:
    python3 tools/gen_gametest_structure.py
"""
from __future__ import annotations

import gzip
import struct
from pathlib import Path

DATA_VERSION = 3955
SIZE = (5, 5, 5)

# NBT tag ids
TAG_END = 0
TAG_BYTE = 1
TAG_INT = 3
TAG_LONG = 4
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10


def _name(value: str) -> bytes:
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def tag(tag_id: int, name: str, payload: bytes) -> bytes:
    return bytes([tag_id]) + _name(name) + payload


def string(value: str) -> bytes:
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def int_payload(value: int) -> bytes:
    return struct.pack(">i", value)


def list_payload(element_id: int, payloads: list[bytes]) -> bytes:
    return bytes([element_id]) + struct.pack(">i", len(payloads)) + b"".join(payloads)


def build() -> bytes:
    # Palette: index 0 = air, index 1 = stone.
    air = tag(TAG_COMPOUND, "", tag(TAG_STRING, "Name", string("minecraft:air")))
    stone = tag(TAG_COMPOUND, "", tag(TAG_STRING, "Name", string("minecraft:stone")))
    palette = tag(TAG_LIST, "palette", list_payload(TAG_COMPOUND, [air, stone]))

    # Blocks: one stone per column at y=0 (state index 1).
    blocks: list[bytes] = []
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            pos = tag(TAG_LIST, "pos", list_payload(TAG_INT, [int_payload(x), int_payload(0), int_payload(z)]))
            blocks.append(tag(TAG_COMPOUND, "", pos + tag(TAG_INT, "state", int_payload(1))))
    blocks_tag = tag(TAG_LIST, "blocks", list_payload(TAG_COMPOUND, blocks))
    entities = tag(TAG_LIST, "entities", list_payload(TAG_COMPOUND, []))

    root = (
        tag(TAG_INT, "DataVersion", int_payload(DATA_VERSION))
        + tag(TAG_LIST, "size", list_payload(TAG_INT, [int_payload(v) for v in SIZE]))
        + palette
        + blocks_tag
        + entities
    )
    return bytes([TAG_COMPOUND]) + _name("") + root + bytes([TAG_END])


def main() -> None:
    target = Path("src/main/resources/data/inf/structure/platform.nbt")
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(gzip.compress(build(), mtime=0))
    print(f"wrote {target} ({target.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
