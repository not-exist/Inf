#!/usr/bin/env bash
# End-to-end GUI test for Linux: Xvfb + real XTEST input.
#
# Everything runs inside one shell so the client, the window manager and the driver
# share a display. Used by .github/workflows/_gui.yml and by hand during development.
set -euo pipefail

DISPLAY_NUM="${DISPLAY_NUM:-99}"
SCREEN="${SCREEN:-1280x800x24}"
CLIENT_LOG="${CLIENT_LOG:-client.log}"

cd "$(dirname "$0")/../.."

cleanup() {
    [[ -n "${CLIENT_PID:-}" ]] && kill -9 "$CLIENT_PID" 2>/dev/null || true
    [[ -n "${XVFB_PID:-}" ]] && kill -9 "$XVFB_PID" 2>/dev/null || true
    [[ -n "${WM_PID:-}" ]] && kill -9 "$WM_PID" 2>/dev/null || true
}
trap cleanup EXIT

Xvfb ":$DISPLAY_NUM" -screen 0 "$SCREEN" > xvfb.log 2>&1 &
XVFB_PID=$!
export DISPLAY=":$DISPLAY_NUM"
sleep 3
if ! xdpyinfo > /dev/null 2>&1; then
    echo "Xvfb did not come up on $DISPLAY"
    exit 2
fi

# A window manager is optional but makes focus and window placement predictable.
if command -v openbox > /dev/null 2>&1; then
    openbox --sm-disable > wm.log 2>&1 &
    WM_PID=$!
    sleep 1
fi

rm -rf run/clienttest
mkdir -p run/clienttest

env DISPLAY="$DISPLAY" ./gradlew --no-daemon --console=plain prepareRunDir prepareClientTest > /dev/null
env DISPLAY="$DISPLAY" ./gradlew --no-daemon --console=plain runClientTest > "$CLIENT_LOG" 2>&1 &
CLIENT_PID=$!

# The driver polls the harness state; give the client time to reach the title screen.
python3 tools/gui_driver/driver.py --os linux