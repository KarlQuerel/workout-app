"""Regenerate the launcher icon layers, notification icon and Play Store graphics.

Usage: uv run --with fonttools --with pillow python tools/store_assets.py
"""

import io

from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont
from PIL import Image, ImageDraw, ImageFont

ARCHIVO = "tools/fonts/Archivo-Italic.ttf"
GEIST = "app/src/main/res/font/geist.ttf"
DRAWABLE = "app/src/main/res/drawable"

DAWN = ((0xFF, 0xB0, 0x20), (0xFF, 0x5A, 0x1F))
INK = (0x1B, 0x0A, 0x00)
CREAM = (0xFF, 0xF4, 0xE0)
APP_BG = (0x0B, 0x0C, 0x0E)
APP_INK = (0xF4, 0xF5, 0xF6)
APP_DIM = (0xA3, 0xA8, 0xAE)

# The KG mark as designed on the 108-unit adaptive canvas.
TEXT = "KG"
AXES = {"wdth": 118, "wght": 900}
TEXT_SIZE, TEXT_X, BASELINE = 46, 55, 70
DOT_X, DOT_Y, DOT_R = 82, 30, 5
CENTER = 54
# Launchers mask to a 66-unit circle; the mark is shrunk until it fits inside this radius.
SAFE_RADIUS = 32
# The launcher shows the middle 72 units of the canvas, so the Play icon crops the same way.
VISIBLE = (18, 90)


def fmt(v):
    return f"{v:.2f}".rstrip("0").rstrip(".")


def instance(path, axes):
    return instantiateVariableFont(TTFont(path), axes)


def pil_font(tt, size):
    buf = io.BytesIO()
    tt.save(buf)
    buf.seek(0)
    return ImageFont.truetype(buf, size)


class Mark:
    """Glyph outlines and dot of the icon, fitted into the safe circle."""

    def __init__(self, font):
        self.font = font
        cmap = font.getBestCmap()
        hmtx = font["hmtx"]
        self.glyphs = [cmap[ord(c)] for c in TEXT]
        self.offsets, x = [], 0
        for g in self.glyphs:
            self.offsets.append(x)
            x += hmtx[g][0]
        self.scale = TEXT_SIZE / font["head"].unitsPerEm
        self.left = TEXT_X - x * self.scale / 2
        self.k = 1.0
        self.k = min(1.0, SAFE_RADIUS / self._reach())

    def _transform(self, offset):
        ks = self.k * self.scale
        e = CENTER + self.k * (self.left + offset * self.scale - CENTER)
        f = CENTER + self.k * (BASELINE - CENTER)
        return (ks, 0, 0, -ks, e, f)

    def _draw(self, pen_for):
        glyph_set = self.font.getGlyphSet()
        for g, offset in zip(self.glyphs, self.offsets):
            glyph_set[g].draw(TransformPen(pen_for(), self._transform(offset)))

    def bounds(self):
        pen = BoundsPen(self.font.getGlyphSet())
        self._draw(lambda: pen)
        x0, y0, x1, y1 = pen.bounds
        cx, cy, r = self.dot()
        return min(x0, cx - r), min(y0, cy - r), max(x1, cx + r), max(y1, cy + r)

    def _reach(self):
        x0, y0, x1, y1 = self.bounds()
        corners = [(x0, y0), (x1, y0), (x0, y1), (x1, y1)]
        return max(((x - CENTER) ** 2 + (y - CENTER) ** 2) ** 0.5 for x, y in corners)

    def text_path(self):
        pen = SVGPathPen(self.font.getGlyphSet(), ntos=fmt)
        self._draw(lambda: pen)
        return pen.getCommands()

    def dot(self):
        return (
            CENTER + self.k * (DOT_X - CENTER),
            CENTER + self.k * (DOT_Y - CENTER),
            self.k * DOT_R,
        )

    def dot_path(self):
        cx, cy, r = self.dot()
        return (f"M{fmt(cx - r)},{fmt(cy)}a{fmt(r)},{fmt(r)} 0 1,0 {fmt(2 * r)},0"
                f"a{fmt(r)},{fmt(r)} 0 1,0 -{fmt(2 * r)},0z")


def argb(rgb):
    return "#FF" + "".join(f"{c:02X}" for c in rgb)


def vector(paths, size="108dp", viewport=(0, 0, 108, 108), comment=None):
    x, y, w, h = viewport
    body = "".join(
        f'\t\t<path\n\t\t\tandroid:fillColor="{color}"\n\t\t\tandroid:pathData="{data}" />\n'
        for color, data in paths
    )
    note = f"\t<!-- {comment} -->\n" if comment else ""
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'\tandroid:width="{size}"\n\tandroid:height="{size}"\n'
        f'\tandroid:viewportWidth="{fmt(w)}"\n\tandroid:viewportHeight="{fmt(h)}">\n'
        f"{note}"
        f'\t<group\n\t\tandroid:translateX="{fmt(-x)}"\n\t\tandroid:translateY="{fmt(-y)}">\n'
        f"{body}\t</group>\n</vector>\n"
    )


BACKGROUND = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:aapt="http://schemas.android.com/aapt"
	android:width="108dp"
	android:height="108dp"
	android:viewportWidth="108"
	android:viewportHeight="108">
	<path android:pathData="M0,0h108v108h-108z">
		<aapt:attr name="android:fillColor">
			<gradient
				android:type="linear"
				android:startX="0"
				android:startY="0"
				android:endX="108"
				android:endY="108"
				android:startColor="{argb(DAWN[0])}"
				android:endColor="{argb(DAWN[1])}" />
		</aapt:attr>
	</path>
</vector>
"""


def write_drawables(mark):
    text, dot = mark.text_path(), mark.dot_path()
    white = argb((255, 255, 255))
    files = {
        "ic_launcher_background.xml": BACKGROUND,
        "ic_launcher_foreground.xml": vector(
            [(argb(INK), text), (argb(CREAM), dot)],
            comment="Generated by tools/store_assets.py",
        ),
        "ic_launcher_monochrome.xml": vector(
            [(white, text), (white, dot)],
            comment="Generated by tools/store_assets.py",
        ),
    }
    x0, y0, x1, y1 = mark.bounds()
    side = max(x1 - x0, y1 - y0) + 4
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    files["ic_notification.xml"] = vector(
        [(white, text), (white, dot)],
        size="24dp",
        viewport=(cx - side / 2, cy - side / 2, side, side),
        comment="Generated by tools/store_assets.py",
    )
    for name, xml in files.items():
        with open(f"{DRAWABLE}/{name}", "w") as f:
            f.write(xml)


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def icon_image(font, mark, px):
    lo, hi = VISIBLE
    unit = px / (hi - lo)
    img = Image.new("RGB", (px, px))
    draw = ImageDraw.Draw(img)
    # Same diagonal gradient as the adaptive background, seen through the visible window.
    for d in range(2 * px - 1):
        t = (2 * lo + d / unit) / 216
        draw.line([(d, 0), (0, d)], fill=lerp(DAWN[0], DAWN[1], t))
    to_px = lambda v: (v - lo) * unit
    text_font = pil_font(font, round(TEXT_SIZE * mark.k * unit))
    draw.text(
        (to_px(CENTER + mark.k * (TEXT_X - CENTER)), to_px(CENTER + mark.k * (BASELINE - CENTER))),
        TEXT, font=text_font, fill=INK, anchor="ms",
    )
    cx, cy, r = mark.dot()
    draw.ellipse([to_px(cx - r), to_px(cy - r), to_px(cx + r), to_px(cy + r)], fill=CREAM)
    return img


def feature_graphic(font, mark, path):
    img = Image.new("RGB", (1024, 500), APP_BG)
    tile = icon_image(font, mark, 300)
    mask = Image.new("L", (300, 300), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, 299, 299], radius=72, fill=255)
    img.paste(tile, (72, 100), mask)

    draw = ImageDraw.Draw(img)
    size = 120
    title = pil_font(font, size)
    while draw.textlength("WORKOUT", font=title) > 1024 - 432 - 56:
        size -= 2
        title = pil_font(font, size)
    draw.text((432, 262), "WORKOUT", font=title, fill=APP_INK, anchor="ls")
    subtitle = pil_font(instance(GEIST, {"wght": 500}), 42)
    draw.text((436, 330), "offline gym tracker", font=subtitle, fill=APP_DIM, anchor="ls")
    for x in range(436, 900):
        draw.line([(x, 356), (x, 359)], fill=lerp(DAWN[0], DAWN[1], (x - 436) / 464))
    img.save(path)


if __name__ == "__main__":
    font = instance(ARCHIVO, AXES)
    mark = Mark(font)
    write_drawables(mark)
    icon_image(font, mark, 512).save("docs/store/play-icon-512.png")
    feature_graphic(font, mark, "docs/store/feature-graphic-1024x500.png")
    print(f"mark scaled to {mark.k:.3f}; wrote launcher layers, notification icon and store graphics")
