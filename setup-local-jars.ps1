#requires -Version 5
<#
.SYNOPSIS
  Installs CirclePack's bundled local jars (in ./jars) into the local Maven
  repository (~/.m2) so that `mvn compile` / `mvn package` can resolve the
  groupId=local dependencies declared in pom.xml.

.DESCRIPTION
  The pom.xml declares 8 dependencies with groupId "local" that are NOT on
  Maven Central -- they ship as jars in this repo's ./jars folder. A fresh
  checkout cannot build until these are registered in ~/.m2. Run this script
  once per machine (or after wiping ~/.m2).

  Requires Maven (mvn) on PATH and a JDK (JAVA_HOME set).

.EXAMPLE
  ./setup-local-jars.ps1
  ./setup-local-jars.ps1 -MvnPath "C:\Users\me\tools\apache-maven-3.9.9\bin\mvn.cmd"
#>
param([string]$MvnPath = 'mvn')

$ErrorActionPreference = 'Stop'
$jarsDir = Join-Path $PSScriptRoot 'jars'

# artifactId, version, jar filename  (groupId is always 'local')
$deps = @(
    @{ id = 'commons-codec';     ver = '1.5';   file = 'commons-codec-1.5.jar' },
    @{ id = 'Complex';           ver = '1.0';   file = 'Complex.jar' },
    @{ id = 'Convert';           ver = '2.0';   file = 'Convert2.0.jar' },
    @{ id = 'DJEP';              ver = '2.4.0'; file = 'DJEP2.4.0Minimal.jar' },
    @{ id = 'FunctionChoiceBox'; ver = '1.0';   file = 'FunctionChoiceBox.jar' },
    @{ id = 'FunctionField';     ver = '1.0';   file = 'FunctionField.jar' },
    @{ id = 'FunctionParser';    ver = '1.0';   file = 'FunctionParser.jar' },
    @{ id = 'xercesImpl';        ver = '1.0';   file = 'xercesImpl.jar' }
)

$fail = 0
foreach ($d in $deps) {
    $path = Join-Path $jarsDir $d.file
    if (-not (Test-Path $path)) { Write-Warning "MISSING jar: $path"; $fail++; continue }
    Write-Host "Installing local:$($d.id):$($d.ver)  <-  jars/$($d.file)"
    & $MvnPath -q install:install-file "-DgroupId=local" "-DartifactId=$($d.id)" "-Dversion=$($d.ver)" "-Dpackaging=jar" "-Dfile=$path"
    if ($LASTEXITCODE -ne 0) { Write-Warning "FAILED: $($d.id)"; $fail++ }
}

if ($fail -gt 0) { Write-Error "$fail dependency(ies) failed to install."; exit 1 }
Write-Host "`nAll 8 local jars installed. You can now run:  mvn clean compile" -ForegroundColor Green
