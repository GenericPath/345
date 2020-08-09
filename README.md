# OpenOtago - COSC345 Project
![Tests](https://github.com/GenericPath/345/workflows/Tests/badge.svg) ![Build](https://github.com/GenericPath/345/workflows/Build/badge.svg) [![Documentation](https://github.com/GenericPath/345/workflows/Documentation/badge.svg)](https://zyviax.github.io/345Documentation/app/index.html) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/69862f4fa1f84105979181bf83eb4340)](https://www.codacy.com/manual/garth.dhnz/345?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=GenericPath/345&amp;utm_campaign=Badge_Grade) [![codecov](https://codecov.io/gh/GenericPath/345/branch/master/graph/badge.svg)](https://codecov.io/gh/GenericPath/345)

**OpenOtago is an android application to provide a mobile replacement to Univeristy of Otago online resources.**

<table>
<tr>
    <td>see <a href="https://github.com/GenericPath/345/blob/master/proposal/proposal.pdf">proposal</a></td> 
</tr>

<tr>
    <td>see <a href="https://zyviax.github.io/345Documentation/app/index.html">latest documentation</a></td>
</tr>
</table>

Created by
-   Damian Soo (6551336)
-   Garth Wales (4861462)
-   Louis Whitburn (2548261)

---
## **Commiting Code** 
All submitted code runs on the CI pipelines defined in ```.github/workflows/```

```test.yml```

-   Tests code quality (lintDebug)
-   Runs unit tests (testDebug)
-   Runs instrumented tests (connectedCheck)

```build.yml```

-   Tests debug build success (assembleDebug)

```publishDocs.yml```

-   Deploy documentation pages (dokka)

---
## *Beta release*

The alpha release notes still apply, with the following additions:

**To run instrumentation tests**

-   ```./gradlew connectedCheck``` (Run instrumentation tests)

This will need a suitable device to run on (e.g. an emulator).
This is more easily done via Android Studio.

**To do for final release**
-   Redo UI (users will select the courses they want to follow and then these will be listed and refeshed as appropriate)
-   Improve error handling
-   Implement MATH/STAT support

---
## *Alpha release* 
Features fetching, storing and viewing lecture slides for Computer Science papers.

**Requirements to build:**

-   Java Environment
-   Android SDK version 29 

**To build application locally:**

-   Clone the repository

-   *Option 1* - Android Studio:
    -   Open with Android Studio, wait for initial gradle setup
    -   Build (Ctrl+F9) / Emulate (Shift+F10)

-   *Option 2* - *NIX:
    -   ```./gradlew assembleDebug```
    -   install ```/app/build/outputs/apk/debug/app-debug.apk``` onto device

**To build documentation locally:**

-   ```./gradlew dokka```
-   open ```/app/build/dokka/app/index.html```

**To run tests locally:**

-   ```./gradlew lintDebug``` (Test code formatting)
-   ```./gradlew testDebug``` (Run unit tests)
