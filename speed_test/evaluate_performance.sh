#!/usr/bin/env bash
set -euo pipefail

JSON_FILE="metrics.json"

if [[ ! -f "$JSON_FILE" ]]; then
  echo "ERROR: JSON file '$JSON_FILE' not found!"
  exit 1
fi

# Helper function to extract a JSON field for a given transform
get_field() {
  local name="$1"
  local field="$2"
  jq -r ".data[] | select(.transformName==\"$name\") | .$field" "$JSON_FILE"
}

# Extract metrics
SMALL_ROWS=$(get_field "Small WKT to WKB" "transformLinesWritten")
SMALL_DURATION=$(get_field "Small WKT to WKB" "transformDuration")

BIG_ROWS=$(get_field "Big WKT to WKB" "transformLinesWritten")
BIG_DURATION=$(get_field "Big WKT to WKB" "transformDuration")

# Validate
if [[ -z "$SMALL_ROWS" || -z "$BIG_ROWS" ]]; then
  echo "ERROR: Missing transform data. Check transform names."
  exit 1
fi

# Durations are in milliseconds -> convert to seconds
small_speed=$(echo "$SMALL_ROWS / ($SMALL_DURATION / 1000)" | bc -l)
big_speed=$(echo "$BIG_ROWS / ($BIG_DURATION / 1000)" | bc -l)

ratio=$(echo "$big_speed / $small_speed" | bc -l)

echo "---------------------------"
echo " Performance Evaluation"
echo "---------------------------"
echo "Small WKT->WKB speed: $small_speed rows/sec"
echo "Big   WKT->WKB speed: $big_speed rows/sec"
echo "Ratio (big/small):   $ratio"
echo ""

# Threshold evaluation
if (( $(echo "$ratio >= 2.50" | bc -l) )); then
  echo "Result: OUTSTANDING"
elif (( $(echo "$ratio >= 1.50" | bc -l) )); then
  echo "Result: TARGET"
elif (( $(echo "$ratio >= 1.00" | bc -l) )); then
  echo "Result: MINIMAL"
else
  echo "Result: FAIL - Performance too low!"
  exit 1
fi
