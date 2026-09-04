@echo off
title Rescue Training DB Backup

set MYSQLDUMP="D:\MySQL\MySQL Server 8.0\bin\mysqldump.exe"
set BACKUP_DIR=%~dp0backup
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set stamp=%%I

echo Backing up rescue_training ...
%MYSQLDUMP% -u root -proot123456 rescue_training > "%BACKUP_DIR%\rescue_training_%stamp%.sql"

echo.
echo Backup saved: backup\rescue_training_%stamp%.sql
echo.
pause
