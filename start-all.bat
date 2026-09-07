@echo off
title Shanghai Rescue Training System - Launcher

cd /d "%~dp0"

rem --- Local development environment variables (production uses a secret store) ---
set "DB_URL=jdbc:mysql://localhost:3306/rescue_training?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
set "DB_USERNAME=root"
set "DB_PASSWORD=root123456"
set "JWT_SECRET=shrescue-jwt-secret-key-2026-shanghai-rescue-base-training-system-0123456789"
set "INITIAL_USER_PASSWORD=admin123"
set "CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174"

echo ==========================================
echo   Shanghai Rescue Training System
echo ==========================================
echo.

echo [1/3] Starting Backend (port 8080)...
start "Backend-8080" cmd /k "cd /d %~dp0 && java -jar backend\rescue-admin\target\rescue-admin-1.0.0.jar"

echo [2/3] Starting PC Admin (port 5174)...
start "PC-Admin-5174" cmd /k "cd /d %~dp0pc-admin && npm run dev -- --port 5174"

echo [3/3] Starting Android H5 (port 5173)...
start "Android-H5-5173" cmd /k "cd /d %~dp0rescue-app && npm run dev:h5"

echo.
echo ==========================================
echo   Waiting 25 seconds for services to start...
echo ==========================================
ping -n 26 127.0.0.1 >nul

echo Opening PC Admin and Android H5 in browser...
start http://localhost:5174
start http://localhost:5173

echo.
echo ==========================================
echo   All services started. Pages opened.
echo ==========================================
echo.
echo   PC Admin : http://localhost:5174
echo   Android  : http://localhost:5173
echo   Backend  : http://localhost:8080
echo.
echo   Initial account password is supplied through INITIAL_USER_PASSWORD.
echo   Close each window to stop its service.
echo.
pause
