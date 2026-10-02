#!/usr/bin/env bash
# Captures the fleet on each simulator at the default and the largest Dynamic Type size.
#
#   scripts/ios-matrix.sh [out-dir] [simulator-udid...]
#
# Without UDIDs it uses every booted simulator. The app must be installed (build and run once,
# or `xcodebuild ... build` then `simctl install`). Restores each simulator's text size.
set -euo pipefail

out="${1:-build/matrix/ios}"
shift || true
mkdir -p "$out"
bundle=com.umain.hylla

devices=("$@")
if [ ${#devices[@]} -eq 0 ]; then
  while IFS= read -r udid; do devices+=("$udid"); done < <(xcrun simctl list devices booted -j | python3 -c '
import json, sys
for runtime in json.load(sys.stdin)["devices"].values():
    for d in runtime:
        if d["state"] == "Booted": print(d["udid"])')
fi

for udid in "${devices[@]}"; do
  name=$(xcrun simctl list devices -j | python3 -c "
import json, sys
for runtime in json.load(sys.stdin)['devices'].values():
    for d in runtime:
        if d['udid'] == '$udid': print(d['name'].replace(' ', '-').replace('(', '').replace(')', ''))")
  original=$(xcrun simctl ui "$udid" content_size)
  for size in large accessibility-extra-extra-extra-large; do
    xcrun simctl ui "$udid" content_size "$size"
    xcrun simctl terminate "$udid" "$bundle" 2>/dev/null || true
    xcrun simctl launch "$udid" "$bundle" -HyllaResetDefaults YES >/dev/null
    sleep 3
    xcrun simctl io "$udid" screenshot "$out/$name-$size.png" >/dev/null 2>&1
    echo "$out/$name-$size.png"
  done
  xcrun simctl ui "$udid" content_size "$original"
done
