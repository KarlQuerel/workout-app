"""Regenerate the Play Store graphics from the in-app icon and font.

Usage: python3 tools/store_assets.py
"""

from PIL import Image, ImageDraw, ImageFont

BG = (0, 0, 0)
ACCENT = (74, 222, 128)
INK_HI = (232, 237, 243)
DIM = (139, 152, 167)
FONT = "app/src/main/res/font/vt323.ttf"

# The ic_launcher_foreground barbell, as (x, y, w, h) in its 108-unit viewport.
BARBELL = [
	(26, 49, 56, 10),
	(28, 33, 10, 42),
	(70, 33, 10, 42),
	(20, 43, 7, 22),
	(81, 43, 7, 22),
]
MARK_SPAN = 68  # design width, from x=20 to x=88


def draw_barbell(draw, cx, cy, width, color=ACCENT):
	scale = width / MARK_SPAN
	for x, y, w, h in BARBELL:
		x0 = cx + (x - 54) * scale
		y0 = cy + (y - 54) * scale
		draw.rectangle([x0, y0, x0 + w * scale, y0 + h * scale], fill=color)


def icon(path):
	img = Image.new("RGB", (512, 512), BG)
	draw_barbell(ImageDraw.Draw(img), 256, 256, 340)
	img.save(path)


def feature_graphic(path):
	img = Image.new("RGB", (1024, 500), BG)
	draw = ImageDraw.Draw(img)
	draw_barbell(draw, 196, 250, 232)
	title = ImageFont.truetype(FONT, 190)
	subtitle = ImageFont.truetype(FONT, 72)
	draw.text((376, 196), "WORKOUT", font=title, fill=INK_HI, anchor="lm")
	draw.text((380, 328), "offline gym tracker", font=subtitle, fill=DIM, anchor="lm")
	draw.line([(380, 372), (900, 372)], fill=ACCENT, width=3)
	img.save(path)


if __name__ == "__main__":
	icon("docs/store/play-icon-512.png")
	feature_graphic("docs/store/feature-graphic-1024x500.png")
	print("wrote docs/store/play-icon-512.png and docs/store/feature-graphic-1024x500.png")
