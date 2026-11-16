#!/usr/bin/env python3
"""Generate the custom GUI, block, item and advancement art for Inf."""
import os
import random
from PIL import Image, ImageDraw

ROOT = "src/main/resources/assets/inf/textures"
OUTLINE = (11, 9, 8, 255)
STEEL = (200, 205, 210, 255)
STEEL_HI = (236, 240, 244, 255)
STEEL_DARK = (118, 126, 134, 255)
IRON = (138, 143, 148, 255)
IRON_HI = (176, 181, 186, 255)
WOOD = (107, 74, 42, 255)
WOOD_HI = (143, 103, 62, 255)
BRASS = (201, 162, 39, 255)
BRASS_HI = (240, 211, 122, 255)
EMBER = (232, 98, 44, 255)
EMBER_HI = (255, 177, 92, 255)
DEEP = (11, 9, 8, 255)
PANEL_TOP = (30, 23, 19, 255)
PANEL_BOTTOM = (16, 12, 10, 255)
LINE_MID = (58, 44, 34, 255)
RED = (209, 69, 59, 255)
GREEN = (143, 163, 90, 255)
AMBER = (217, 164, 65, 255)


def ensure(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(4))


def save(img, path):
    ensure(path)
    img.save(path)


def panel(d):
    for y in range(260):
        d.line([(0, y), (419, y)], fill=lerp(PANEL_TOP, PANEL_BOTTOM, y / 259.0))
    rnd = random.Random(11)
    for _ in range(4200):
        x, y = rnd.randrange(4, 416), rnd.randrange(4, 256)
        shade = rnd.choice([(255, 255, 255, 8), (0, 0, 0, 14)])
        d.point((x, y), fill=shade)
    d.rectangle([0, 0, 419, 259], outline=OUTLINE, width=4)
    d.rectangle([4, 4, 415, 255], outline=LINE_MID, width=1)
    d.rectangle([6, 6, 413, 253], outline=(5, 4, 3, 255), width=1)
    d.line([(12, 30), (407, 30)], fill=BRASS, width=1)
    d.line([(12, 31), (407, 31)], fill=LINE_MID, width=1)
    for x, y in ((10, 8), (404, 8), (10, 246), (404, 246)):
        d.rectangle([x, y, x + 3, y + 3], fill=BRASS)
        d.point((x + 1, y + 1), fill=BRASS_HI)
    # alcove, centre plate and inventory plate
    d.rectangle([14, 36, 146, 170], fill=(14, 11, 9, 255), outline=(5, 4, 3, 255))
    d.rectangle([15, 37, 145, 169], outline=LINE_MID)
    d.rectangle([148, 36, 244, 170], fill=(20, 15, 12, 255), outline=(5, 4, 3, 255))
    d.rectangle([149, 37, 243, 169], outline=LINE_MID)
    d.rectangle([242, 174, 412, 258], fill=(14, 11, 9, 255), outline=(5, 4, 3, 255))
    d.rectangle([243, 175, 411, 257], outline=LINE_MID)
    d.line([(246, 38), (246, 168)], fill=LINE_MID)
    d.line([(12, 256), (407, 256)], fill=BRASS)
    for bx, by in ((18, 40), (142, 40), (18, 166), (142, 166), (152, 40), (240, 40), (152, 166), (240, 166)):
        d.rectangle([bx, by, bx + 2, by + 2], fill=BRASS)
    # forge glow behind the anvil
    glow = Image.new("RGBA", (420, 260), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([20, 58, 168, 190], fill=(232, 98, 44, 26))
    gd.ellipse([48, 82, 142, 170], fill=(255, 161, 72, 30))
    gd.ellipse([30, 64, 158, 186], outline=(232, 98, 44, 46), width=2)
    gd.ellipse([20, 52, 168, 198], outline=(232, 98, 44, 22), width=1)
    img = Image.new("RGBA", (420, 260), (0, 0, 0, 0))
    img.alpha_composite(glow)
    base = ImageDraw.Draw(img)
    # anvil silhouette
    base.polygon([(56, 118), (128, 118), (140, 128), (128, 136), (118, 136), (122, 150), (62, 150), (66, 136), (56, 128)], fill=(42, 46, 50, 255), outline=OUTLINE)
    base.rectangle([62, 112, 122, 118], fill=(74, 80, 86, 255), outline=OUTLINE)
    base.rectangle([66, 114, 120, 116], fill=(104, 112, 119, 255))
    base.polygon([(56, 118), (40, 124), (56, 128)], fill=(62, 68, 74, 255), outline=OUTLINE)
    base.rectangle([70, 150, 116, 156], fill=(34, 38, 42, 255), outline=OUTLINE)
    base.rectangle([76, 156, 110, 160], fill=(26, 29, 32, 255), outline=OUTLINE)
    for i in range(8):
        x = 60 + i * 9
        base.line([(x, 121), (x + 4, 133)], fill=(86, 92, 98, 255))
    # hammer resting on the anvil
    base.rectangle([96, 88, 100, 112], fill=WOOD, outline=OUTLINE)
    base.rectangle([97, 89, 99, 111], fill=WOOD_HI)
    base.rectangle([86, 84, 110, 92], fill=(74, 80, 86, 255), outline=OUTLINE)
    base.rectangle([88, 86, 108, 90], fill=BRASS, outline=None)
    base.rectangle([88, 87, 108, 88], fill=BRASS_HI)
    # forge arch and chains
    base.arc([22, 42, 142, 182], 180, 360, fill=LINE_MID, width=2)
    base.line([(22, 112), (22, 170)], fill=LINE_MID)
    base.line([(142, 112), (142, 170)], fill=LINE_MID)
    base.line([(40, 42), (40, 66)], fill=(58, 44, 34, 255))
    base.line([(124, 42), (124, 66)], fill=(58, 44, 34, 255))
    base.arc([34, 54, 130, 150], 200, 340, fill=(58, 44, 34, 140), width=1)
    # crossed hammers emblem in the lower-left
    base.line([(58, 182), (106, 204)], fill=(74, 58, 42, 255), width=4)
    base.line([(106, 182), (58, 204)], fill=(74, 58, 42, 255), width=4)
    base.rectangle([48, 178, 62, 185], fill=(94, 74, 50, 255), outline=OUTLINE)
    base.rectangle([98, 178, 112, 185], fill=(94, 74, 50, 255), outline=OUTLINE)
    base.line([(52, 180), (58, 184)], fill=BRASS)
    base.line([(108, 180), (102, 184)], fill=BRASS)
    # embers
    rnd = random.Random(23)
    for _ in range(38):
        x, y = rnd.randrange(38, 152), rnd.randrange(64, 168)
        size = rnd.choice([1, 1, 2])
        color = rnd.choice([EMBER, EMBER_HI, (255, 220, 160, 255)])
        base.rectangle([x, y, x + size - 1, y + size - 1], fill=color)
    # slot recesses
    for sx, sy in ((24, 48), (24, 78), (24, 108)):
        slot(base, sx, sy, (5, 4, 3, 255))
    # value bar frame
    bar(base, 150, 72, 90, 8)
    base.line([(150, 156), (238, 156)], fill=LINE_MID)
    # right chance panel inset
    base.rectangle([250, 34, 409, 167], fill=(16, 12, 10, 210), outline=(5, 4, 3, 255))
    base.line([(251, 35), (408, 35)], fill=LINE_MID)
    base.line([(251, 36), (408, 36)], fill=(5, 4, 3, 255))
    # player inventory frames
    for row in range(3):
        for col in range(9):
            slot(base, 246 + col * 18, 178 + row * 18, (5, 4, 3, 255))
    for col in range(9):
        slot(base, 246 + col * 18, 236, (5, 4, 3, 255))
    base.line([(246, 172), (408, 172)], fill=LINE_MID)
    return img


def slot(d, x, y, border):
    d.rectangle([x, y, x + 17, y + 17], fill=(5, 4, 3, 255))
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=(11, 9, 8, 255))
    d.rectangle([x + 3, y + 3, x + 14, y + 14], fill=(22, 17, 14, 255))
    d.line([(x + 3, y + 3), (x + 14, y + 3)], fill=(5, 4, 3, 255))
    d.line([(x + 3, y + 3), (x + 3, y + 14)], fill=(5, 4, 3, 255))
    d.line([(x + 14, y + 3), (x + 14, y + 14)], fill=LINE_MID)
    d.line([(x + 3, y + 14), (x + 14, y + 14)], fill=LINE_MID)
    d.rectangle([x, y, x + 17, y + 17], outline=border)


def bar(d, x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=(5, 4, 3, 255))
    d.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=(33, 25, 20, 255))
    d.line([(x + 1, y + 1), (x + w - 2, y + 1)], fill=(5, 4, 3, 255))
    d.line([(x + 1, y + h - 2), (x + w - 2, y + h - 2)], fill=LINE_MID)


def buttons(d):
    states = [
        (270, (33, 25, 20, 255), LINE_MID, None),
        (298, (43, 34, 27, 255), BRASS, EMBER),
        (326, (18, 14, 12, 255), BRASS, None),
    ]
    for y, fill, border, underline in states:
        btn(d, 0, y, 100, 26, fill, BRASS if y == 270 else border, underline)
        btn(d, 110, y, 60, 26, fill, border, underline)
        btn(d, 180, y, 56, 26, fill, border, underline)


def btn(d, x, y, w, h, fill, border, underline):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=border)
    d.rectangle([x + 2, y + 2, x + w - 3, y + h - 3], fill=fill)
    d.line([(x + 2, y + 2), (x + w - 3, y + 2)], fill=(61, 49, 39, 255))
    if underline:
        d.line([(x + 2, y + h - 3), (x + w - 3, y + h - 3)], fill=underline)


def slot_states(d):
    for i, border in enumerate([(5, 4, 3, 255), BRASS, RED]):
        slot(d, 420 + i * 22, 0, border)


def icons(d):
    # anvil 16x16
    d.rectangle([420, 26, 435, 29], fill=BRASS)
    d.rectangle([423, 30, 432, 33], fill=BRASS)
    d.rectangle([420, 34, 435, 38], fill=BRASS)
    # hammer
    d.rectangle([442, 24, 444, 38], fill=WOOD)
    d.rectangle([438, 24, 449, 29], fill=BRASS_HI)
    # flame
    d.polygon([(464, 38), (462, 32), (466, 27), (468, 33), (471, 29), (472, 36), (468, 39)], fill=EMBER)
    d.polygon([(466, 37), (465, 33), (468, 32), (469, 36)], fill=EMBER_HI)


def gui_atlas():
    img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    panel_img = panel(d)
    img.alpha_composite(panel_img)
    slot_states(d)
    icons(d)
    buttons(d)
    bar(d, 0, 350, 90, 8)
    save(img, f"{ROOT}/gui/forge_table.png")


def item(name, draw):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw(d)
    save(img, f"{ROOT}/item/{name}.png")


def blank_blade(d, kind):
    if kind == "sword":
        d.polygon([(7, 1), (8, 1), (9, 2), (9, 9), (8, 10), (7, 10), (6, 9), (6, 2)], fill=IRON, outline=OUTLINE)
        d.line([(7, 2), (7, 8)], fill=IRON_HI)
        d.rectangle([5, 10, 10, 11], fill=(96, 101, 106, 255), outline=OUTLINE)
        d.rectangle([7, 11, 8, 14], fill=WOOD, outline=OUTLINE)
        d.point((6, 4), fill=OUTLINE)
        d.point((8, 7), fill=OUTLINE)
    elif kind == "dagger":
        d.polygon([(7, 5), (8, 5), (9, 6), (9, 9), (8, 10), (7, 10), (6, 9), (6, 6)], fill=IRON, outline=OUTLINE)
        d.line([(7, 6), (7, 9)], fill=IRON_HI)
        d.rectangle([5, 10, 10, 10], fill=(96, 101, 106, 255), outline=OUTLINE)
        d.rectangle([7, 11, 8, 13], fill=WOOD, outline=OUTLINE)
        d.point((6, 7), fill=OUTLINE)
    elif kind == "pickaxe":
        d.line([(4, 3), (11, 3)], fill=IRON, width=2)
        d.point((3, 4), fill=IRON); d.point((12, 2), fill=IRON)
        d.line([(4, 2), (4, 4)], fill=OUTLINE); d.line([(11, 2), (11, 4)], fill=OUTLINE)
        d.rectangle([7, 4, 8, 14], fill=WOOD, outline=OUTLINE)
        d.point((4, 3), fill=IRON_HI)
    elif kind == "axe":
        d.polygon([(4, 2), (9, 3), (10, 7), (4, 8), (2, 5)], fill=IRON, outline=OUTLINE)
        d.line([(4, 3), (8, 4)], fill=IRON_HI)
        d.rectangle([8, 4, 9, 14], fill=WOOD, outline=OUTLINE)
        d.point((3, 6), fill=OUTLINE)
    elif kind == "scythe":
        d.arc([2, 1, 13, 9], 200, 340, fill=IRON, width=2)
        d.line([(8, 6), (8, 14)], fill=WOOD, width=2)
        d.line([(8, 7), (8, 13)], fill=WOOD_HI)
        d.point((4, 3), fill=IRON_HI)


def forged_blade(d, kind):
    if kind == "sword":
        d.polygon([(7, 1), (8, 1), (9, 2), (9, 9), (8, 10), (7, 10), (6, 9), (6, 2)], fill=STEEL, outline=OUTLINE)
        d.line([(7, 2), (7, 8)], fill=STEEL_HI)
        d.rectangle([5, 10, 10, 11], fill=BRASS, outline=OUTLINE)
        d.rectangle([7, 11, 8, 14], fill=WOOD, outline=OUTLINE)
        d.point((7, 14), fill=BRASS_HI)
        d.point((6, 10), fill=BRASS_HI)
    elif kind == "dagger":
        d.polygon([(7, 5), (8, 5), (9, 6), (9, 9), (8, 10), (7, 10), (6, 9), (6, 6)], fill=STEEL, outline=OUTLINE)
        d.line([(7, 6), (7, 9)], fill=STEEL_HI)
        d.rectangle([5, 10, 10, 10], fill=BRASS, outline=OUTLINE)
        d.rectangle([7, 11, 8, 13], fill=WOOD, outline=OUTLINE)
        d.point((7, 13), fill=BRASS_HI)
    elif kind == "pickaxe":
        d.line([(4, 3), (11, 3)], fill=STEEL, width=2)
        d.point((3, 4), fill=STEEL); d.point((12, 2), fill=STEEL)
        d.line([(4, 2), (4, 4)], fill=OUTLINE); d.line([(11, 2), (11, 4)], fill=OUTLINE)
        d.line([(5, 3), (10, 3)], fill=STEEL_HI)
        d.rectangle([7, 4, 8, 14], fill=WOOD, outline=OUTLINE)
    elif kind == "axe":
        d.polygon([(4, 2), (9, 3), (10, 7), (4, 8), (2, 5)], fill=STEEL, outline=OUTLINE)
        d.line([(4, 3), (8, 4)], fill=STEEL_HI)
        d.line([(4, 2), (4, 8)], fill=STEEL_HI)
        d.rectangle([8, 4, 9, 14], fill=WOOD, outline=OUTLINE)
    elif kind == "scythe":
        d.arc([2, 1, 13, 9], 200, 340, fill=STEEL, width=2)
        d.line([(4, 4), (9, 6)], fill=STEEL_HI)
        d.line([(8, 6), (8, 14)], fill=WOOD, width=2)
        d.line([(8, 7), (8, 13)], fill=WOOD_HI)


def hammer_item(d):
    d.rectangle([7, 7, 8, 14], fill=WOOD, outline=OUTLINE)
    d.line([(8, 8), (8, 13)], fill=WOOD_HI)
    d.rectangle([3, 3, 12, 7], fill=STEEL, outline=OUTLINE)
    d.line([(4, 3), (11, 3)], fill=STEEL_HI)
    d.rectangle([4, 4, 4, 6], fill=BRASS)
    d.rectangle([11, 4, 11, 6], fill=BRASS)
    d.rectangle([6, 6, 9, 7], fill=STEEL_DARK)


def block_texture(d):
    d.rectangle([0, 0, 15, 15], fill=(38, 32, 28, 255), outline=OUTLINE)
    for y in range(1, 15):
        d.line([(1, y), (14, y)], fill=(48, 40, 35, 255) if y % 3 else (30, 25, 22, 255))
    d.rectangle([1, 1, 14, 2], fill=(74, 64, 56, 255))
    d.rectangle([2, 5, 13, 9], fill=(18, 14, 12, 255), outline=OUTLINE)
    d.rectangle([3, 6, 12, 8], fill=(60, 24, 14, 255))
    d.line([(4, 7), (11, 7)], fill=EMBER)
    d.point((6, 7), fill=EMBER_HI)
    d.point((9, 7), fill=EMBER_HI)
    for x, y in ((2, 11), (12, 11), (2, 3), (12, 3)):
        d.rectangle([x, y, x + 1, y + 1], fill=BRASS)


def advancement_bg(d):
    d.rectangle([0, 0, 15, 15], fill=(24, 19, 16, 255))
    for y in range(16):
        d.line([(0, y), (15, y)], fill=(30, 24, 20, 255) if y % 4 else (19, 15, 13, 255))
    d.point((4, 5), fill=EMBER)
    d.point((11, 9), fill=(109, 56, 32, 255))
    d.point((7, 12), fill=(72, 40, 26, 255))


def main():
    gui_atlas()
    item("hammer", hammer_item)
    for kind in ("sword", "pickaxe", "axe", "scythe", "dagger"):
        item(f"{kind}_blank", lambda d, k=kind: blank_blade(d, k))
        item(f"forged_{kind}", lambda d, k=kind: forged_blade(d, k))
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    block_texture(ImageDraw.Draw(img))
    save(img, f"{ROOT}/block/forge_table.png")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    advancement_bg(ImageDraw.Draw(img))
    save(img, f"{ROOT}/gui/advancement_background.png")


if __name__ == "__main__":
    main()
