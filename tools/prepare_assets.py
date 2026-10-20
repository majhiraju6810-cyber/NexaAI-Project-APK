#!/usr/bin/env python3
"""
Regenerates every production logo asset from the ORIGINAL logo file
(branding/reference/nexa_logo_original.png). The original is never modified.

Needs: pip install pillow numpy scipy   (run on a PC/Mac/Linux; the generated
files are already committed in app/src/main/res so this is optional).

Technique: the logo sits on a near-black background, so the transparent
versions use "alpha = brightest channel, colour = rgb / alpha". Composited over
black this reproduces the original pixels exactly, and over the app's
#02030A background it looks identical -- glow and gradients are preserved.
"""
import os, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "branding", "reference", "nexa_logo_original.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")
EXPORTS = os.path.join(ROOT, "branding", "exports")
NODPI = os.path.join(RES, "drawable-nodpi")
for d in (NODPI, EXPORTS):
    os.makedirs(d, exist_ok=True)

logo = Image.open(SRC).convert("RGB")
A = np.asarray(logo).astype(np.float32) / 255.0


def to_rgba(rgb_float, floor=0.02):
    """Black-background -> transparent, glow preserved.
    `floor` removes faint background haze (higher = cleaner edges, less outer glow)."""
    v = rgb_float.max(axis=2)
    alpha = np.clip((v - floor) / (1 - floor), 0, 1)
    col = rgb_float / np.maximum(v, 1e-4)[..., None]
    out = np.dstack([np.clip(col, 0, 1), alpha])
    return Image.fromarray((out * 255 + 0.5).astype(np.uint8), "RGBA")


def feather(img, px):
    """Fade alpha to 0 over `px` pixels at every edge so no crop rectangle can show."""
    a = np.asarray(img).copy().astype(np.float32)
    h, w = a.shape[:2]
    ramp_y = np.minimum(np.arange(h), np.arange(h)[::-1]) / float(px)
    ramp_x = np.minimum(np.arange(w), np.arange(w)[::-1]) / float(px)
    f = np.clip(np.minimum(ramp_y[:, None], ramp_x[None, :]), 0, 1)
    a[..., 3] *= f
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def crop_rgba(box, floor=0.02, feather_px=0):
    x0, y0, x1, y1 = box
    img = to_rgba(A[y0:y1, x0:x1], floor)
    return feather(img, feather_px) if feather_px else img


def pad_square(img, fill=(0, 0, 0, 0)):
    s = max(img.size)
    out = Image.new("RGBA", (s, s), fill)
    out.paste(img, ((s - img.width) // 2, (s - img.height) // 2), img)
    return out


# ---- 1. N symbol only (hand-traced outline of the 3D "N", ring excluded) ----
P = [(82,205),(88,185),(100,160),(120,135),(160,100),(200,70),(225,62),(248,62),(270,72),
     (440,240),(640,440),(668,468),(668,205),(700,178),(825,75),(842,70),(862,78),(872,100),
     (878,128),(878,690),(872,712),(850,745),(815,785),(785,799),(750,800),(690,785),
     (297,380),(297,640),(82,830)]
S, OX, OY = 0.5, 381.5, 181.5          # traced on a 2x zoom crop -> logo coordinates
poly = [(x * S + OX, y * S + OY) for x, y in P]
SS = 4
m = Image.new("L", (logo.width * SS, logo.height * SS), 0)
ImageDraw.Draw(m).polygon([(x * SS, y * SS) for x, y in poly], fill=255, outline=255, width=3)
m = m.resize(logo.size, Image.LANCZOS).filter(ImageFilter.GaussianBlur(0.6))
n_rgba = logo.convert("RGBA")
n_rgba.putalpha(m)
xs = [p[0] for p in poly]; ys = [p[1] for p in poly]
MARGIN = 14
n_box = (int(min(xs)) - MARGIN, int(min(ys)) - MARGIN, int(max(xs)) + MARGIN, int(max(ys)) + MARGIN)
n_sym = n_rgba.crop(n_box)
n_sym.save(os.path.join(NODPI, "nexa_n_symbol.png"), optimize=True)

# ---- 2. N + orbital ring symbol (square, transparent) ----
sym = pad_square(crop_rgba((255, 150, 1010, 690)))
# soft fade at the cut top/bottom so the thin outer circle does not end in a hard edge
a = np.asarray(sym).copy()
h = a.shape[0]
y0 = (sym.height - 540) // 2
fade = np.ones(sym.height, dtype=np.float32)
for i in range(46):
    f = i / 46.0
    fade[y0 + i] = f; fade[y0 + 539 - i] = f
fade[:y0] = 0; fade[y0 + 540:] = 0
a[..., 3] = (a[..., 3].astype(np.float32) * fade[:, None]).astype(np.uint8)
sym = Image.fromarray(a, "RGBA")
sym.save(os.path.join(NODPI, "nexa_logo_symbol.png"), optimize=True)

# ---- 3. Wordmark + tagline (bounding boxes found from the image itself) ----
def bbox_of_bright(y_lo, y_hi, thr):
    v = A[y_lo:y_hi].max(axis=2)
    ys_, xs_ = np.where(v > thr)
    return xs_.min(), ys_.min() + y_lo, xs_.max(), ys_.max() + y_lo

wx0, wy0, wx1, wy1 = bbox_of_bright(700, 880, 0.60)   # letters only, ignores flare lines
word = crop_rgba((wx0 - 22, wy0 - 6, wx1 + 22, wy1 + 16), floor=0.14, feather_px=10)
word.save(os.path.join(NODPI, "nexa_wordmark.png"), optimize=True)
tx0, ty0, tx1, ty1 = bbox_of_bright(888, 940, 0.40)
tag = crop_rgba((tx0 - 12, ty0 - 10, tx1 + 12, ty1 + 10), floor=0.12, feather_px=8)
tag.save(os.path.join(NODPI, "nexa_tagline.png"), optimize=True)

# ---- 4. Full logo (symbol + wordmark + tagline) ----
full = crop_rgba((120, 90, 1140, 945), floor=0.08, feather_px=24)
full.save(os.path.join(NODPI, "nexa_logo.png"), optimize=True)

# ---- 5. Compact header logo (wordmark only, small) ----
hw = 420
header = word.resize((hw, round(word.height * hw / word.width)), Image.LANCZOS)
header.save(os.path.join(NODPI, "nexa_header_logo.png"), optimize=True)

# ---- 6. Launcher icon layers (adaptive: 108dp canvas, 66dp safe circle) ----
DENS = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
mono_src = Image.new("RGBA", n_sym.size, (255, 255, 255, 0))
mono_src.putalpha(n_sym.getchannel("A"))
white = Image.new("RGBA", n_sym.size, (255, 255, 255, 255))
white.putalpha(n_sym.getchannel("A"))
for name, px in DENS.items():
    d = os.path.join(RES, f"mipmap-{name}")
    os.makedirs(d, exist_ok=True)
    fg = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    s = round(px * 0.62)
    sm = sym.resize((s, s), Image.LANCZOS)
    fg.paste(sm, ((px - s) // 2, (px - s) // 2), sm)
    fg.save(os.path.join(d, "ic_launcher_foreground.png"), optimize=True)
    mono = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    mw = round(px * 0.50)
    mh = round(white.height * mw / white.width)
    mm = white.resize((mw, mh), Image.LANCZOS)
    mono.paste(mm, ((px - mw) // 2, (px - mh) // 2), mm)
    mono.save(os.path.join(d, "ic_launcher_monochrome.png"), optimize=True)

# ---- 7. Store / avatar icon (512 px, dark background) ----
bg = Image.new("RGBA", (512, 512), (2, 3, 10, 255))
s = round(512 * 0.80)
sm = sym.resize((s, s), Image.LANCZOS)
bg.paste(sm, ((512 - s) // 2, (512 - s) // 2), sm)
bg.convert("RGB").save(os.path.join(EXPORTS, "nexa_icon_512.png"), optimize=True)

print("assets written")
for f in sorted(os.listdir(NODPI)):
    im = Image.open(os.path.join(NODPI, f)); print(" ", f, im.size, os.path.getsize(os.path.join(NODPI, f)) // 1024, "KB")
