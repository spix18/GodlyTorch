# Agent notes

Project-specific build/run/verify instructions for AI coding agents.

## Build

```bash
./gradlew :app:assembleDebug              # debug APK
./gradlew :app:assembleRelease            # release APK (unsigned by default; CI signs from secrets)
```

JDK 17, AGP 8+, Android SDK 35.

APK output: `app/build/outputs/apk/{debug,release}/app-*-new.apk` (the `applicationVariants` block in `app/build.gradle` renames outputs).

## Install + smoke test

```bash
adb install -r app/build/outputs/apk/debug/app-debug-new.apk
adb shell monkey -p com.teamdarkness.godlytorch -c android.intent.category.LAUNCHER 1
```

## Architecture invariants

- **One launcher activity.** `MainActivity` is the sole `LAUNCHER` activity. Theme-matched cold-start splash is achieved via `SplashScreen.setSplashScreenTheme(themeRes)` called from `MainActivity.onCreate` and `PreferenceFragment.onSharedPreferenceChanged`. Do **not** reintroduce launcher-aliases — caused the recents-double-instance + close-on-theme-change regressions.
- **All sysfs writes go through `LedController`.** Don't inline `echo N > /sys/class/leds/...` in fragments or services — pipe through `LedController.buildOff/buildOnDualReset/buildOnSingleReset` so format stays consistent.
- **Tile services share `TorchTileBase`.** White/Yellow are dual-tone-only; Master supports both. Subclasses only override `tileStatusKey`, mutex list, default label, and the `buildToggleOn` / `buildCycleOn` command builders.

## Preserved historical quirks (do not "fix" without explicit ask)

- Master tile single-tone toggle uses `(brightnessMax * pct) / brightnessMax` (likely intended `/ 100`). Preserved verbatim — see comment in `MasterTileService.kt`.
- Per-LED knob value clamps to **225** (not 255) — preserved from upstream.
- Cycle behaviour clamps `pct > 90 → 100` only at steps 3 and 6 (not all steps). Preserved from original case statements.
- White/Yellow toggle "on" command skips the toggle-reset prefix that the cycle "on" command uses. Preserved.

## Adding a device

Append to `DeviceList.kt`. Use `Device.aliases(...)` for codename variants. `Utils.checkSupport()` matches by `Build.DEVICE` substring or alias.

## CI

`.github/workflows/build.yml` builds the debug APK on PR-merge / `workflow_dispatch` and creates a draft GitHub Release with the APK attached. Release build is intentionally skipped — unsigned release APKs cannot be installed on Android. To ship a signed release, build locally with a real keystore.
