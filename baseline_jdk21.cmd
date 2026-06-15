@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
"C:\Users\night\.maven\maven-3.9.15\bin\mvn.cmd" -version
"C:\Users\night\.maven\maven-3.9.15\bin\mvn.cmd" clean compile test-compile
if errorlevel 1 exit /b %errorlevel%
"C:\Users\night\.maven\maven-3.9.15\bin\mvn.cmd" clean test
