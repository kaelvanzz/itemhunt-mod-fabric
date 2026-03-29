#!/bin/bash
#
# Gradle Wrapper script for Linux and macOS
#
# Set default values
GRADLE_VERSION="8.9"
GRADLE_HOME="${GRADLE_HOME:-$HOME/.gradle/wrapper}"
# Check if we're running on Windows using WSL or Cygwin
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # On Windows, use .bat version
    exec "$0.bat" "$@"
fi
GRADLE_DIST="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIST_FILE="${GRADLE_HOME}/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIR="${GRADLE_HOME}/gradle-${GRADLE_VERSION}"
if [ ! -d "$GRADLE_DIR" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$GRADLE_HOME"
    curl -L -o "$GRADLE_DIST_FILE" "$GRADLE_DIST"
    unzip -q "$GRADLE_DIST_FILE" -d "$GRADLE_HOME"
    rm "$GRADLE_DIST_FILE"
fi
exec "$GRADLE_DIR/bin/gradle" "$@"