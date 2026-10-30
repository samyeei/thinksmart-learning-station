"""Explicit device-only keyboard activation/disable. No account or message access."""
import argparse
import json
from pathlib import Path
import subprocess
ADB = "adb"
SERIAL = None
def adb(*args):
    return subprocess.run([ADB,"-s",SERIAL,*args],check=True,capture_output=True,text=True).stdout.strip()

PACKAGE = "local.thinksmart.manualkeyboard"
SERVICE = PACKAGE + "/.ManualKeyboardService"
EMPTY = PACKAGE + "/.CollapsedIme"
CHINESE = "org.fcitx.fcitx5.android/.input.FcitxInputMethodService"
BACKUP = None
KEYS = ("default_input_method", "enabled_input_methods", "enabled_accessibility_services", "accessibility_enabled", "show_ime_with_hard_keyboard")

def services():
    value = adb("shell", "settings", "get", "secure", "enabled_accessibility_services")
    return [] if value in ("null", "") else value.split(":")

def set_services(items):
    if items:
        adb("shell", "settings", "put", "secure", "enabled_accessibility_services", ":".join(items))
        adb("shell", "settings", "put", "secure", "accessibility_enabled", "1")
    else:
        adb("shell", "settings", "delete", "secure", "enabled_accessibility_services")
        adb("shell", "settings", "put", "secure", "accessibility_enabled", "0")

def main():
    parser = argparse.ArgumentParser()
    global SERIAL, BACKUP
    parser.add_argument("--serial",required=True)
    parser.add_argument("--backup-dir",type=Path,required=True)
    parser.add_argument("action", choices=("enable", "disable", "status"))
    options = parser.parse_args()
    SERIAL=options.serial
    BACKUP=options.backup_dir / "keyboard-before.json"
    action=options.action
    if not options.backup_dir.is_dir(): parser.error("Backup directory must exist on your chosen drive")
    if action in ("enable","disable"):
        if "org.fcitx.fcitx5.android/" not in adb("shell","ime","list","-s"):
            parser.error("Install and enable Fcitx first; no settings changed")
    if action == "enable":
        if not BACKUP.exists():
            BACKUP.parent.mkdir(parents=True, exist_ok=True)
            BACKUP.write_text(json.dumps({k: adb("shell", "settings", "get", "secure", k) for k in KEYS}, indent=2))
            BACKUP.chmod(0o600)
        adb("shell", "pm", "grant", PACKAGE, "android.permission.WRITE_SECURE_SETTINGS")
        adb("shell", "ime", "enable", EMPTY)
        values = services()
        if SERVICE not in values:
            values.append(SERVICE)
        set_services(values)
    elif action == "disable":
        set_services([s for s in services() if s != SERVICE])
        adb("shell", "ime", "set", CHINESE)
        adb("shell", "ime", "disable", EMPTY)
    print(json.dumps({k: adb("shell", "settings", "get", "secure", k) for k in KEYS}, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
