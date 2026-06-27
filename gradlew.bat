@if "%DEBUG%" == "" @echo off
@rem Gradle wrapper bootstrap for Windows

set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if defined JAVA_HOME goto findJava
  set JAVA_EXE=java
  goto run
:findJava
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
:run

"%JAVA_EXE%" -cp "%CLASSPATH%" GradleBootstrap %*
