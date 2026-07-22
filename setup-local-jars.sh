#!/usr/bin/env bash
#
# Installs CirclePack's bundled local jars (in ./jars) into the local Maven
# repository (~/.m2) so that `mvn compile` / `mvn package` can resolve the
# groupId=local dependencies declared in pom.xml.
#
# The pom.xml declares 8 dependencies with groupId "local" that are NOT on
# Maven Central -- they ship as jars in this repo's ./jars folder. A fresh
# checkout cannot build until these are registered in ~/.m2. Run once per
# machine (or after wiping ~/.m2). Requires mvn on PATH and a JDK.
#
# Usage:  ./setup-local-jars.sh          (uses `mvn` from PATH)
#         MVN=/path/to/mvn ./setup-local-jars.sh
#
set -euo pipefail
cd "$(dirname "$0")"
MVN="${MVN:-mvn}"

# artifactId  version  jarfile   (groupId is always 'local')
install_one() {
    local id="$1" ver="$2" file="jars/$3"
    if [[ ! -f "$file" ]]; then echo "MISSING jar: $file" >&2; return 1; fi
    echo "Installing local:$id:$ver  <-  $file"
    "$MVN" -q install:install-file -DgroupId=local -DartifactId="$id" -Dversion="$ver" -Dpackaging=jar -Dfile="$file"
}

install_one commons-codec     1.5   commons-codec-1.5.jar
install_one Complex           1.0   Complex.jar
install_one Convert           2.0   Convert2.0.jar
install_one DJEP              2.4.0 DJEP2.4.0Minimal.jar
install_one FunctionChoiceBox 1.0   FunctionChoiceBox.jar
install_one FunctionField     1.0   FunctionField.jar
install_one FunctionParser    1.0   FunctionParser.jar
install_one xercesImpl        1.0   xercesImpl.jar

echo
echo "All 8 local jars installed. You can now run:  mvn clean compile"
