# Verification report (2026-10-20)

## Could NOT be run here
| Check | Result | Reason |
|---|---|---|
| `gradle :app:assembleDebug` | NOT RUN | no Gradle, Kotlin compiler, Android SDK or `android.jar`; `curl` to services.gradle.org and dl.google.com: "Could not resolve host" |
| `gradle :app:testDebugUnitTest` | NOT RUN | same |
| Run on device/emulator, animation frame rate, sound playback, launcher icon look | NOT RUN | no device |

## Actually executed
| Check | Result |
|---|---|
| All 10 Android XML files parse | PASS |
| Every `R.*` and `@resource` reference resolves to an existing file/entry | PASS (none unresolved) |
| Kotlin bracket balance and block-comment balance (21 files) | PASS |
| Heuristic missing-import scan | 11 hits, all inspected: false positives |
| Opening WAV: 3.0 s, 44.1 kHz mono, peak about -6 dBFS, silent first/last sample | PASS |
| Logo cut-outs viewed on the app background colour | PASS after one fix (wordmark haze removed) |
| Numeric expectations of StageMetricsTest/OpeningTimelineTest re-computed in Python | PASS (this checks my expected numbers, not the Kotlin) |

## Unit tests written (not yet executed)
OpeningTimelineTest (7), StageMetricsTest (6), GreetingTest (5), OrbPaletteTest (5) = 23 tests.
