#!/usr/bin/env bash
# Builds GymManagerGUI.jar -- a self-contained runnable JAR for the GUI demo.
# Run this once before zipping for the professor.

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "==> Cleaning bin/"
rm -rf bin
mkdir -p bin

echo "==> Compiling all sources (engine + GUI)"
javac -d bin src/main/java/*.java src/main/java/gui/*.java

echo "==> Writing JAR manifest"
cat > manifest.txt <<'EOF'
Manifest-Version: 1.0
Main-Class: GymManagerGUI

EOF

echo "==> Building GymManagerGUI.jar"
jar cfm GymManagerGUI.jar manifest.txt -C bin .
rm manifest.txt

echo "==> Done. To launch:"
echo "    java -jar GymManagerGUI.jar"
