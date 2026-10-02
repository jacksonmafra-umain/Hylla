#!/usr/bin/env python3
"""Lays screenshots out in a labelled grid, each fitted into the same box.

    scripts/contact-sheet.py out.png COLUMNS BOX label=path [label=path ...]
"""
import sys
from PIL import Image, ImageDraw, ImageFont

out, cols, box, *cells = sys.argv[1:]
cols, box = int(cols), int(box)
font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", max(16, box // 18))
label_h, pad = box // 10, box // 25
rows = (len(cells) + cols - 1) // cols
sheet = Image.new("RGB", (cols * (box + pad) + pad, rows * (box + label_h + pad) + pad), "white")
draw = ImageDraw.Draw(sheet)
for k, cell in enumerate(cells):
    label, path = cell.split("=", 1)
    image = Image.open(path).convert("RGB")
    image.thumbnail((box, box))
    x = pad + (k % cols) * (box + pad)
    y = pad + (k // cols) * (box + label_h + pad)
    draw.text((x, y), label, fill="black", font=font)
    sheet.paste(image, (x + (box - image.width) // 2, y + label_h))
    draw.rectangle((x + (box - image.width) // 2 - 1, y + label_h - 1, x + (box + image.width) // 2, y + label_h + image.height), outline="#cccccc")
sheet.save(out, optimize=True)
print(out, sheet.size)
