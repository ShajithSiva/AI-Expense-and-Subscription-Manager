@echo off

echo ==========================================
echo   AI Financial Advisor Backend
echo ==========================================
echo.

cd /d "%~dp0"

echo Installing/checking dependencies...
call npm install

echo.
echo Starting Node.js backend...
echo.

call npm start

pause