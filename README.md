# OpenOtago - COSC345 Project
OpenOtago is an application to provide a mobile replacement to The Univeristy of Otago online resources.

Created by
 - Burnie Lorimer (2367465)
 - Damian Soo (6551336)
 - Garth Wales (4861462) 
 - Louis Whitburn (2548261)


----
## Alpha release
 Features downloading and viewing lecture slides for Computer Science papers.

Requirements to build:
 - Java Environment
 - Android SDK version ???

To build android application:
- Clone the repository
- Option 1 - Android Studio:
    - Open with Android Studio, wait for initial gradle setup
    - Build (Ctrl+F9) / Emulate (Shift+F10)
- Option 2 - Bash:
    - ./gradlew assembleDebug
    - install /app/build/outputs/apk/debug/app-debug.apk onto device

To build documentation:
 - ./gradlew dokka
 - open /app/build/dokka/app/index.html
