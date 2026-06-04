#!/bin/sh
set -e

ADOCFMT_VERSION="${ADOCFMT_VERSION:-0.2.0}"
ADOCFMT_CACHE_DIR="${XDG_CACHE_HOME:-$HOME/.cache}/adocfmt"
ADOCFMT_CACHE_JAR="$ADOCFMT_CACHE_DIR/adocfmt-${ADOCFMT_VERSION}.jar"
ADOCFMT_DOWNLOAD_URL="https://github.com/dheid/adocfmt/releases/download/v${ADOCFMT_VERSION}/adocfmt.jar"

if [ -z "$ADOCFMT_JAR" ]; then
  if [ ! -f "$ADOCFMT_CACHE_JAR" ]; then
    mkdir -p "$ADOCFMT_CACHE_DIR"
    echo "Downloading adocfmt ${ADOCFMT_VERSION}..."
    if command -v curl > /dev/null 2>&1; then
      curl -fsSL -o "$ADOCFMT_CACHE_JAR" "$ADOCFMT_DOWNLOAD_URL"
    elif command -v wget > /dev/null 2>&1; then
      wget -q -O "$ADOCFMT_CACHE_JAR" "$ADOCFMT_DOWNLOAD_URL"
    else
      echo "Error: neither curl nor wget found. Install one to download adocfmt." >&2
      exit 2
    fi
  fi
  ADOCFMT_JAR="$ADOCFMT_CACHE_JAR"
fi

if ! command -v java > /dev/null 2>&1; then
  echo "Error: java not found on PATH. Install Java 17+ to use adocfmt." >&2
  exit 2
fi

exec java -jar "$ADOCFMT_JAR" "$@"
