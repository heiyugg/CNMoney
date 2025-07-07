@echo off
echo ========================================
echo CNMoney Plugin Release Script
echo ========================================
echo.

:: Check if git is available
git --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git is not installed or not in PATH
    echo Please install Git first: https://git-scm.com/
    pause
    exit /b 1
)

:: Get current version from pom.xml
echo [INFO] Reading current version from pom.xml...
for /f "tokens=2 delims=<>" %%i in ('findstr "<version>" pom.xml ^| findstr -v "maven" ^| findstr -v "paper" ^| findstr -v "vault" ^| findstr -v "placeholder"') do (
    set CURRENT_VERSION=%%i
    goto :version_found
)
:version_found

echo [INFO] Current version: %CURRENT_VERSION%
echo.

:: Ask for new version
set /p NEW_VERSION="Enter new version (current: %CURRENT_VERSION%): "
if "%NEW_VERSION%"=="" set NEW_VERSION=%CURRENT_VERSION%

echo [INFO] Using version: %NEW_VERSION%
echo.

:: Update version in pom.xml
echo [1/6] Updating version in pom.xml...
powershell -Command "(Get-Content pom.xml) -replace '<version>%CURRENT_VERSION%</version>', '<version>%NEW_VERSION%</version>' | Set-Content pom.xml"

:: Build the project
echo [2/6] Building project...
call mvn clean package -q
if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo [SUCCESS] Build completed successfully!
echo.

:: Git operations
echo [3/6] Adding files to git...
git add .

echo [4/6] Committing changes...
set /p COMMIT_MSG="Enter commit message (default: Release v%NEW_VERSION%): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Release v%NEW_VERSION%
git commit -m "%COMMIT_MSG%"

echo [5/6] Creating git tag...
git tag -a "v%NEW_VERSION%" -m "Release version %NEW_VERSION%"

echo [6/6] Pushing to GitHub...
git push origin main
git push origin "v%NEW_VERSION%"

echo.
echo ========================================
echo Release Process Completed!
echo ========================================
echo.
echo Version: v%NEW_VERSION%
echo.
echo Next steps:
echo 1. Go to your GitHub repository
echo 2. Create a new Release using tag: v%NEW_VERSION%
echo 3. Wait for JitPack to build: https://jitpack.io/#heiyugg/cnmoney
echo.
echo After JitPack build completes, others can use:
echo ^<dependency^>
echo     ^<groupId^>com.github.heiyugg^</groupId^>
echo     ^<artifactId^>cnmoney^</artifactId^>
echo     ^<version^>v%NEW_VERSION%^</version^>
echo     ^<scope^>provided^</scope^>
echo ^</dependency^>
echo.
pause
