@echo off
echo ================================
echo     CNMoney Plugin Build Script
echo ================================
echo.

echo [INFO] Starting CNMoney plugin compilation...
echo.

REM Clean previous build
echo [Step 1/3] Cleaning previous build...
call mvn clean
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)
echo.

REM Compile and package
echo [Step 2/3] Compiling and packaging...
call mvn package
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo.

REM Show results
echo [Step 3/3] Build completed!
echo.
echo ================================
echo     Build Success!
echo ================================
echo.
echo Output file location: target\CNMoney-*.jar
echo.

REM List generated jar files
if exist "target\*.jar" (
    echo Generated files:
    dir /b target\*.jar
    echo.
)

echo Press any key to exit...
pause > nul
