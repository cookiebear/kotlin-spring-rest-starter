@echo off
setlocal
set "ROOT=%~dp0"
set "VERSION=8.14.3"
set "DIST=%ROOT%.gradle-dist"
if not exist "%DIST%\gradle-%VERSION%\bin\gradle.bat" (
  if not exist "%DIST%" mkdir "%DIST%"
  powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%DIST%\gradle.zip'; Expand-Archive '%DIST%\gradle.zip' -DestinationPath '%DIST%' -Force; Remove-Item '%DIST%\gradle.zip'"
  if errorlevel 1 exit /b 1
)
call "%DIST%\gradle-%VERSION%\bin\gradle.bat" -p "%ROOT%" %*
