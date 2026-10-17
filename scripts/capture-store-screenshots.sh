#!/usr/bin/env bash
# ==============================================================================
# ShowTime Play Store Screenshot Capture Utility
#
# Guides you through capturing all 8 store screenshots via ADB or allows
# one-shot captures directly from your connected device.
# ==============================================================================

set -euo pipefail

OUTPUT_DIR="${1:-./play-store-listing/raw-screenshots}"
mkdir -p "$OUTPUT_DIR"

echo "========================================================"
echo "🎬 ShowTime: Play Store Screenshot Capture Utility"
echo "📁 Output Directory: $OUTPUT_DIR"
echo "========================================================"

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "❌ Error: 'adb' command not found in your PATH."
    echo "Please ensure Android SDK platform-tools are in your PATH."
    exit 1
fi

# Verify connected device
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "❌ Error: No Android device detected via ADB."
    echo "Please connect your phone, enable USB Debugging, and authorize this Mac."
    exit 1
fi

DEVICE_NAME=$(adb devices -l | grep -v "List" | grep "device " | awk '{print $1}')
echo "✅ Connected Device: $DEVICE_NAME"
echo ""

capture_slide() {
    local index="$1"
    local filename="$2"
    local screen_title="$3"
    local instructions="$4"

    echo "--------------------------------------------------------"
    echo "📸 [$index/8] $screen_title"
    echo "👉 $instructions"
    read -r -p "Press [ENTER] to capture (or 's' to skip): " user_input

    if [[ "$user_input" =~ ^[sS]$ ]]; then
        echo "⏭️  Skipped."
        return
    fi

    local target_path="$OUTPUT_DIR/$filename"
    adb exec-out screencap -p > "$target_path"
    
    if [ -s "$target_path" ]; then
        echo "✅ Saved: $target_path"
    else
        echo "⚠️ Warning: Captured file was empty. Retrying..."
        adb exec-out screencap -p > "$target_path"
    fi
    echo ""
}

# 1. Panoramic Duo Part 1: Home Discovery
capture_slide "1" "01_home_discovery.png" \
    "Home Dashboard & Hero Carousel (Panorama Part A)" \
    "Open ShowTime -> Home tab. Scroll to the top hero backdrop carousel."

# 2. Panoramic Duo Part 2: Movie Details
capture_slide "2" "02_movie_details.png" \
    "Movie Details & Cast (Panorama Part B)" \
    "Tap any featured movie (e.g. Dune / Oppenheimer) to open Movie Details."

# 3. Movie Match
capture_slide "3" "03_movie_match.png" \
    "Movie Match (Swipe Deck)" \
    "Open Movie Match from Cinephile Hub -> Start a swipe session."

# 4. Cinema Receipt
capture_slide "4" "04_cinema_receipt.png" \
    "Cinema Receipt" \
    "Go to Library / Profile -> Cinema Receipt -> View generated receipt."

# 5. Streaming Universe Hub
capture_slide "5" "05_streaming_hub.png" \
    "Streaming Universe Hub" \
    "Go to Watch Providers / Where to Stream tab (showing Netflix, Prime, Disney+)."

# 6. Cinema Diary & Taste Profile
capture_slide "6" "06_cinema_diary.png" \
    "Cinema Diary & Taste Profile" \
    "Go to Library -> Cinema Diary (or Taste Profile showing genre distribution)."

# 7. Watchlist Backlog Challenges
capture_slide "7" "07_backlog_challenges.png" \
    "Watchlist Backlog Challenges" \
    "Go to Library -> Challenges (showing active marathons/progress rings)."

# 8. Daily Cinema Games / Curated Lists
capture_slide "8" "08_daily_games.png" \
    "Daily Games & Community Collections" \
    "Open Daily Cinema Wordle, Daily Poll, or Community Curated Lists."

echo "========================================================"
echo "🎉 All screenshots captured successfully!"
echo "📁 Files saved in: $OUTPUT_DIR"
echo "Tip: You can now drag and drop them into GleeeStudio!"
echo "========================================================"
