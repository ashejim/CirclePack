@echo off
rem runCP.cmd - launch CirclePack from a terminal (Windows cmd / double-click)
rem
rem   runCP                  run CirclePack
rem   runCP myscript.cps     run, auto-loading a script
rem   runCP -b               recompile src -> bin first, then run
rem
rem Runs from the compiled classes in 'bin' plus the libraries in 'jars'.

setlocal
set "DIR=%~dp0"
set "CP=%DIR%bin;%DIR%jars\*"

if /I "%~1"=="-b"    goto build
if /I "%~1"=="build" goto build
goto run

:build
echo Compiling src -^> bin ...
dir /s /b "%DIR%src\*.java" > "%TEMP%\cp_srcs.txt"
javac -encoding UTF-8 -cp "%CP%" -d "%DIR%bin" @"%TEMP%\cp_srcs.txt"
if errorlevel 1 (echo compile failed& exit /b 1)
rem after a build, launch with no extra args
java -cp "%CP%" allMains.SplashMain
goto end

:run
java -cp "%CP%" allMains.SplashMain %*

:end
endlocal
