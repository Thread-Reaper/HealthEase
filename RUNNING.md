Quick Run (without Maven)
=========================

Prerequisites
- JDK 17 installed and on PATH (java -version shows 17)
- JavaFX SDK 17 downloaded. Set env var JAVA_FX to its lib folder, e.g.:
  - PowerShell:  setx JAVA_FX "C:\\javafx-sdk-17\\lib"
  - CMD:         setx JAVA_FX C:\\javafx-sdk-17\\lib
  Reopen terminal after setting.

Scripts
- run-app.bat
  - Compiles sources into out/ using your JAVA_FX path, then starts the GUI app
  - Usage: double-click or run from cmd

- run-relay.bat [port]
  - Runs the lightweight TCP relay server for Internet chat
  - Default port 5050
  - Example: run-relay.bat 5050

Notes
- You can keep a libs directory for third-party jars (e.g., sqlite-jdbc). The scripts include lib\* on classpath automatically if it exists.
- If the GUI fails to start with a JavaFX error, verify JAVA_FX points to the SDK lib folder that contains javafx-*.jar files.
- In the Messenger screen, fill Relay host and port to connect, then specify a Peer username to send messages via the relay.

