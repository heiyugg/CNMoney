@echo off
echo ========================================
echo CNMoney Plugin Local Build Script
echo ========================================
echo.

:: Check if lib directory exists
if not exist "lib" (
    echo [ERROR] lib directory not found!
    echo Please run "下载依赖.bat" first to download dependencies.
    echo.
    pause
    exit /b 1
)

:: Check if dependencies exist
set MISSING_DEPS=0

if not exist "lib\VaultAPI-1.7.1.jar" (
    echo [ERROR] VaultAPI-1.7.1.jar not found in lib directory
    set MISSING_DEPS=1
)

if not exist "lib\PlaceholderAPI-2.11.6.jar" (
    echo [ERROR] PlaceholderAPI-2.11.6.jar not found in lib directory
    set MISSING_DEPS=1
)

if %MISSING_DEPS%==1 (
    echo.
    echo [ERROR] Missing dependencies! Please run "下载依赖.bat" first.
    echo.
    pause
    exit /b 1
)

echo [OK] All dependencies found
echo.

:: Use local pom.xml for compilation
echo Using local dependencies configuration...
echo Compiling with pom-local.xml...
echo.

:: Clean and compile
echo [1/3] Cleaning previous build...
mvn -f pom-local.xml clean

echo.
echo [2/3] Compiling source code...
mvn -f pom-local.xml compile

echo.
echo [3/3] Packaging plugin...
mvn -f pom-local.xml package

echo.
echo ========================================
echo Build completed!
echo ========================================

if exist "target\CNMoney-1.0.0-SNAPSHOT.jar" (
    echo [SUCCESS] Plugin built successfully!
    echo Location: target\CNMoney-1.0.0-SNAPSHOT.jar
) else (
    echo [ERROR] Build failed! Check the output above for errors.
)

echo.
pause
