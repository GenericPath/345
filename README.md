# OpenOtago - COSC345 Project

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af979dcfdf3e42068c6a61d58e11bb36)](https://app.codacy.com/manual/garth.dhnz/345?utm_source=github.com&utm_medium=referral&utm_content=GenericPath/345&utm_campaign=Badge_Grade_Settings)

![Test](https://github.com/GenericPath/345/workflows/Test/badge.svg)![Build & Publish](https://github.com/GenericPath/345/workflows/Build%20&%20Publish/badge.svg)

**OpenOtago is an application to provide a mobile replacement to Univeristy of Otago online resources.**

<table>
<tr>
    <td>see <a href="https://github.com/GenericPath/345/blob/master/proposal/proposal.pdf">proposal</a></td> 
</tr>

<tr>
    <td>see (to be updated)<a href="https://altitude.otago.ac.nz/lwhitburn/c345-project/-/jobs/artifacts/master/file/public/app/index.html?job=pages">latest documentation</a></td>
</tr>
</table>

Created by
 - Damian Soo (6551336)
 - Garth Wales (4861462) 
 - Louis Whitburn (2548261)

---
### **Commiting Code** 
All submitted code runs on the CI pipelines defined in ```.github/workflows/```

 ```test.yml```
 - Tests code quality (lintDebug)
 - Run unit tests (testDebug)
 
 ```buildPublish.yml```
 - Tests debug build success (assembleDebug)    
 - Deploy documentation pages (dokka)

----
## *Alpha release* 
Features fetching, storing and viewing lecture slides for Computer Science papers.

**Requirements to build:**
 - Java Environment
 - Android SDK version 29 

**To build application locally:**
- Clone the repository
- *Option 1* - Android Studio:
    - Open with Android Studio, wait for initial gradle setup
    - Build (Ctrl+F9) / Emulate (Shift+F10)
- *Option 2* - *NIX:
    - ```./gradlew assembleDebug ```
    - install ```/app/build/outputs/apk/debug/app-debug.apk``` onto device

**To build documentation locally:**
 - ```./gradlew dokka```
 - open ```/app/build/dokka/app/index.html```

 **To run tests locally:**
 - ```./gradlew lintDebug``` (Test code formatting)
 - ```./gradlew testDebug``` (Run unit tests)
