# OpenOtago - COSC345 Project

![Tests](https://github.com/GenericPath/345/workflows/Tests/badge.svg) ![Build](https://github.com/GenericPath/345/workflows/Build/badge.svg) [![Documentation](https://github.com/GenericPath/345/workflows/Documentation/badge.svg)](https://zyviax.github.io/345Documentation/app/index.html) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/69862f4fa1f84105979181bf83eb4340)](https://www.codacy.com/manual/garth.dhnz/345?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=GenericPath/345&amp;utm_campaign=Badge_Grade) [![codecov](https://codecov.io/gh/GenericPath/345/branch/master/graph/badge.svg)](https://codecov.io/gh/GenericPath/345)


---
### OpenOtago is an android replacement to Univeristy of Otago\'s online resources

Created by
-   Damian Soo (6551336)
-   Garth Wales (4861462)
-   Louis Whitburn (2548261)

---
## **User documentation**

### Latest version: v1.0
### App store status: <a href="https://gitlab.com/fdroid/rfp/-/issues/1511">SUBMITTED</a>


<table>
<tr>
    <td>See <a href="https://github.com/GenericPath/345/blob/master/fastlane/metadata/android/en-US/full_description.txt">full description</a></td>
</tr>

<tr>
    <td>See <a href="https://github.com/GenericPath/345/tree/master/fastlane/metadata/android/en-US/images">screenshots</a></td>
</tr>
</table>

### If you wish to submit a bug or have an issue please drop a message <a href="https://github.com/GenericPath/345/issues">here</a>

---
App stores pull description, images and changelogs from directory <a href="https://github.com/GenericPath/345/tree/master/fastlane/metadata/android/en-US">fastlane/metadata/android/en-US</a>

If commiting changes to user documentation see <a href=https://gitlab.com/snippets/1895688>explanation</a>

---
## **Commiting Code**

Each commit runs the pipelines found in ```.github/workflows/```

```test.yml```

-   Runs <a href="https://github.com/GenericPath/345/blob/master/app/src/test/java/com/otago/open/UnitTest.kt">unit tests</a>
-   Runs <a href="https://github.com/GenericPath/345/blob/master/app/src/androidTest/java/com/otago/open/InstrumentedTest.kt">instrumentation tests</a>
-   Generates <a href="https://codecov.io/gh/GenericPath/345">code coverage</a>

```build.yml```

-   Tests build success

```publishDocs.yml```

-   Deploys <a href="https://zyviax.github.io/345Documentation/app/index.html">documentation</a>

<a href=https://app.codacy.com/manual/garth.dhnz/345/dashboard>Codacy</a> completes static analysis on each commit to recognise potential code pitfalls.

All known issues are tracked with <a href="https://github.com/GenericPath/345/issues">github issues</a>

---
### Requirements to build
-   Java Environment
-   Android SDK version 29

---
#### To build application

-   Clone the repository

-   *Option 1* - Android Studio:
    -   Open with Android Studio, wait for initial gradle setup
    -   Build (Ctrl+F9) / Emulate (Shift+F10)

-   *Option 2* - *NIX:
    -   ```./gradlew assembleDebug```
    -   install ```/app/build/outputs/apk/debug/app-debug.apk``` onto device

#### To run instrumentation tests (*on your machine*)

-   ```./gradlew connectedCheck``` (Run instrumentation tests)

This will need a suitable device to run on (e.g. an emulator).
This is easiest via Android Studio.

### For other tests (*on your machine*)
-   ```./gradlew testDebug``` (Run unit tests)
-   ```./gradlew lintDebug``` (Basic static analysis)

---
## For more
<table>
<tr>
    <td>See <a href="https://github.com/GenericPath/345/blob/master/proposal/proposal.pdf">initial proposal</a></td>
</tr>

<tr>
    <td>See <a href="https://zyviax.github.io/345Documentation/app/index.html">latest documentation</a></td>
</tr>

<tr>
    <td>See <a href="https://github.com/GenericPath/345/issues">github issues</a></td>
</tr>

<tr>
    <td>See <a href="https://github.com/GenericPath/345/blob/master/app/src/test/java/com/otago/open/UnitTest.kt">unit test code</a> & <a       href="https://github.com/GenericPath/345/blob/master/app/src/androidTest/java/com/otago/open/InstrumentedTest.kt">instrumented test code</a></td>
</tr>
</table>
