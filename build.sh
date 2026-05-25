#!/usr/bin/env bash
set -euo pipefail

# Clean
rm -rf bin
mkdir -p bin

# Compile
javac -d bin src/main/java/*.java

# Package
( cd bin && jar cfe ../gym.jar Main *.class )

echo "Built gym.jar successfully."
