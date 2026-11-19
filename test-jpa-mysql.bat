@echo off
chcp 65001 >nul
cd /d "%~dp0.."
call mvnw.cmd compile exec:java "-Dexec.mainClass=vn.iotstar.config.JpaSmokeTest"
pause
