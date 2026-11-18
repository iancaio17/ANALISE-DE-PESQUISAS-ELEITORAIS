#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
SAPE_JAVAC=javac
SAPE_JAR=jar
if [[ -n "${JAVA_HOME:-}" ]]; then SAPE_JAVAC="$JAVA_HOME/bin/javac"; SAPE_JAR="$JAVA_HOME/bin/jar"; fi
mkdir -p build/classes bin
find build/classes -type f -delete
find src -type f -name '*.java' > build/fontes.txt
"$SAPE_JAVAC" -encoding UTF-8 --release 17 --module-path lib/linux --add-modules javafx.controls -d build/classes @build/fontes.txt
cp -R resources/. build/classes/
"$SAPE_JAR" --create --file bin/sape.jar --main-class apresentacao.Launcher -C build/classes .
echo 'Projeto compilado. Execute bash iniciar.sh.'
