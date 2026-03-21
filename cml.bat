@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ===== 文字化け対策（batは UTF-8 with BOM 推奨）=====
chcp 65001 > nul

rem ===== このbat自身の場所を基準にする =====
set "BASE_DIR=%~dp0WEB-INF"
cd /d "%BASE_DIR%"

rem ===== ディレクトリ設定 =====
set "SRC_DIR=%BASE_DIR%\src"
set "LIB_DIR=%BASE_DIR%\lib"
set "OUT_DIR=%BASE_DIR%\classes"

rem ===== 存在チェック =====
if not exist "%SRC_DIR%" (
    echo [ERROR] src folder not found: "%SRC_DIR%"
    exit /b 1
)

rem ===== javac 存在確認 =====
where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] javac not found. Please install JDK and set PATH correctly.
    exit /b 1
)

if not exist "%OUT_DIR%" (
    mkdir "%OUT_DIR%"
)

rem ===== クラスパス作成 =====
set "CLASSPATH=%OUT_DIR%"

if exist "%LIB_DIR%" (
    for /r "%LIB_DIR%" %%f in (*.jar) do (
        set "CLASSPATH=!CLASSPATH!;%%~ff"
    )
)

rem ===== コンパイル対象一覧ファイル作成 =====
set "SOURCE_LIST=%TEMP%\javac_sources_%RANDOM%_%RANDOM%.txt"
if exist "%SOURCE_LIST%" del /q "%SOURCE_LIST%"

set "SOURCE_COUNT=0"

rem ===== .java を再帰的に収集 =====
rem ===== javac の @argfile 対策として \ を / に変換して書き込む =====
for /r "%SRC_DIR%" %%f in (*.java) do (
    set "JAVA_FILE=%%~ff"
    set "JAVA_FILE=!JAVA_FILE:\=/!"
    >>"%SOURCE_LIST%" echo "!JAVA_FILE!"
    set /a SOURCE_COUNT+=1
)

rem ===== .java が1件もない場合 =====
if "%SOURCE_COUNT%"=="0" (
    echo [ERROR] No .java files found under "%SRC_DIR%"
    if exist "%SOURCE_LIST%" del /q "%SOURCE_LIST%"
    exit /b 1
)

echo.
echo === compile start ===
echo SRC_DIR   : %SRC_DIR%
echo LIB_DIR   : %LIB_DIR%
echo OUT_DIR   : %OUT_DIR%
echo SOURCES   : %SOURCE_COUNT%
echo TARGET    : Java 17
echo.

rem ===== コンパイル前にクラス削除 =====
if exist "%OUT_DIR%" (
    echo [INFO] Deleting old .class files...
    for /r "%OUT_DIR%" %%f in (*.class) do (
        del /q "%%~ff"
    )

    for /f "delims=" %%d in ('dir "%OUT_DIR%" /ad /b /s ^| sort /r') do (
        rd "%%d" 2>nul
    )
)

rem ===== コンパイル実行 =====
javac --release 17 -encoding UTF-8 -cp "%CLASSPATH%" -d "%OUT_DIR%" @"%SOURCE_LIST%"
set "JAVAC_RESULT=%ERRORLEVEL%"

if exist "%SOURCE_LIST%" del /q "%SOURCE_LIST%"

if not "%JAVAC_RESULT%"=="0" (
    echo.
    echo [ERROR] Compile failed.
    exit /b %JAVAC_RESULT%
)

echo.
echo [OK] Compile completed.
echo Output: "%OUT_DIR%"
exit /b 0