#!/usr/bin/env bash
# ==============================================================================
# ShowTime GleeeStudio Headless Skia Screenshot Exporter
#
# Uses the sibling GleeeStudio project's Skia rendering engine read-only to
# generate framed Play Store screenshots (Desktop, Tablet, Phone).
# Usage:
#   ./scripts/render-gleeestudio-screenshots.sh [desktop|tablet|phone|all]
# ==============================================================================

set -euo pipefail

TARGET="${1:-all}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GLEEESTUDIO_DIR="${GLEEESTUDIO_DIR:-$REPO_ROOT/../GleeeStudio}"
MANIFEST_PATH="${MANIFEST_PATH:-$REPO_ROOT/scripts/gleeestudio/showtime-slides.json}"

case "$TARGET" in
    desktop|tablet|phone|all)
        ;;
    *)
        echo "Usage: $0 [desktop|tablet|phone|all]"
        exit 1
        ;;
esac

if [ ! -d "$GLEEESTUDIO_DIR" ]; then
    echo "Error: GleeeStudio project not found at: $GLEEESTUDIO_DIR"
    echo "Set GLEEESTUDIO_DIR=/path/to/GleeeStudio and re-run."
    exit 1
fi

pick_java21_home() {
    local candidate
    for candidate in \
        "${JAVA21_HOME:-}" \
        "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
        "/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home" \
        "$(/usr/libexec/java_home -v 21 2>/dev/null || true)" \
        "${JAVA_HOME:-}"; do
        if [ -n "$candidate" ] && [ -x "$candidate/bin/javac" ] && [ -x "$candidate/bin/java" ]; then
            if "$candidate/bin/java" -version 2>&1 | grep -qE 'version "(2[1-9]|[3-9][0-9])\.'; then
                echo "$candidate"
                return 0
            fi
        fi
    done
    return 1
}

JDK21_HOME=$(pick_java21_home || true)
if [ -n "$JDK21_HOME" ]; then
    JAVAC_BIN="$JDK21_HOME/bin/javac"
    JAVA_BIN="$JDK21_HOME/bin/java"
else
    JAVAC_BIN="javac"
    JAVA_BIN="java"
fi

echo "========================================================"
echo "ShowTime x GleeeStudio Headless Skia Batch Renderer"
echo "Target: $TARGET"
echo "GleeeStudio: $GLEEESTUDIO_DIR"
echo "Java: $("$JAVA_BIN" -version 2>&1 | head -n 1)"
echo "========================================================"

echo "Resolving GleeeStudio Skia/Compose JVM classpath..."
CP_LINE=$(
    "$GLEEESTUDIO_DIR/gradlew" -p "$GLEEESTUDIO_DIR" \
        -I "$REPO_ROOT/scripts/gleeestudio/print-classpath.init.gradle.kts" \
        :composeApp:printJvmExportClasspath -q \
        | grep '^CLASSPATH_START=' \
        | tail -n 1
)
GLEEE_CP="${CP_LINE#CLASSPATH_START=}"

if [ -z "$GLEEE_CP" ]; then
    echo "Error: Failed to resolve GleeeStudio JVM classpath."
    exit 1
fi

BUILD_TMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/showtime_gleee_renderer.XXXXXX")"
trap 'rm -rf "$BUILD_TMP_DIR"' EXIT

echo "Compiling GleeeBatchRenderer.java..."
"$JAVAC_BIN" -cp "$GLEEE_CP" -d "$BUILD_TMP_DIR" "$REPO_ROOT/scripts/gleeestudio/GleeeBatchRenderer.java"

EXTRA_ARGS=()
if [ -n "${OVERRIDE_INPUT_DIR:-}" ]; then
    EXTRA_ARGS+=("--override-input-dir=$OVERRIDE_INPUT_DIR")
fi
if [ -n "${OVERRIDE_OUTPUT_DIR:-}" ]; then
    EXTRA_ARGS+=("--override-output-dir=$OVERRIDE_OUTPUT_DIR")
fi

"$JAVA_BIN" -Djava.awt.headless=true \
    -cp "$BUILD_TMP_DIR:$GLEEE_CP" \
    GleeeBatchRenderer \
    --manifest="$MANIFEST_PATH" \
    --project-root="$REPO_ROOT" \
    --gleeestudio-dir="$GLEEESTUDIO_DIR" \
    --target="$TARGET" \
    ${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"}
