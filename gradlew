#!/usr/bin/env bash
set -euo pipefail
VERSION=8.14.3
DIR="$(cd "$(dirname "$0")" && pwd)"
DIST="$DIR/.gradle-dist"
if [ ! -x "$DIST/gradle-$VERSION/bin/gradle" ]; then
  command -v curl >/dev/null || { echo 'curl is required'; exit 1; }
  command -v unzip >/dev/null || { echo 'unzip is required'; exit 1; }
  mkdir -p "$DIST"
  echo "Downloading Gradle $VERSION on first run..."
  curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$DIST/gradle.zip"
  unzip -q "$DIST/gradle.zip" -d "$DIST"
  rm "$DIST/gradle.zip"
fi
exec "$DIST/gradle-$VERSION/bin/gradle" -p "$DIR" "$@"
