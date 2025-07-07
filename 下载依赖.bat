@echo off
echo ========================================
echo CNMoney Plugin Dependencies Download
echo ========================================
echo.

:: Create lib directory
if not exist "lib" (
    mkdir lib
    echo Creating lib directory...
)

echo Downloading VaultAPI 1.7.1...
powershell -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/MilkBowl/VaultAPI/releases/download/1.7.1/VaultAPI-1.7.1.jar' -OutFile 'lib/VaultAPI-1.7.1.jar' -UseBasicParsing; Write-Host 'VaultAPI download successful!' -ForegroundColor Green } catch { Write-Host 'VaultAPI download failed: ' $_.Exception.Message -ForegroundColor Red }"

echo.
echo Downloading PlaceholderAPI 2.11.6...
powershell -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/PlaceholderAPI/PlaceholderAPI/releases/download/2.11.6/PlaceholderAPI-2.11.6.jar' -OutFile 'lib/PlaceholderAPI-2.11.6.jar' -UseBasicParsing; Write-Host 'PlaceholderAPI download successful!' -ForegroundColor Green } catch { Write-Host 'PlaceholderAPI download failed: ' $_.Exception.Message -ForegroundColor Red }"

echo.
echo ========================================
echo Download completed! Checking files...
echo ========================================

if exist "lib\VaultAPI-1.7.1.jar" (
    echo [OK] VaultAPI-1.7.1.jar exists
) else (
    echo [MISSING] VaultAPI-1.7.1.jar not found
)

if exist "lib\PlaceholderAPI-2.11.6.jar" (
    echo [OK] PlaceholderAPI-2.11.6.jar exists
) else (
    echo [MISSING] PlaceholderAPI-2.11.6.jar not found
)

echo.
echo If download failed, please manually download these files to lib directory:
echo 1. VaultAPI: https://github.com/MilkBowl/VaultAPI/releases/download/1.7.1/VaultAPI-1.7.1.jar
echo 2. PlaceholderAPI: https://github.com/PlaceholderAPI/PlaceholderAPI/releases/download/2.11.6/PlaceholderAPI-2.11.6.jar
echo.
pause
