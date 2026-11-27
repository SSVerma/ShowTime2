#!/usr/bin/env bash
# ==============================================================================
# ShowTime Play Store Adaptive (Desktop / Googlebook & Tablet) Capture Utility
#
# Guides you through capturing Desktop (>= 1200dp) and Tablet (~840dp) raw
# screenshots from a running emulator or connected device via ADB.
# Usage:
#   ./scripts/capture-adaptive-screenshots.sh [desktop|tablet|both]
# ==============================================================================

set -euo pipefail

MODE="${1:-desktop}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DESKTOP_DIR="$REPO_ROOT/play-store-listing/raw-desktop-screenshots"
TABLET_DIR="$REPO_ROOT/play-store-listing/raw-tablet-screenshots"

case "$MODE" in
    desktop|tablet|both)
        ;;
    *)
        echo "Usage: $0 [desktop|tablet|both]"
        exit 1
        ;;
esac

echo "========================================================"
echo "ShowTime: Adaptive Store Screenshot Capture Utility"
echo "Mode: $MODE"
echo "========================================================"

if ! command -v adb &> /dev/null; then
    echo "Error: 'adb' command not found in your PATH."
    echo "Please ensure Android SDK platform-tools are in your PATH."
    exit 1
fi

DEVICE_SERIAL="${ANDROID_SERIAL:-}"
if [ -n "$DEVICE_SERIAL" ]; then
    ADB_CMD=(adb -s "$DEVICE_SERIAL")
else
    CONNECTED_DEVICES=()
    while IFS= read -r serial; do
        if [ -n "$serial" ]; then
            CONNECTED_DEVICES+=("$serial")
        fi
    done < <(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')

    if [ "${#CONNECTED_DEVICES[@]}" -eq 0 ]; then
        echo "Error: No Android device or emulator detected via ADB."
        echo "Start your Desktop API 37.1 or Tablet emulator first, then re-run."
        exit 1
    elif [ "${#CONNECTED_DEVICES[@]}" -gt 1 ]; then
        echo "Multiple devices detected. Set ANDROID_SERIAL=<serial> or disconnect extra devices:"
        printf '  - %s\n' "${CONNECTED_DEVICES[@]}"
        exit 1
    fi
    DEVICE_SERIAL="${CONNECTED_DEVICES[0]}"
    ADB_CMD=(adb -s "$DEVICE_SERIAL")
fi

echo "Connected Device: $DEVICE_SERIAL"
echo ""

capture_slide() {
    local output_dir="$1"
    local index="$2"
    local total="$3"
    local filename="$4"
    local screen_title="$5"
    local instructions="$6"

    echo "--------------------------------------------------------"
    echo "[$index/$total] $screen_title"
    echo " -> $instructions"
    read -r -p "Press [ENTER] to capture (or 's' to skip): " user_input

    if [[ "$user_input" =~ ^[sS]$ ]]; then
        echo "Skipped: $filename"
        echo ""
        return
    fi

    local target_path="$output_dir/$filename"
    "${ADB_CMD[@]}" exec-out screencap -p > "$target_path"

    if [ ! -s "$target_path" ]; then
        echo "Warning: Captured file was empty. Retrying..."
        "${ADB_CMD[@]}" exec-out screencap -p > "$target_path"
    fi

    if [ -s "$target_path" ]; then
        echo "Saved: $target_path"
    else
        echo "Error: Failed to capture $target_path"
    fi
    echo ""
}

capture_desktop_set() {
    mkdir -p "$DESKTOP_DIR"
    echo "=== Capturing Large Desktop / Googlebook Screenshots (>= 1200dp) ==="
    echo "Output Directory: $DESKTOP_DIR"
    echo ""

    capture_slide "$DESKTOP_DIR" "1" "5" "01_desktop_home.png" \
        "Desktop Home Dashboard" \
        "Open ShowTime in Large Desktop (>= 1200dp) showing the 248dp sidebar, Ctrl+K top bar, widescreen hero carousel, and Cinephile Hub."

    capture_slide "$DESKTOP_DIR" "2" "5" "02_desktop_search_split.png" \
        "Two-Pane Search Inspector (Dark Theme)" \
        "Switch to Dark theme, open Search, search a title, and tap a Movie/TV result so both list and detail panes appear side by side."

    capture_slide "$DESKTOP_DIR" "3" "5" "03_desktop_discover.png" \
        "Discover & Browse / Community Grid" \
        "Open Discover & Browse (or Community Collections) showing multi-column responsive grids and vibe filters."

    capture_slide "$DESKTOP_DIR" "4" "5" "04_desktop_shortcuts_or_details.png" \
        "Desktop Shortcuts or Context OmniMenu" \
        "Open Movie/TV Details and trigger either the right-click OmniMenu or press '?' for the Keyboard Shortcuts dialog."

    capture_slide "$DESKTOP_DIR" "5" "5" "05_desktop_diary_dark.png" \
        "Cinema Diary or Taste Profile (Dark / OLED)" \
        "Open Cinema Diary or Taste Profile in Dark/OLED theme."
}

capture_tablet_set() {
    mkdir -p "$TABLET_DIR"
    echo "=== Capturing Medium Tablet Screenshots (~840dp) ==="
    echo "Output Directory: $TABLET_DIR"
    echo ""

    capture_slide "$TABLET_DIR" "1" "4" "01_tablet_home.png" \
        "Tablet Home Dashboard (Navigation Rail)" \
        "Resize/switch to Medium width (~840dp) showing ShowTime's Home Dashboard and polished Navigation Rail."

    capture_slide "$TABLET_DIR" "2" "4" "02_tablet_discover.png" \
        "Tablet Discover & Browse" \
        "Open Discover & Browse in Medium width showing the multi-column poster grid with Navigation Rail."

    capture_slide "$TABLET_DIR" "3" "4" "03_tablet_community.png" \
        "Library -> Community Adaptive Grid" \
        "Open Library -> Community tab showing multi-column curated collection cards."

    capture_slide "$TABLET_DIR" "4" "4" "04_tablet_details.png" \
        "Tablet Movie or TV Details" \
        "Tap any movie or TV series to show the adaptive details layout on Tablet."
}

if [ "$MODE" = "desktop" ] || [ "$MODE" = "both" ]; then
    capture_desktop_set
fi

if [ "$MODE" = "tablet" ] || [ "$MODE" = "both" ]; then
    capture_tablet_set
fi

echo "========================================================"
echo "Capture complete!"
echo "Next step — render final aesthetic Play Store frames with GleeeStudio:"
echo "  ./scripts/render-gleeestudio-screenshots.sh all"
echo "========================================================"
