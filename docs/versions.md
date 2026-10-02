# Versions

Every pinned version and the date it was last verified as the current stable release on its
official source. Pre-releases (alpha, beta, release candidate) are never adopted. Renovate
(`renovate.json`) opens the bumps; update this table in the same commit as each bump.

## Android

Source of truth: `android/gradle/libs.versions.toml` and `android/gradle/wrapper/gradle-wrapper.properties`.

| Dependency | Version | Verified | Source |
| --- | --- | --- | --- |
| Gradle | 9.8.0 | 2026-10-01 | services.gradle.org/versions/current |
| Android Gradle Plugin | 9.4.1 | 2026-10-01 | Google Maven |
| Kotlin (built-in via AGP, Compose compiler plugin) | 2.4.20 | 2026-10-01 | Maven Central |
| Compose BOM | 2026.09.00 | 2026-10-01 | Google Maven |
| androidx.activity:activity-compose | 1.13.0 | 2026-10-01 | Google Maven |
| androidx.core:core-ktx | 1.19.1 | 2026-10-01 | Google Maven |
| androidx.lifecycle:lifecycle-runtime-compose | 2.11.0 | 2026-10-01 | Google Maven |
| org.jetbrains.kotlinx:kotlinx-serialization-json | 1.11.0 | 2026-10-01 | Maven Central |
| Kotlin serialization plugin | 2.4.20 | 2026-10-01 | Maven Central |
| androidx.window:window, window-core | 1.5.1 | 2026-10-01 | Google Maven |
| androidx.navigation3:navigation3-runtime, navigation3-ui | 1.2.0 | 2026-10-02 | Google Maven |
| androidx.compose.material:material-icons-core (via BOM) | 1.7.8 | 2026-10-02 | Compose BOM |
| androidx.compose.material3:material3-adaptive-navigation-suite (via BOM) | 1.4.0 | 2026-10-02 | Compose BOM |
| junit:junit | 4.13.2 | 2026-10-01 | Maven Central |
| androidx.test.ext:junit | 1.3.0 | 2026-10-02 | Google Maven |
| androidx.test:runner | 1.7.0 | 2026-10-02 | Google Maven |
| androidx.compose.ui:ui-test-junit4, ui-test-manifest (via BOM) | BOM 2026.09.00 | 2026-10-02 | Compose BOM |
| compileSdk / targetSdk | 37 | 2026-10-01 | Android SDK platforms |
| minSdk | 34 | 2026-10-01 | `docs/decisions.md` |

Resolved at kickoff and added to the catalog by the chapter that first needs them:

| Dependency | Version | Verified |
| --- | --- | --- |
| androidx.compose.material3.adaptive:adaptive | 1.3.0 | 2026-10-01 |
| androidx.core:core-splashscreen | 1.2.0 | 2026-10-01 |
| org.jetbrains.kotlinx:kotlinx-coroutines-core | 1.11.0 | 2026-10-01 |

## iOS

Source of truth: `ios/project.yml`. No Swift packages yet; any added are pinned to an exact
version in `project.yml`.

| Tool | Version | Verified |
| --- | --- | --- |
| Xcode | 27.0 (27A266a) | 2026-10-01 |
| Swift | 6.3.3 | 2026-10-01 |
| XcodeGen | 2.46.0 | 2026-10-01 |
| iOS deployment target | 18.0 | 2026-10-01 |

### Test runtimes

Simulator runtimes are test environments, not dependencies; the no-pre-release rule applies to
what ships in the app, not to what it is tested on.

| Runtime | Build | Status | Chosen |
| --- | --- | --- | --- |
| iOS 27.2 simulator | 24B5089g | Beta | 2026-10-01, primary simulator target |
| iOS 27.0 simulator | 24A434 | Stable | 2026-10-01, release baseline |
| iOS 18.6 simulator | 22G86 | Stable | 2026-10-01, deployment-target floor |
