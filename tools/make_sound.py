#!/usr/bin/env python3
"""Synthesises the optional 3.0 s NEXA opening sound (no external samples used).
Timeline mirrors the visual one: energy swell 0-0.9 s, whoosh ~0.65 s, chime on the
N reveal at 0.95 s, soft pad 1.4-2.0 s, closing tone 2.0-2.6 s, natural fade to 3.0 s.
Needs numpy + scipy. Output: app/src/main/res/raw/nexa_hologram.wav (44.1 kHz mono 16-bit)."""
import os, wave
import numpy as np
from scipy.signal import butter, lfilter

SR = 44100
N = int(3.0 * SR)
t = np.arange(N) / SR
rng = np.random.default_rng(7)

def smooth(x): x = np.clip(x, 0, 1); return x * x * (3 - 2 * x)
def seg(a, b): return smooth((t - a) / (b - a))
def bell(c, w): return np.exp(-0.5 * ((t - c) / w) ** 2)

# 1. low energy swell (sweep 70 -> 210 Hz) with a detuned shimmer
f = 70 + 140 * seg(0.0, 0.95)
phase = 2 * np.pi * np.cumsum(f) / SR
swell = (np.sin(phase) + 0.5 * np.sin(phase * 1.005 + 0.7)) * (0.20 * seg(0.0, 0.9) * (1 - seg(0.95, 1.5)))

# 2. holographic whoosh = band-passed noise, brighter as it peaks
noise = rng.standard_normal(N)
b1, a1 = butter(2, [250 / (SR / 2), 1400 / (SR / 2)], btype="band")
b2, a2 = butter(2, [1800 / (SR / 2), 6000 / (SR / 2)], btype="band")
lo, hi = lfilter(b1, a1, noise), lfilter(b2, a2, noise)
mix = seg(0.25, 0.8)
whoosh = (lo * (1 - mix) + hi * mix * 0.6) * bell(0.62, 0.20) * 0.45

# 3. gentle digital chime on the N reveal (0.95 s)
def chime(t0, freqs, amps, decay):
    tt = np.clip(t - t0, 0, None)
    env = (1 - np.exp(-tt * 900)) * np.exp(-tt * decay) * (t >= t0)
    return sum(a * np.sin(2 * np.pi * fr * tt) for fr, a in zip(freqs, amps)) * env
ch = chime(0.95, [1318.5, 1975.5, 2637.0], [0.20, 0.10, 0.05], 4.5)

# 4. soft pad under the wordmark reveal (1.4 - 2.0 s)
pad_f = 440 + 220 * seg(1.4, 2.0)
pad_ph = 2 * np.pi * np.cumsum(pad_f) / SR
pad = (np.sin(pad_ph) + 0.6 * np.sin(pad_ph * 1.5)) * 0.07 * bell(1.75, 0.28)

# 5. restrained closing tone (2.0 - 2.6 s) that fades naturally
end = (np.sin(2 * np.pi * 880 * t) + 0.5 * np.sin(2 * np.pi * 1320 * t)) * 0.10 * seg(2.0, 2.25) * (1 - seg(2.35, 3.0))

x = swell + whoosh + ch + pad + end
x *= np.clip(t / 0.01, 0, 1)                 # 10 ms fade-in (no click)
x *= 1 - seg(2.55, 3.0)                      # natural fade-out
x = x / np.max(np.abs(x)) * 0.5              # peak about -6 dBFS
pcm = (x * 32767).astype("<i2")

out = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "app", "src", "main", "res", "raw", "nexa_hologram.wav")
os.makedirs(os.path.dirname(out), exist_ok=True)
with wave.open(out, "wb") as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
print("wrote", out, os.path.getsize(out) // 1024, "KB", len(pcm) / SR, "s")
