#!/usr/bin/env python3
"""Install locally built components on one explicitly selected device. Never flashes."""
import argparse
import json
from pathlib import Path
import subprocess
import sys

MODULES = {
    "app": "local.thinksmart.chatgpt",
    "dashboard": "local.thinksmart.dashboard",
    "screen-toggle": "local.thinksmart.screenmode",
    "reboot-button": "local.thinksmart.reboot",
    "manual-keyboard": "local.thinksmart.manualkeyboard",
}
ROOT = Path(__file__).resolve().parents[1]

def installed_apk_paths(result):
    """Android pm path exits 1 with empty output when a package is absent."""
    lines=result.stdout.splitlines()
    if result.returncode in (0,1) and not lines and not result.stderr.strip():
        return []
    if result.returncode or result.stderr.strip() or any(not line.startswith("package:/") for line in lines):
        raise RuntimeError("Could not safely query installed APKs; installation stopped")
    return [line.removeprefix("package:") for line in lines]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--backup-dir", type=Path, required=True,
                        help="Existing private directory on your chosen backup drive")
    parser.add_argument("--apply", action="store_true", help="Otherwise only inspect and show plan")
    options = parser.parse_args()
    def adb(*args, check=True):
        return subprocess.run(["adb", "-s", options.serial, *args], check=check,
                              capture_output=True, text=True)
    if not options.backup_dir.is_dir():
        parser.error("Create and verify your backup directory on the mounted external drive first")
    if adb("get-state").stdout.strip() != "device":
        parser.error("Device is not ready; confirm USB debugging on the device")
    properties = {}
    for name in ("ro.product.model", "ro.build.version.sdk", "ro.build.version.release",
                 "ro.product.cpu.abilist", "ro.build.fingerprint"):
        properties[name] = adb("shell", "getprop", name).stdout.strip()
    print(json.dumps(properties, ensure_ascii=False, indent=2))
    if properties["ro.build.version.sdk"] != "27" or "armeabi-v7a" not in properties["ro.product.cpu.abilist"].split(","):
        parser.error("This source release is validated for Android 8.1/API27 ARMv7 only; review adaptation separately")
    apks = {module: ROOT / module / "build/outputs/apk/debug" / f"{module}-debug.apk"
            for module in MODULES}
    missing = [str(path.relative_to(ROOT)) for path in apks.values() if not path.is_file()]
    if missing:
        parser.error("Build assembleDebug first. Missing: " + ", ".join(missing))
    print("Plan: backup existing component APKs and screen timeout; install 5 locally built APKs; set 30-second timeout; open local parent setup.")
    print("No firmware/root/account/IME/home/kiosk change. APK backup does not include private app data.")
    if not options.apply:
        print("Read-only plan finished. Review it before adding --apply.")
        return
    destination = options.backup_dir / "learning-station-before-install"
    destination.mkdir(mode=0o700, exist_ok=False)
    snapshot = {"properties": properties, "screen_off_timeout": adb("shell", "settings", "get", "system", "screen_off_timeout").stdout.strip()}
    saved = destination / "state.json"
    saved.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2)); saved.chmod(0o600)
    for module, package in MODULES.items():
        result = adb("shell", "pm", "path", package, check=False)
        paths = installed_apk_paths(result)
        for index, path in enumerate(paths):
            target = destination / f"{module}-{index}.apk"
            adb("pull", path, str(target)); target.chmod(0o600)
    for module in MODULES:
        result = adb("install", "-r", str(apks[module]), check=False)
        if result.returncode or "Success" not in result.stdout:
            print(f"Installation stopped at {module}. No uninstall or data wipe was attempted.", file=sys.stderr)
            print("Check signature/version compatibility locally; use your existing signing key for updates.", file=sys.stderr)
            sys.exit(1)
    adb("shell", "settings", "put", "system", "screen_off_timeout", "30000")
    adb("shell", "am", "start", "-n", "local.thinksmart.chatgpt/.ParentSetupActivity")
    print("Installed. Parent must privately create the password on the device. Continue with docs/INSTALL.md permissions and manual acceptance.")

if __name__ == "__main__":
    main()
