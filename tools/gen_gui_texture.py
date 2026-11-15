#!/usr/bin/env python3
"""Generate the forge GUI atlas. Run from the repository root."""
import os
import struct
import zlib

W = H = 256
PIXELS = [[(0, 0, 0, 0)] * W for _ in range(H)]

DEEP = (11, 9, 8, 255)
PANEL = (22, 17, 14, 255)
RAISED = (33, 25, 20, 255)
LINE_DARK = (5, 4, 3, 255)
LINE_MID = (58, 44, 34, 255)
BRASS = (201, 162, 39, 255)
BRASS_HI = (240, 211, 122, 255)
EMBER = (232, 98, 44, 255)
RISK = (209, 69, 59, 255)
WOOD = (138, 106, 63, 255)


def rect(x, y, w, h, color):
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            if 0 <= xx < W and 0 <= yy < H:
                PIXELS[yy][xx] = color


def panel():
    rect(0, 0, 176, 222, LINE_DARK)
    rect(2, 2, 172, 218, PANEL)
    rect(4, 4, 168, 214, PANEL)
    rect(4, 4, 168, 1, LINE_MID)
    rect(4, 4, 1, 214, LINE_MID)
    rect(4, 217, 168, 1, DEEP)
    rect(171, 4, 1, 214, DEEP)
    rect(8, 20, 160, 1, BRASS)
    rect(8, 21, 160, 1, LINE_MID)
    rect(8, 139, 160, 1, LINE_MID)
    rect(8, 194, 160, 1, LINE_MID)
    for (x, y) in ((8, 8), (164, 8), (8, 212), (164, 212)):
        rect(x, y, 2, 2, BRASS)
    for row in range(3):
        for column in range(9):
            slot(8 + column * 18, 140 + row * 18, LINE_DARK)
    for column in range(9):
        slot(8 + column * 18, 198, LINE_DARK)


def slot(x, y, border):
    rect(x, y, 18, 18, LINE_DARK)
    rect(x + 1, y + 1, 16, 16, DEEP)
    rect(x + 3, y + 3, 12, 12, PANEL)
    rect(x + 3, y + 3, 12, 1, LINE_DARK)
    rect(x + 3, y + 3, 1, 12, LINE_DARK)
    rect(x + 14, y + 3, 1, 12, LINE_MID)
    rect(x + 3, y + 14, 12, 1, LINE_MID)
    rect(x, y, 18, 1, border)
    rect(x, y + 17, 18, 1, border)
    rect(x, y, 1, 18, border)
    rect(x + 17, y, 1, 18, border)


def button(x, y, w, h, fill, border, underline):
    rect(x, y, w, h, border)
    rect(x + 1, y + 1, w - 2, h - 2, fill)
    rect(x + 1, y + 1, w - 2, 1, RAISED)
    if underline:
        rect(x + 1, y + h - 2, w - 2, 1, underline)


def bar(x, y, w, h):
    rect(x, y, w, h, LINE_DARK)
    rect(x + 1, y + 1, w - 2, h - 2, RAISED)
    rect(x + 1, y + 1, w - 2, 1, LINE_DARK)
    rect(x + 1, y + 1, 1, h - 2, LINE_DARK)
    rect(x + 1, y + h - 2, w - 2, 1, LINE_MID)
    rect(x + w - 2, y + 1, 1, h - 2, LINE_MID)


def icons():
    # anvil
    rect(176, 22, 8, 2, BRASS)
    rect(178, 24, 4, 2, BRASS)
    rect(176, 26, 8, 2, BRASS)
    # hammer
    rect(188, 20, 2, 8, WOOD)
    rect(186, 20, 6, 3, BRASS_HI)
    # flame
    rect(200, 25, 4, 3, EMBER)
    rect(201, 22, 2, 3, EMBER)
    rect(199, 26, 2, 2, EMBER)


def main():
    panel()
    slot(176, 0, LINE_DARK)
    slot(196, 0, BRASS)
    slot(216, 0, RISK)
    icons()

    states = [
        (32, RAISED, LINE_DARK, None),
        (54, RAISED, BRASS, EMBER),
        (76, PANEL, BRASS, None),
    ]
    for y, fill, border, underline in states:
        primary = BRASS if y == 32 else border
        button(176, y, 72, 20, fill, primary, underline)
        button(176, y + 66, 36, 20, fill, border, underline)
        button(176, y + 132, 28, 20, fill, border, underline)

    bar(0, 230, 116, 6)
    bar(0, 242, 116, 4)

    raw = bytearray()
    for row in PIXELS:
        raw.append(0)
        for pixel in row:
            raw.extend(pixel)
    compressed = zlib.compress(bytes(raw), 9)

    def chunk(kind, data):
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", compressed)
    png += chunk(b"IEND", b"")
    os.makedirs("src/main/resources/assets/inf/textures/gui", exist_ok=True)
    with open("src/main/resources/assets/inf/textures/gui/forge_table.png", "wb") as handle:
        handle.write(png)


if __name__ == "__main__":
    main()
