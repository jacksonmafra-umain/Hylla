#!/usr/bin/env bash
# Captures the fleet in every fold state at 100% and 200% text, on a foldable emulator.
#
#   ANDROID_SERIAL=emulator-5556 scripts/android-matrix.sh [out-dir]
#
# Needs an emulator with fold states (fold_api36, or any foldable AVD) and a debug build installed.
# Restores the fold state, rotation and font scale it found.
set -euo pipefail

: "${ANDROID_SERIAL:?Set ANDROID_SERIAL to the foldable emulator, never a physical device}"
case "$ANDROID_SERIAL" in emulator-*) ;; *) echo "Refusing to drive $ANDROID_SERIAL: emulators only" >&2; exit 1 ;; esac

out="${1:-build/matrix/android}"
mkdir -p "$out"
package=com.umain.hylla

original_scale=$(adb shell settings get system font_scale | tr -d '\r')
original_rotation=$(adb shell settings get system user_rotation | tr -d '\r')
restore() {
  adb shell cmd device_state state reset >/dev/null
  adb shell settings put system font_scale "$original_scale"
  adb shell settings put system user_rotation "$original_rotation"
}
trap restore EXIT

adb shell settings put system accelerometer_rotation 0

# The display that is on now: a fold has two, and screencap needs to be told which.
active_display() {
  adb shell dumpsys display | grep -o "isActive=true, displayId=[0-9]*, uniqueId='local:[0-9]*'" | head -1 | grep -o "local:[0-9]*" | cut -d: -f2
}

capture() { # name
  # A fold change can turn the screen off and lock it; wake it and dismiss an insecure keyguard.
  adb shell input keyevent KEYCODE_WAKEUP
  adb shell wm dismiss-keyguard
  adb shell am force-stop "$package"
  adb shell am start -W -n "$package/.MainActivity" >/dev/null
  sleep 2
  adb exec-out screencap -d "$(active_display)" -p > "$out/$1.png"
  echo "$out/$1.png"
}

for scale in 1.0 2.0; do
  adb shell settings put system font_scale "$scale"
  for state in 0:closed 1:half-opened 2:opened; do
    adb shell cmd device_state state "${state%%:*}"
    for rotation in 0:portrait 1:landscape; do
      adb shell settings put system user_rotation "${rotation%%:*}"
      capture "${state#*:}-${rotation#*:}-text${scale/./}"
    done
  done
done
