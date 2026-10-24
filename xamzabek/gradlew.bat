@rem Gradle wrapper script for Windows
@rem

@if "%DEBUG%" == "" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

set APP_BASE_NAME=%~n0

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1

set CLASSPATH=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

@rem Execute Gradle
%JAVA_EXE% "-classpath" "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End of local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd
exit /b %ERRORLEVEL%

:mainEnd
if "%OS%"=="Windows_NT" endlocal
