# Godly Torch

[![License](https://img.shields.io/badge/License-GNU%20GPL%20v3-orange.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)]()

Root-only torch app with per-LED brightness knobs for devices with dual-tone (white + yellow) flashlight LEDs. Works on single-LED devices too.

> ⚠️ **Requires root.** Writes directly to `/sys/class/leds/...` to bypass the camera HAL torch limits.

## What's new in this fork

This fork modernises the original [BRoy98/GodlyTorch](https://github.com/BRoy98/GodlyTorch) and adds:

- **Auto device detection** — first launch matches your `Build.DEVICE` codename against the catalog (with alias fallback) and auto-selects the right LED profile. No manual picking on supported devices.
- **More supported devices** — expanded catalog with extra codename aliases for OnePlus 9R (`LE2100`, `OnePlus9R`), OnePlus 8T variants, Mi 6 (`wayne`), and others.
- **Four built-in themes** — Cardinal (red), Obsidian (dark), Ember (warm brown), Polar (cool light). Switch live in Settings — no app restart, no flash, no close-on-change.
- **Theme-matched cold-start splash** — each theme has its own splash icon (two-bolt design from the official logo) and background color, persisted via `SplashScreen.setSplashScreenTheme` so the next cold launch always matches your last picked theme.
- **Edge-to-edge UI** — full Android 15 support, predictive back gesture, target SDK 35.
- **Zero telemetry** — removed Firebase, Bitrise, all analytics. App has no network code at all.
- **Modernised internals** — Kotlin coroutines instead of legacy `Handler` callbacks, AGP 8, JDK 17, lifecycle-aware delays, shared `LedController` / `TorchTileBase` / `KnobTheming` helpers.

## Features

- Independent white / yellow / master brightness knobs (dual-tone) or one master knob (single-LED)
- Quick Settings tiles: Master, White, Yellow — toggle, fixed-intensity, or N-step intensity cycle
- Four hand-picked themes (Cardinal, Obsidian, Ember, Polar) with seamless live theme switching and matching cold-start splash
- Lightweight: no analytics, no network, no Firebase

## Supported devices

Profiles for many OnePlus, Xiaomi, HTC, LeEco, Mi 6, etc. live in `app/src/main/java/com/teamdarkness/godlytorch/Utils/DeviceList.kt`. Unsupported devices can submit their `Build.DEVICE` ID via the in-app **Contact** flow.

## Build

```bash
./gradlew :app:assembleDebug
```

APK lands in `app/build/outputs/apk/debug/`. Requires **JDK 17** and **Android SDK 35**.

The repo ships with a `debug-key` for local debug signing only. Release builds are signed via CI keystore secrets (see `.github/workflows/build.yml`).

## Architecture

```
app/src/main/java/com/teamdarkness/godlytorch/
├── Activity/           MainActivity (single launcher, dynamic theme via SplashScreen.setSplashScreenTheme)
├── Fragment/           Launch / ThreeKnob / SingleKnob / Incompatible + KnobTheming helper
├── Service/            TorchTileBase + Master / White / Yellow tile subclasses (~60 lines each)
├── Settings/           SettingsActivity + PreferenceFragment + DeviceListAdapter
├── Utils/              Prefs, LedController (sysfs writes), AppTheme, Device, DeviceList, Utils
└── Dialog/             TileDialog
```

Key design notes:

- **`LedController`** owns all `echo N > /sys/class/leds/...` command building and dispatch (`com.topjohnwu.libsu`)
- **`TorchTileBase`** encodes the shared QS tile lifecycle / mutex / 6-step cycle. Per-tile classes only describe their identity and "on" command builder
- Dynamic theming via `SplashScreen.setSplashScreenTheme(themeRes)` (API 31+) — no launcher aliases, no double recents

## Credits

- [**Bishwajyoti Roy**](https://github.com/broy98/) — original author
- [**Rohan Khurana**](https://github.com/rk2810/) — co-author

## License

GNU GPL v3. See [LICENSE](LICENSE).
