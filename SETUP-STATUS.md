# TouchV setup status

This repository now contains the editable Android project at the repository root, plus the supplied drawable plates under `app/src/main/res/drawable-nodpi/`. The original ZIP archives are retained.

Local validation in the setup session:

- ZIP integrity checks passed for all three archives.
- The source tree contains the Android manifest, Gradle build files/wrapper, Java app sources, resources, README, and engine self-tests.
- Build and self-tests were not run: the setup runtime has Java 17 but no `javac`, Android SDK, or configured Android build environment.

Remaining validation: run `./selftest/run.sh` and `./gradlew :app:assembleDebug` in an environment with JDK 17 and Android SDK Platform/Build Tools 34.0.0. UI work remains to connect score/sort/result plates and the Play Again plate to their screen layouts.
