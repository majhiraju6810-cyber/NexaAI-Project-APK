# Feature status (written 2026-10-20)

"Written" means source exists. **Nothing below has been compiled or run yet** (see TEST_REPORT.md).

| Area | Status | Notes |
|---|---|---|
| Android app ID `com.nexa.ai`, app name NEXA AI | Written | manifest + gradle |
| Official logo imported and cut into variants | Written, assets visually checked | full logo, N+ring symbol, N only, wordmark, tagline, header, mono icon layer, 512 store icon |
| Launcher icon (adaptive + themed/monochrome) | Written, assets visually checked | 5 densities; not seen on a real launcher |
| System splash (SplashScreen API) | Written | near-black, empty icon, hands over to the reveal |
| 3 s Hologram Reveal (Compose Canvas) | Written | follows the 6-step timeline; tap to skip |
| Hand-over into dashboard | Written | same orb drawn by the same function; glides to its slot |
| Reduced motion / Lite mode | Written | Settings: follow system / reduced / full; low-RAM auto Lite |
| Opening sound (optional) | Written; WAV file generated and checked | silent in silent/vibrate mode, off switch in Settings |
| Dashboard (greeting, status, orb) | Partially implemented | greeting in Bengali/English by local time; status honestly says AI not connected |
| Settings (name, language, intro, sound, motion, orb preview, about) | Written | every control changes something real |
| Orb states Idle/Listening/Thinking/Speaking/Executing/Error/Emergency | Partially implemented | drawn and previewable; NOT connected to any real voice/AI/emergency logic; audio level is simulated in preview only |
| Fake loading percentage | Deliberately absent | startup has no real work to report |
| Onboarding, language engine, voice, wake word, emotion, SOS, phone control, WhatsApp, alarms, memory vault, automation, AI IDE, app generator, web research, provider system | Planned | not started; listed under "Coming next" in the app |

## Not done / limits
- No 3D engine: the orb is layered 2D Canvas (gradients, additive glow, rotating rings). Chosen for reliability and zero extra dependencies.
- Wordmark is the supplied raster artwork, not live text (no matching font was available).
- Bengali UI strings are mine: please have a native speaker review them.
- Animation timing/performance on real devices is unmeasured.
