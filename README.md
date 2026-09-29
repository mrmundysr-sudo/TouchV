# TouchV!

Android Studio project for the TouchV! card game. Open this repository root in Android Studio and run the `app` configuration.

## Project

- Java, single Activity, package `com.touchv.game`
- Android Gradle Plugin 8.5.2, Gradle wrapper 8.7, JDK 17
- compile/target SDK 34; minimum SDK 26
- Portrait orientation
- Engine self-tests: `./selftest/run.sh` (requires a JDK with `javac`)

Build a debug APK with `./gradlew :app:assembleDebug`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Android SDK Platform 34 and Build Tools 34.0.0 are required.

## Artwork

The complete final plate set and markup notes are in `touchv-deepseek-plates-pack.zip` at the repository root. The app still uses placeholder drawables; integrating the provided art into the UI is part of the next improvement pass.

## Source and specifications

- `TOUCH-V-DEEPSEEK-HANDOFF.md`: interaction and visual handoff
- `TOUCHV-OFFICIAL-RULES.md`: locked rules
- `00-DEEPSEEK-PROMPT.md`: build prompt
- `selftest/`: headless rules-engine test programs

The original source and handoff ZIPs are retained at the repository root as reference copies.
