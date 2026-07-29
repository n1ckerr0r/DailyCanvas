@ECHO OFF
SET DIR=%~dp0
SET CLASSPATH=%DIR%gradle\wrapper\gradle-wrapper-main.jar;%DIR%gradle\wrapper\gradle-wrapper-shared.jar

IF NOT "%JAVA_HOME%"=="" GOTO useJavaHome
SET JAVA_EXE=java.exe
GOTO run

:useJavaHome
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

:run
"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
