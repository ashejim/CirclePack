# runCP.ps1 - launch CirclePack from a terminal (PowerShell)
#
#   .\runCP.ps1                 run CirclePack
#   .\runCP.ps1 myscript.cps    run, auto-loading a script
#   .\runCP.ps1 -b              recompile src -> bin first, then run
#
# Runs from the compiled classes in 'bin' plus the libraries in 'jars',
# so it reflects your current source build (see also the Ant build for
# producing the distributable jar).

$ErrorActionPreference = 'Stop'
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$cp  = "$dir\bin;$dir\jars\*"

$rest = @($args)
if ($rest.Count -gt 0 -and ($rest[0] -eq '-b' -or $rest[0] -eq 'build')) {
    if ($rest.Count -gt 1) { $rest = $rest[1..($rest.Count - 1)] } else { $rest = @() }
    Write-Host "Compiling src -> bin ..."
    $srcs = Get-ChildItem -Recurse "$dir\src" -Filter *.java | ForEach-Object FullName
    & javac -encoding UTF-8 -cp $cp -d "$dir\bin" @srcs
    if ($LASTEXITCODE -ne 0) { Write-Host "compile failed"; exit 1 }
}

& java -cp $cp allMains.SplashMain @rest
