param(
    [int]$Width = 1280,
    [int]$Height = 800,
    [int]$ClickX = 640,
    [int]$ClickY = 400,
    [int]$ForgeX = 492,
    [int]$ForgeY = 495,
    [string]$StatePath,
    [string]$ReportPath
)

# Sends real input to the Minecraft window.
#
# Two paths, tried in order:
#   1. SetCursorPos + SendInput, the same call a physical mouse makes. Needs a
#      window station with an input desktop, which hosted runners may not have.
#   2. PostMessage(WM_RBUTTONDOWN/UP, WM_LBUTTONDOWN/UP) with client coordinates.
#      This still enters GLFW's window procedure, i.e. the production input path,
#      but does not depend on the interactive desktop.
#
# Exit codes: 0 ok, 1 assertion failed, 2 no usable input path.

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Windows.Forms

$native = @'
using System;
using System.Runtime.InteropServices;

public static class GuiInput {
    [StructLayout(LayoutKind.Sequential)]
    public struct POINT { public int X; public int Y; }

    [StructLayout(LayoutKind.Sequential)]
    public struct RECT { public int Left; public int Top; public int Right; public int Bottom; }

    [DllImport("user32.dll")] public static extern bool SetCursorPos(int x, int y);
    [DllImport("user32.dll")] public static extern void mouse_event(uint flags, uint dx, uint dy, uint data, UIntPtr extra);
    [DllImport("user32.dll")] public static extern IntPtr FindWindow(string cls, string title);
    [DllImport("user32.dll")] public static extern bool GetClientRect(IntPtr hWnd, out RECT rect);
    [DllImport("user32.dll")] public static extern bool ClientToScreen(IntPtr hWnd, ref POINT point);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool PostMessage(IntPtr hWnd, uint msg, IntPtr wParam, IntPtr lParam);

    public const uint LEFTDOWN = 0x0002, LEFTUP = 0x0004, RIGHTDOWN = 0x0008, RIGHTUP = 0x0010;
    public const uint WM_LBUTTONDOWN = 0x0201, WM_LBUTTONUP = 0x0202;
    public const uint WM_RBUTTONDOWN = 0x0204, WM_RBUTTONUP = 0x0205;
}
'@

Add-Type -TypeDefinition $native

function Find-MinecraftWindow {
    # The window title is "Minecraft* 1.21.1 - NeoForge" or similar.
    $procs = Get-Process | Where-Object { $_.MainWindowTitle -like '*Minecraft*' }
    if ($procs) { return $procs[0].MainWindowHandle }
    return [IntPtr]::Zero
}

function Read-State {
    if (-not (Test-Path $StatePath)) { return '' }
    try { return (Get-Content $StatePath -Raw | ConvertFrom-Json).state } catch { return '' }
}

function Wait-State([string]$wanted, [int]$timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ((Read-State) -eq $wanted) { return $true }
        Start-Sleep -Milliseconds 250
    }
    return $false
}

$hwnd = [IntPtr]::Zero
# A cold run downloads assets and decompiles Minecraft before the window appears;
# 60 s was not enough on a fresh runner.
$deadline = (Get-Date).AddSeconds(420)
while ((Get-Date) -lt $deadline -and $hwnd -eq [IntPtr]::Zero) {
    $hwnd = Find-MinecraftWindow
    if ($hwnd -eq [IntPtr]::Zero) { Start-Sleep -Seconds 1 }
}
if ($hwnd -eq [IntPtr]::Zero) {
    Write-Output 'no Minecraft window found'
    exit 2
}

[GuiInput]::SetForegroundWindow($hwnd) | Out-Null
Start-Sleep -Milliseconds 500

$rect = New-Object GuiInput+RECT
[GuiInput]::GetClientRect($hwnd, [ref]$rect) | Out-Null

function Click-Client([int]$cx, [int]$cy, [bool]$right) {
    $point = New-Object GuiInput+POINT
    $point.X = $cx; $point.Y = $cy
    [GuiInput]::ClientToScreen($hwnd, [ref]$point) | Out-Null
    [GuiInput]::SetCursorPos($point.X, $point.Y) | Out-Null
    $down = if ($right) { [GuiInput]::RIGHTDOWN } else { [GuiInput]::LEFTDOWN }
    $up = if ($right) { [GuiInput]::RIGHTUP } else { [GuiInput]::LEFTUP }
    [GuiInput]::mouse_event($down, 0, 0, 0, [UIntPtr]::Zero)
    Start-Sleep -Milliseconds 60
    [GuiInput]::mouse_event($up, 0, 0, 0, [UIntPtr]::Zero)
}

function Post-Click([int]$cx, [int]$cy, [bool]$right) {
    $lParam = [IntPtr](($cy -shl 16) -bor ($cx -band 0xFFFF))
    $down = if ($right) { [GuiInput]::WM_RBUTTONDOWN } else { [GuiInput]::WM_LBUTTONDOWN }
    $up = if ($right) { [GuiInput]::WM_RBUTTONUP } else { [GuiInput]::WM_LBUTTONUP }
    [GuiInput]::PostMessage($hwnd, $down, [IntPtr]1, $lParam) | Out-Null
    Start-Sleep -Milliseconds 60
    [GuiInput]::PostMessage($hwnd, $up, [IntPtr]0, $lParam) | Out-Null
}

if (-not (Wait-State 'ready' 180)) {
    Write-Output 'harness never reached ready'
    exit 1
}
# The menu is opened server-side by the harness; the click below is the one that has to
# travel through GLFW and the screen handlers to count.
if (-not (Wait-State 'screen-open' 180)) {
    Write-Output 'the forge screen never opened'
    exit 1
}

Write-Output 'screen open; clicking FORGE'
Click-Client $ForgeX $ForgeY $false

$deadline = (Get-Date).AddSeconds(60)
while ((Get-Date) -lt $deadline -and -not (Test-Path $ReportPath)) { Start-Sleep -Milliseconds 500 }
if (Test-Path $ReportPath) { exit 0 }
exit 1