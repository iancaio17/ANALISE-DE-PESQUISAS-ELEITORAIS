#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
if [[ "$(uname -s)" != "Linux" || "$(uname -m)" != "x86_64" ]]; then
  echo 'Neste sistema, abra pom.xml no IntelliJ ou execute: mvn javafx:run'
  exit 1
fi
SAPE_JAVA=java
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then SAPE_JAVA="$JAVA_HOME/bin/java"; fi
exec "$SAPE_JAVA" --module-path lib/linux --add-modules javafx.controls -jar bin/sape.jar
