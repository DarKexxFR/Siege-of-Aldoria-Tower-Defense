#!/bin/bash
# Build script for Siege of Aldoria
set -e

JAVA_BIN="/c/Program Files/Java/jdk-26.0.1/bin"
JAVAC="$JAVA_BIN/javac"
JAR_TOOL="$JAVA_BIN/jar"
JPACKAGE="$JAVA_BIN/jpackage"

echo "🏰 Building Siege of Aldoria..."

# 1. Compile
mkdir -p out
find src -name "*.java" > sources.txt
"$JAVAC" -d out --source-path src/main/java @sources.txt
echo "  ✅ Compiled"

# 2. Copy resources
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/. out/
    echo "  ✅ Resources copied"
fi

# 3. Package JAR
"$JAR_TOOL" cfe SiegeOfAldoria.jar com.siegeofaldoria.Main -C out .
echo "  ✅ JAR → SiegeOfAldoria.jar"

# 4. Build .exe (optional — pass --exe flag)
if [[ "$1" == "--exe" ]]; then
    echo "  📦 Building Windows .exe..."
    rm -rf _pkg_input dist/
    mkdir _pkg_input
    cp SiegeOfAldoria.jar _pkg_input/

    "$JPACKAGE" \
      --type app-image \
      --input _pkg_input \
      --main-jar SiegeOfAldoria.jar \
      --main-class com.siegeofaldoria.Main \
      --name "Siege of Aldoria" \
      --app-version "1.0.0" \
      --dest dist \
      --java-options "-Xmx256m" \
      --java-options "-Dawt.useSystemAAFontSettings=on"

    rm -rf _pkg_input
    echo "  ✅ EXE → dist/Siege of Aldoria/Siege of Aldoria.exe"
    echo ""
    echo "  📁 Pour distribuer : zippe le dossier dist/Siege of Aldoria/"
fi

echo ""
echo "▶  Lancer : java -jar SiegeOfAldoria.jar"
[[ "$1" == "--exe" ]] && echo "▶  Ou      : dist/Siege of Aldoria/Siege of Aldoria.exe"
