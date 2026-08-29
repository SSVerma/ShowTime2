#!/usr/bin/env bash
# ShowTime Code Quality Verification Script
# Validates entire codebase or staged changes against docs/CODE_QUALITY_AND_SECURITY_GUIDE.md

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "\n${BOLD}${BLUE}==> Running ShowTime Code Quality & Security Audit...${NC}\n"

TARGET_DIR="${1:-.}"
HAS_ERRORS=0

report_error() {
    local rule_title="$1"
    local file="$2"
    local line_num="$3"
    local line_content="$4"

    echo -e "${RED}[VIOLATION] ${BOLD}${rule_title}${NC}"
    echo -e "  ${PURPLE}File:${NC} ${file}:${line_num}"
    echo -e "  ${YELLOW}Code:${NC} ${line_content}"
    echo ""
    HAS_ERRORS=1
}

# 1. Wildcard Imports
echo "Checking for wildcard imports..."
while IFS=: read -r file line_num line_content; do
    [ -z "$file" ] && continue
    report_error "Wildcard import found (import .*)" "$file" "$line_num" "$line_content"
done < <(grep -rnE --include="*.kt" --include="*.java" --exclude-dir=".git" --exclude-dir="build" "^import +[a-zA-Z0-9_.]+\.\*" "$TARGET_DIR" || true)

# 1b. Inline Fully Qualified Class Names (FQCN)
echo "Checking for inline fully qualified class names..."
while IFS=: read -r file line_num line_content; do
    [ -z "$file" ] && continue
    if [[ "$file" =~ /test/ ]] || [[ "$file" =~ /androidTest/ ]]; then
        continue
    fi
    if [[ "$line_content" =~ ^[[:space:]]*(package|import|//|\*) ]] || [[ "$line_content" =~ \"com\.ssverma\. ]]; then
        continue
    fi
    report_error "Inline fully qualified class name (FQCN) found. Use top-level import." "$file" "$line_num" "$line_content"
done < <(grep -rnE --include="*.kt" --include="*.java" --exclude-dir=".git" --exclude-dir="build" "com\.ssverma\.[a-zA-Z0-9_.]+" "$TARGET_DIR" || true)

# 2. Hardcoded Hex Colors
echo "Checking for hardcoded hex colors..."
while IFS=: read -r file line_num line_content; do
    [ -z "$file" ] && continue
    if [[ "$file" =~ (Color|Theme|Palette|Spacing|Default|Brush)\.kt$ ]] || [[ "$file" =~ /test/ ]] || [[ "$file" =~ /androidTest/ ]]; then
        continue
    fi
    report_error "Hardcoded Hex Color found. Use MaterialTheme.colorScheme tokens." "$file" "$line_num" "$line_content"
done < <(grep -rnE --include="*.kt" --exclude-dir=".git" --exclude-dir="build" "Color\((0x[0-9a-fA-F]{6,8}|[0-9]+)\)" "$TARGET_DIR" || true)

# 3. Leftover Debug Logs
echo "Checking for leftover debug statements..."
while IFS=: read -r file line_num line_content; do
    [ -z "$file" ] && continue
    if [[ "$file" =~ /test/ ]] || [[ "$file" =~ /androidTest/ ]] || [[ "$file" =~ /debug/ ]] || [[ "$file" =~ Debug.*\.kt$ ]]; then
        continue
    fi
    if [[ "$line_content" =~ ^[[:space:]]*// ]]; then
        continue
    fi
    report_error "Leftover debug log or print statement." "$file" "$line_num" "$line_content"
done < <(grep -rnE --include="*.kt" --include="*.java" --exclude-dir=".git" --exclude-dir="build" "(Log\.[dvew]\(|println\(|printStackTrace\(\)|System\.out\.print)" "$TARGET_DIR" || true)

# 4. Leaked Secrets
echo "Checking for leaked secrets / keys..."
while IFS=: read -r file line_num line_content; do
    [ -z "$file" ] && continue
    report_error "Potential API Key / Secret Token hardcoded." "$file" "$line_num" "$line_content"
done < <(grep -rnE --include="*.kt" --include="*.java" --exclude-dir=".git" --exclude-dir="build" "(api_key|apiKey|client_secret|clientSecret|private_key)\s*=\s*\"[a-zA-Z0-9_-]{24,}\"" "$TARGET_DIR" || true)

if [ $HAS_ERRORS -ne 0 ]; then
    echo -e "${RED}${BOLD}[FAILED] Quality audit found violations!${NC}\n"
    exit 1
else
    echo -e "${GREEN}${BOLD}[SUCCESS] Full quality audit passed with 0 violations! [✓]${NC}\n"
    exit 0
fi
