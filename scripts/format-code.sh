#!/usr/bin/env bash
# ShowTime Code Formatter (Cmd + Option + L Equivalent)
# Formats Kotlin, Java, and XML files using Android Studio CLI Formatter

set -e

GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'

FORMATTER_BIN=""
if [ -x "/Applications/Android Studio.app/Contents/bin/format.sh" ]; then
    FORMATTER_BIN="/Applications/Android Studio.app/Contents/bin/format.sh"
elif [ -x "/Applications/Android Studio Preview.app/Contents/bin/format.sh" ]; then
    FORMATTER_BIN="/Applications/Android Studio Preview.app/Contents/bin/format.sh"
elif [ -x "/Applications/IntelliJ IDEA.app/Contents/bin/format.sh" ]; then
    FORMATTER_BIN="/Applications/IntelliJ IDEA.app/Contents/bin/format.sh"
elif [ -x "$HOME/Applications/Android Studio.app/Contents/bin/format.sh" ]; then
    FORMATTER_BIN="$HOME/Applications/Android Studio.app/Contents/bin/format.sh"
elif command -v format.sh &> /dev/null; then
    FORMATTER_BIN=$(command -v format.sh)
fi

if [ -z "$FORMATTER_BIN" ]; then
    echo -e "${RED}[ERROR] Android Studio CLI formatter (format.sh) not found.${NC}"
    echo -e "${YELLOW}Please ensure Android Studio is installed in /Applications or format.sh is in your PATH.${NC}"
    exit 1
fi

TARGET="${1:-staged}"

echo -e "\n${BOLD}${CYAN}==> [ShowTime Code Formatter] Using: ${FORMATTER_BIN}${NC}"

if [ "$TARGET" == "staged" ]; then
    FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(kt|kts|java|xml)$' || true)
    if [ -z "$FILES" ]; then
        echo -e "${YELLOW}No staged code files (.kt, .kts, .java, .xml) found to reformat.${NC}\n"
        exit 0
    fi
    echo -e "${CYAN}Reformatting staged code files (Cmd + Option + L equivalent)...${NC}"
    TARGET_FILES=()
    while IFS= read -r f; do
        [ -f "$f" ] && TARGET_FILES+=("$f")
    done <<< "$FILES"

    if [ ${#TARGET_FILES[@]} -gt 0 ]; then
        "$FORMATTER_BIN" -allowDefaults "${TARGET_FILES[@]}" > /dev/null 2>&1 || true
        for f in "${TARGET_FILES[@]}"; do
            git add "$f"
        done
        echo -e "${GREEN}[✓] Reformat complete! Re-staged ${#TARGET_FILES[@]} files.${NC}\n"
    fi
elif [ "$TARGET" == "all" ]; then
    echo -e "${CYAN}Reformatting all modified code files in working tree...${NC}"
    FILES=$(git diff --name-only | grep -E '\.(kt|kts|java|xml)$' || true)
    if [ -z "$FILES" ]; then
        echo -e "${YELLOW}No modified code files found.${NC}\n"
        exit 0
    fi
    TARGET_FILES=()
    while IFS= read -r f; do
        [ -f "$f" ] && TARGET_FILES+=("$f")
    done <<< "$FILES"

    if [ ${#TARGET_FILES[@]} -gt 0 ]; then
        "$FORMATTER_BIN" -allowDefaults "${TARGET_FILES[@]}" > /dev/null 2>&1 || true
        echo -e "${GREEN}[✓] Reformat complete for ${#TARGET_FILES[@]} modified files.${NC}\n"
    fi
elif [ -f "$TARGET" ] || [ -d "$TARGET" ]; then
    echo -e "${CYAN}Reformatting target: ${TARGET}...${NC}"
    "$FORMATTER_BIN" -allowDefaults "$TARGET" > /dev/null 2>&1 || true
    echo -e "${GREEN}[✓] Reformat complete!${NC}\n"
else
    echo -e "${RED}[ERROR] Invalid target: ${TARGET}${NC}"
    echo "Usage: ./scripts/format-code.sh [staged|all|<file-or-dir-path>]"
    exit 1
fi
