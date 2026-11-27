#!/usr/bin/env bash
# End-to-end GUI test for Linux: Xvfb + real XTEST input.
#
# Everything runs inside one shell so the client, the window manager and the driver
# share a display. Used by .github/workflows/_gui.yml and by hand during development.
set -euo pipefail

SCREEN="${SCREEN:-1280x800x24}"
CLIENT_LOG="${CLIENT_LOG:-client.log}"

cd "$(dirname "$0")/../.."

cleanup() {
    [[ -n "${CLIENT_PID:-}" ]] && kill -9 "$CLIENT_PID" 2>/dev/null || true
    [[ -n "${XVFB_PID:-}" ]] && kill -9 "$XVFB_PID" 2>/dev/null || true
    [[ -n "${WM_PID:-}" ]] && kill -9 "$WM_PID" 2>/dev/null || true
}
trap cleanup EXIT

# Do not assume :99 is free: a runner image may already run a server there, and a
# stale /tmp/.X99-lock makes Xvfb exit at once, which reads as "did not come up".
DISPLAY_NUM="$(python3 tools/gui_driver/free_display.py)"
Xvfb ":$DISPLAY_NUM" -screen 0 "$SCREEN" > xvfb.log 2>&1 &
XVFB_PID=$!
export DISPLAY=":$DISPLAY_NUM"

# Probe the socket rather than xdpyinfo: x11-utils is not installed on the runner
# image, so a missing binary used to look like a broken X server.
ready=0
for _ in $(seq 1 40); do
    if [ -S "/tmp/.X11-unix/X$DISPLAY_NUM" ]; then ready=1; break; fi
    sleep 0.5
done
if [ "$ready" -ne 1 ]; then
    echo "Xvfb did not come up on $DISPLAY"
    cat xvfb.log || true
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

# The driver polls the harness state and fails on its own timeout.
python3 tools/gui_driver/driver.py --os linux