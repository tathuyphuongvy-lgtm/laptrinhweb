@echo off
chcp 65001 >nul
cd /d "%~dp0.."
if not exist config\db.properties (
  echo Hay copy config\db.properties.example thanh config\db.properties va sua tai khoan MySQL truoc.
  pause
  exit /b 1
)
call mvnw.cmd test-compile exec:java "-Dexec.mainClass=vn.iotstar.dev.DevServer" "-Dexec.classpathScope=test" "-Dapp.db=mysql"
pause
