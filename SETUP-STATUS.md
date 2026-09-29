# TouchV setup status

The repository now contains the editable Android project at its root. The original source and artwork ZIP archives are retained alongside it.

Local validation in the setup session:

- ZIP integrity checks passed for all three archives.
- The source tree contains the Android manifest, Gradle build files and wrapper, Java app sources, resources, README, and engine self-tests.
- Build and self-tests were not run: the setup runtime has Java 17 but no `javac`, Android SDK, or configured Android build environment.

Remaining validation: run `./selftest/run.sh` and `./gradlew :app:assembleDebug` in an environment with JDK 17 and Android SDK Platform/Build Tools 34.0.0. The app currently uses placeholder visuals; integrating the plates and Play Again art into the relevant screen layouts is a future UI improvement.
