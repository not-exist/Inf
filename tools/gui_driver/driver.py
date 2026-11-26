#!/usr/bin/env python3
"""Drive a real Minecraft client and check that OS-level input reaches the mod.

The harness (src/clienttest) creates a world, places a forge table and waits. This
script injects a genuine right-click and a left-click, then reads the report the
harness writes. Nothing here calls into Minecraft: the input goes through
X11/XTEST (Linux), SendInput/PostMessage (Windows) or CGEvent (macOS), so the
click has to survive GLFW, ScreenEvent and AbstractContainerScreen to count.

Exit codes: 0 pass, 1 assertion failure, 2 platform capability missing.
"""

from __future__ import annotations

import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
RUN_DIR = REPO / "run"
# The harness writes relative to the game directory, which is run/.
STATE_PATH = RUN_DIR / "clienttest" / "state.json"
REPORT_PATH = RUN_DIR / "clienttest" / "report.json"
EVENTS_PATH = RUN_DIR / "clienttest" / "events.log"

WINDOW_W, WINDOW_H = 1280, 800
# ForgeScreen centres a 420x260 panel; the FORGE button sits at leftPos+12, topPos+212
# and is 100x26, so its centre is 62 px right and 225 px down from the panel origin.
PANEL_W, PANEL_H = 420, 260
LEFT = (WINDOW_W - PANEL_W) // 2
TOP = (WINDOW_H - PANEL_H) // 2
FORGE_BUTTON = (LEFT + 62, TOP + 225)


def log(message: str) -> None:
    print(f"[gui-driver] {message}", flush=True)


def read_state() -> dict:
    try:
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    except (FileNotFoundError, json.JSONDecodeError):
        return {}


def wait_for(predicate, timeout: float, description: str) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        if predicate():
            return True
        time.sleep(0.25)
    log(f"timed out waiting for {description}")
    return False


class LinuxInput:
    """XTEST through ctypes, so no extra packages are installed on the runner."""

    def __init__(self) -> None:
        import ctypes

        self.ctypes = ctypes
        try:
            self.x11 = ctypes.CDLL("libX11.so.6")
            self.xtst = ctypes.CDLL("libXtst.so.6")
        except OSError as exc:
            raise SystemExitWithCode(2, f"libX11/libXtst missing: {exc}") from exc
        self.x11.XOpenDisplay.restype = ctypes.c_void_p
        self.display = self.x11.XOpenDisplay(None)
        if not self.display:
            raise SystemExitWithCode(2, "cannot open the X display")

    def move(self, x: int, y: int) -> None:
        self.xtst.XTestFakeMotionEvent(self.ctypes.c_void_p(self.display), -1, x, y, 0)
        self.x11.XFlush(self.ctypes.c_void_p(self.display))

    def click(self, x: int, y: int, button: int) -> None:
        self.move(x, y)
        time.sleep(0.15)
        self.xtst.XTestFakeButtonEvent(self.ctypes.c_void_p(self.display), button, 1, 0)
        self.x11.XFlush(self.ctypes.c_void_p(self.display))
        time.sleep(0.08)
        self.xtst.XTestFakeButtonEvent(self.ctypes.c_void_p(self.display), button, 0, 0)
        self.x11.XFlush(self.ctypes.c_void_p(self.display))

    def key(self, keysym: int) -> None:
        self.xtst.XTestFakeKeyEvent(self.ctypes.c_void_p(self.display), keysym, 1, 0)
        self.x11.XFlush(self.ctypes.c_void_p(self.display))
        time.sleep(0.05)
        self.xtst.XTestFakeKeyEvent(self.ctypes.c_void_p(self.display), keysym, 0, 0)
        self.x11.XFlush(self.ctypes.c_void_p(self.display))


class SystemExitWithCode(Exception):
    def __init__(self, code: int, message: str) -> None:
        super().__init__(message)
        self.code = code
        self.message = message


def find_window_linux() -> bool:
    """Move the Minecraft window to 0,0 so screen coordinates are predictable."""
    for command in ("wmctrl", "xdotool"):
        if shutil.which(command) is None:
            continue
        try:
            listing = subprocess.run(["xwininfo", "-root", "-tree"], capture_output=True, text=True, check=True).stdout
        except (subprocess.CalledProcessError, FileNotFoundError):
            continue
        for line in listing.splitlines():
            if "Minecraft" in line:
                window_id = line.strip().split()[0]
                subprocess.run([command] + (["-i", "-r", window_id, "-e", "0,0,0,1280,800"] if command == "wmctrl"
                                            else ["windowmove", window_id, "0", "0"]), check=False)
                return True
    log("no window manager helper available; assuming the window is already at 0,0")
    return True


def run_linux(scenario: str) -> int:
    if not os.environ.get("DISPLAY"):
        raise SystemExitWithCode(2, "DISPLAY is not set; run under xvfb-run")
    find_window_linux()
    inputs = LinuxInput()

    # The harness advances on its own from creating-world to screen-open; a late-attaching
    # driver must not insist on observing the intermediate 'ready' state.
    if not wait_for(lambda: read_state().get("state") in ("ready", "screen-open"), 180, "the harness to start"):
        return 1
    if not wait_for(lambda: read_state().get("state") == "screen-open", 180, "the forge screen to open"):
        log("the screen never opened; see run/clienttest/events.log")
        return 1
    # The menu is opened by the harness on the server side; the click below is the part
    # that has to be real, because it must survive GLFW, XTEST and the screen handlers.
    log("screen open; clicking FORGE at %s" % (FORGE_BUTTON,))
    inputs.click(*FORGE_BUTTON, 1)

    if wait_for(lambda: REPORT_PATH.exists(), 60, "the harness report"):
        return evaluate_report()
    return 1


def run_windows(scenario: str) -> int:
    """SendInput first; fall back to PostMessage, which still enters GLFW's WndProc."""
    script = Path(__file__).with_name("drive_windows.ps1")
    result = subprocess.run(
        ["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", str(script),
         "-Width", str(WINDOW_W), "-Height", str(WINDOW_H),
         "-ClickX", str(WINDOW_W // 2), "-ClickY", str(WINDOW_H // 2),
         "-ForgeX", str(FORGE_BUTTON[0]), "-ForgeY", str(FORGE_BUTTON[1]),
         "-StatePath", str(STATE_PATH), "-ReportPath", str(REPORT_PATH)],
        capture_output=True, text=True, timeout=600)
    sys.stdout.write(result.stdout)
    sys.stderr.write(result.stderr)
    if result.returncode == 2:
        return 2
    if REPORT_PATH.exists():
        return evaluate_report()
    return 1


def run_macos(scenario: str) -> int:
    script = Path(__file__).with_name("drive_macos.swift")
    binary = Path(tempfile.gettempdir()) / "inf-gui-driver"
    build = subprocess.run(["swiftc", "-O", str(script), "-o", str(binary)], capture_output=True, text=True)
    if build.returncode != 0:
        log(build.stderr)
        return 2
    result = subprocess.run(
        [str(binary), str(WINDOW_W), str(WINDOW_H), str(WINDOW_W // 2), str(WINDOW_H // 2),
         str(FORGE_BUTTON[0]), str(FORGE_BUTTON[1]), str(STATE_PATH), str(REPORT_PATH)],
        capture_output=True, text=True, timeout=600)
    sys.stdout.write(result.stdout)
    sys.stderr.write(result.stderr)
    if result.returncode == 2:
        return 2
    if REPORT_PATH.exists():
        return evaluate_report()
    return 1


def evaluate_report() -> int:
    try:
        report = json.loads(REPORT_PATH.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        log(f"report is not valid JSON: {exc}")
        return 1
    if report.get("ok") is True and report.get("tier") == "A":
        log(f"pass: {report.get('detail')} (GL: {report.get('gl')})")
        return 0
    log(f"fail: {report.get('failure') or report.get('detail')}")
    if EVENTS_PATH.exists():
        log("events:\n" + EVENTS_PATH.read_text(encoding="utf-8"))
    return 1


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--os", default=sys.platform)
    parser.add_argument("--scenario", default="forge-click")
    args = parser.parse_args()

    REPORT_PATH.unlink(missing_ok=True)
    try:
        if args.os.startswith("linux") or args.os == "linux":
            return run_linux(args.scenario)
        if args.os.startswith("win"):
            return run_windows(args.scenario)
        if args.os in ("darwin", "macos"):
            return run_macos(args.scenario)
        log(f"unsupported platform: {args.os}")
        return 2
    except SystemExitWithCode as exc:
        log(exc.message)
        return exc.code


if __name__ == "__main__":
    sys.exit(main())