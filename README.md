# OpenOtago - COSC345 Project
[![pipeline status](https://altitude.otago.ac.nz/lwhitburn/c345-project/badges/master/pipeline.svg)](https://altitude.otago.ac.nz/lwhitburn/c345-project/-/commits/master)

**OpenOtago is an application to provide a mobile replacement to Univeristy of Otago online resources.**

<table>
<tr>
    <td>see <a href="https://altitude.otago.ac.nz/lwhitburn/c345-project/-/blob/master/proposal/proposal.pdf">proposal</a></td> 
</tr>

<tr>
    <td>see <a href="https://altitude.otago.ac.nz/lwhitburn/c345-project/-/jobs/artifacts/master/file/public/app/index.html?job=pages">latest documentation</a></td>
</tr>
</table>

Created by
 - Burnie Lorimer (2367465)
 - Damian Soo (6551336)
 - Garth Wales (4861462) 
 - Louis Whitburn (2548261)

---
### **Commiting Code** 
All submitted code runs on the CI pipeline defined in ```github-ci.yml```

The pipeline:
 - Tests build success (assembleDebug)    
 - Tests code quality (lintDebug)
 - Run unit tests (testDebug)
 - Deploy documentation pages

----
## *Alpha release* 
Features fetching, storing and viewing lecture slides for Computer Science papers.

**Requirements to build:**
 - Java Environment
 - Android SDK version 29 

**To build application:**
- Clone the repository
- *Option 1* - Android Studio:
    - Open with Android Studio, wait for initial gradle setup
    - Build (Ctrl+F9) / Emulate (Shift+F10)
- *Option 2* - UNIX:
    - ```./gradlew assembleDebug ```
    - install ```/app/build/outputs/apk/debug/app-debug.apk``` onto device

**To build documentation:**
 - ```./gradlew dokka```
 - open ```/app/build/dokka/app/index.html```

 **To run tests**
 - ```./gradlew lintDebug``` (Test code formatting)
 - ```./gradlew testDebug``` (Run unit tests)
