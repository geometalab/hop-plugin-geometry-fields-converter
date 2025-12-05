#!/usr/bin/env python3

import json
from pathlib import Path
import sys

JSON_FILE = Path("/files/out.json")

if not JSON_FILE.is_file():
    print(f"ERROR: JSON file '{JSON_FILE}' not found!")
    sys.exit(1)

with open(JSON_FILE) as f:
    data = json.load(f)

def get_field(name, field):
    for item in data.get("data", []):
        if item.get("transformName") == name:
            return item.get(field)
    return None

# Extract metrics
SMALL_ROWS = get_field("Small WKT to WKB", "transformLinesWritten")
SMALL_DURATION = get_field("Small WKT to WKB", "transformDuration")

BIG_ROWS = get_field("Big WKT to WKB", "transformLinesWritten")
BIG_DURATION = get_field("Big WKT to WKB", "transformDuration")

# Validate
if SMALL_ROWS is None or BIG_ROWS is None:
    print("ERROR: Missing transform data. Check transform names.")
    sys.exit(1)

# Durations are in milliseconds → convert to seconds
small_speed = float(SMALL_ROWS) / (float(SMALL_DURATION) / 1000)
big_speed = float(BIG_ROWS) / (float(BIG_DURATION) / 1000)
ratio = big_speed / small_speed

print("---------------------------")
print(" Performance Evaluation")
print("---------------------------")
print(f"Small WKT→WKB speed: {small_speed:.2f} rows/sec")
print(f"Big   WKT→WKB speed: {big_speed:.2f} rows/sec")
print(f"Ratio (big/small):   {ratio:.2f}\n")

# Threshold evaluation
if ratio >= 0.90:
    print("Result: OUTSTANDING")
elif ratio >= 0.75:
    print("Result: TARGET")
elif ratio >= 0.50:
    print("Result: MINIMAL")
else:
    print("Result: FAIL – Performance too low!")
    sys.exit(1)
