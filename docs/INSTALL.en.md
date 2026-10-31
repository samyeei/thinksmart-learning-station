# Installation and acceptance

[English](INSTALL.en.md) | [Chinese](INSTALL.md) | [Project home](../README.en.md)

## Scope and prerequisites

This source preview targets the reference ThinkSmart View running Android 8.1 / API 27 / ARMv7. Install apps on a working Android system first. This guide does not automatically root, flash, wipe data, disable system packages, or assign Device Owner. Assess other models and systems separately rather than transplanting full-device images.

Prepare ADB with USB debugging, JDK 17, Gradle 9.8.2, SDK 37.2, Node.js, Python 3.9+, and a mounted external backup drive. Replace `<device-serial>`, `<external-backup-directory>`, and `<device-ip>` below. Do not commit real device or household values.

The documentation is in English; app labels and parent pages remain primarily Chinese.

## 1. Inspect, back up, and build

Run from the repository root:

```sh
adb devices -l
gradle --no-daemon testDebugUnitTest assembleDebug
node app/src/test/js/parent-login.test.cjs
node app/src/test/js/parent-password.test.cjs
python3 -m unittest discover -s scripts/tests
python3 scripts/install.py --serial '<device-serial>' --backup-dir '<external-backup-directory>'
```

The last command only displays device properties and a plan. Verify the selected device, the external drive mount, and available space before applying:

```sh
python3 scripts/install.py --serial '<device-serial>' --backup-dir '<external-backup-directory>' --apply
```

The script backs up the existing project APKs and screen timeout, installs five local debug APKs, sets a 30-second timeout, and opens initial parent setup. Use a fresh backup directory for each run; existing snapshots are not overwritten. **APK backups do not include account sessions, chats, parent credentials, system partitions, or private app data.** They are not a full-device recovery backup.

Updating an existing installation requires its original signing key. The script stops on signature/version incompatibility without uninstalling or wiping data. Rebuild with the original key or prepare a separate migration plan. Local debug signing is for personal testing, not a shared distribution identity. `assembleRelease` is unsigned by default; configure private release signing separately.

## 2. Parent password and permissions

Only an ADB installer with the DUMP permission can open initial enrollment. The parent enters the 12-128-character password twice **on the device**. Do not share it with an agent, pass it as a shell argument, or upload it in configuration. Initial enrollment cannot replace an existing password.

Reopen enrollment if needed:

```sh
adb -s '<device-serial>' shell am start -n local.thinksmart.chatgpt/.ParentSetupActivity
```

Grant usage access to **Learning Station parent tools** in Android settings. On Android 8.1, it can also be explicitly granted with:

```sh
adb -s '<device-serial>' shell appops set local.thinksmart.chatgpt GET_USAGE_STATS allow
```

Connect the phone to the same trusted Wi-Fi and open the displayed `http://<device-ip>:8766` address. The page provides foreground usage, fallback-browser blocked domains, on-demand screen viewing, and password changes. A successful password change invalidates all parent sessions. There is no default password or recovery backdoor: preserve existing data and have the owner plan any reset or migration.

Request screen-viewing authorization from the parent page and confirm Android's prompt on the device. Permission may expire when the service stops or the device restarts. Explain this to the child first. Screenshots are not taken continuously. Do not expose this HTTP page publicly; add an encrypted channel if the local network is not sufficiently trusted.

## 3. Official app, child account, and music

- Obtain a compatible ChatGPT version through the [official download entry](https://chatgpt.com/download/) and verify its source and signature. Do not install unknown modified APKs. Diagnose service-component incompatibility before adding Google packages.
- The parent privately completes sign-in and account linking. Scripts do not read passwords or verification codes. Age, region, parent features, and availability follow the service's applicable rules.
- The parent can place the [English tutoring template](tutoring-instructions.en.txt) in suitable account/project instructions. Test new conversations, voice, and photo questions. Instructions are not a security boundary; the official app can still navigate to other pages. This template translates the original Chinese-language tutoring policy and retains its default Simplified Chinese response preference; adapt the language preference yourself if appropriate.
- Obtain a compatible ARMv7 MusicFree build from [upstream](https://github.com/maotoumao/MusicFree). The dashboard only launches it; no music, lyrics, or search plugins are bundled. Use sources you are entitled to access.
- The user grants microphone/camera permission when prompted. This project does not modify or re-sign the official app.

## 4. Manual Chinese keyboard

Install a compatible [Fcitx5 Android](https://github.com/fcitx5-android/fcitx5-android) build and Chinese pinyin components. Enable it and verify Chinese input before enabling the helper. The current integration expects `org.fcitx.fcitx5.android/.input.FcitxInputMethodService`; other IMEs require separate adaptation.

```sh
python3 scripts/keyboard.py --serial '<device-serial>' --backup-dir '<external-backup-directory>' enable
```

This saves relevant previous settings, grants WRITE_SECURE_SETTINGS, and enables the empty IME and accessibility helper while preserving other accessibility services. The helper observes the foreground package and focused-node identity, not text. It does not click chats or menus. Open ChatGPT manually, tap its input field, then the side button labeled `键盘` (Keyboard); `收起键盘` (Hide keyboard) switches back to the empty IME.

Disable the helper:

```sh
python3 scripts/keyboard.py --serial '<device-serial>' --backup-dir '<external-backup-directory>' disable
```

`disable` removes this helper's accessibility service, switches to Fcitx, and disables the empty IME. **It does not restore the complete snapshot.** If you previously used another keyboard, consult the private `keyboard-before.json` backup and restore your default IME and permissions yourself. Preserve the backup directory and use different directories for other devices.

**Use portrait orientation for ChatGPT.** Landscape keyboard display may still reopen the sidebar. No automated sidebar-click patch is installed. Retest after OS/app updates.

## 5. Home screen, kiosk policy, and restart

Select **Learning Station** in Android's default home-app settings. Open that settings page with:

```sh
adb -s '<device-serial>' shell am start -a android.settings.HOME_SETTINGS
```

The dashboard changes shortcuts, not in-app navigation permissions. For system-level kiosk operation, configure [FreeKiosk](https://github.com/RushB-fr/freekiosk) separately with an allowlist, exit password, and recovery route. Preserve components needed for normal sign-in, and test a safe exit before locking down. This is not done by the installer. Do not factory-reset a device merely to obtain Device Owner without reviewing data loss and getting separate approval.

The restart adapter targets `127.0.0.1:8767`, sends POST `/api/reboot` with a device-local 64-character hexadecimal `X-Api-Key`, and expects `success=true` plus `data.executed=true`. Use a compatible FreeKiosk version with restart permissions and check its [REST API documentation](https://github.com/RushB-fr/freekiosk/blob/main/docs/rest-api.md). **A fresh installation cannot restart through this adapter by default:** no public on-device key-enrollment wizard is provided yet. It will show an unavailable message. Do not hardcode, log, or commit the key when adapting the source. Use the physical power controls until setup is complete.

## 6. Manual acceptance checklist

Record each item as **passed on hardware**, **failed**, or **untested**. Source tests are not hardware acceptance.

- Four shortcuts, 24-hour clock, Wi-Fi disconnect/reconnect, and portrait/landscape rotation.
- In portrait, entering pages does not pop up the keyboard; manual show/hide works and sidebar behavior is acceptable.
- Separate child account: continuous voice for at least five minutes, exit and re-entry, photo input, image orientation, and follow-up discussion.
- Requests for final answers/calculations still receive guidance; record exceptions.
- Parent phone page: old/wrong-password handling, session invalidation after password changes, and official-app foreground time increasing during actual use. Missing observation time is not zero usage.
- Authorized screenshots work on demand and stop working after authorization ends. Do not claim automatic recording of official-app content.
- Actual screen-off after 30 seconds, button wake, keep-screen-on and return to timeout. Do not promise touch-to-wake when the OS does not support it.
- With USB disconnected, reboot and check independent operation of the dashboard, keyboard, and parent service. Record if screen-viewing authorization needs renewal.

## Rollback

Keep your APK backups, original signing material, and installation record. Disable the keyboard helper, restore the previous home app and timeout, and reinstall older APKs only when signing/version rules allow it. Downgrades or different signatures may prevent direct rollback; do not automatically add `-d` or uninstall. Private app-data recovery depends on prior backups. Do not wipe data to work around signing failures. Firmware changes require the device-specific recovery plan in the [recovery guide](FLASHING.en.md).

Downloads and music/lyrics plugin configuration: [resource list](RESOURCES.en.md).
