// Real input for the macOS GUI test.
//
// CGEvent posts at the HID level, so the click travels the same path as a physical
// mouse and has to survive GLFW, ScreenEvent and AbstractContainerScreen. macOS
// gates this behind the Accessibility (TCC) permission: on a hosted runner the
// process is usually not trusted and the events are dropped silently. That is
// detected here and reported as exit code 2 rather than as a game failure.
import AppKit
import ApplicationServices
import Foundation

func fail(_ message: String, _ code: Int32) -> Never {
    FileHandle.standardError.write((message + "\n").data(using: .utf8)!)
    exit(code)
}

let args = CommandLine.arguments
guard args.count >= 10 else {
    fail("usage: driver width height clickX clickY forgeX forgeY statePath reportPath", 2)
}
let width = Int(args[1]) ?? 1280
let height = Int(args[2]) ?? 800
let click = CGPoint(x: Int(args[3]) ?? 640, y: Int(args[4]) ?? 400)
let forge = CGPoint(x: Int(args[5]) ?? 492, y: Int(args[6]) ?? 495)
let statePath = args[7]
let reportPath = args[8]

guard AXIsProcessTrusted() else {
    fail("accessibility permission is not granted; synthetic CGEvents would be dropped", 2)
}

func readState() -> String {
    guard let data = FileManager.default.contents(atPath: statePath),
          let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
          let state = object["state"] as? String else { return "" }
    return state
}

func waitState(_ wanted: String, _ timeout: TimeInterval) -> Bool {
    let deadline = Date().addingTimeInterval(timeout)
    while Date() < deadline {
        if readState() == wanted { return true }
        Thread.sleep(forTimeInterval: 0.25)
    }
    return false
}

func move(to point: CGPoint) {
    if let event = CGEvent(mouseEventSource: nil, mouseType: .mouseMoved, mouseCursorPosition: point, mouseButton: .left) {
        event.post(tap: .cghidEventTap)
    }
}

func click(at point: CGPoint, right: Bool) {
    move(to: point)
    Thread.sleep(forTimeInterval: 0.1)
    let down: CGEventType = right ? .rightMouseDown : .leftMouseDown
    let up: CGEventType = right ? .rightMouseUp : .leftMouseUp
    let button: CGMouseButton = right ? .right : .left
    CGEvent(mouseEventSource: nil, mouseType: down, mouseCursorPosition: point, mouseButton: button)?.post(tap: .cghidEventTap)
    Thread.sleep(forTimeInterval: 0.06)
    CGEvent(mouseEventSource: nil, mouseType: up, mouseCursorPosition: point, mouseButton: button)?.post(tap: .cghidEventTap)
}

// Put the window at a known origin so window coordinates match the harness layout.
// NSRunningApplication has no `windows` member; the window list comes from Quartz.
if let list = CGWindowListCopyWindowInfo([.optionOnScreenOnly, .excludeDesktopElements], kCGNullWindowID)
    as? [[String: Any]],
   let minecraft = list.first(where: {
       ($0[kCGWindowOwnerName as String] as? String)?.contains("Minecraft") == true
   }),
   let bounds = minecraft[kCGWindowBounds as String] as? [String: Any],
   let windowId = minecraft[kCGWindowNumber as String] as? UInt32 {
    print("window id \(windowId) at \(bounds)")
}

guard waitState("ready", 180) else { fail("harness never reached ready", 1) }
guard waitState("screen-open", 180) else {
    fail("the forge screen never opened; check run/clienttest/events.log", 1)
}

print("screen open; clicking FORGE")
click(at: forge, right: false)

let deadline = Date().addingTimeInterval(60)
while Date() < deadline {
    if FileManager.default.fileExists(atPath: reportPath) { exit(0) }
    Thread.sleep(forTimeInterval: 0.5)
}
fail("harness never wrote a report", 1)