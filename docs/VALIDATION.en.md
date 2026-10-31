# Validation status

[English](VALIDATION.en.md) | [Chinese](VALIDATION.md) | [Project home](../README.en.md)

Recorded on **2026-10-31**, for the public alpha source preview. This is a translation of the recorded validation, not a claim that hardware tests were rerun for the English documentation.

## Public source candidate: local verification

- JDK 17, Gradle 9.8.2, AGP 9.4.1, SDK 37.2: `testDebugUnitTest assembleDebug` succeeded. All five modules generated debug APKs without the author's private signing configuration.
- **65 JVM tests:** parent/web service 53, dashboard 5, manual keyboard 7; zero failures/errors. Coverage includes isolated HTTP authorization boundaries, session invalidation after password changes, concurrent password rotation, persistence-failure recovery, and rejection of password replacement through initial enrollment.
- Parent sign-in JavaScript: five scenarios passed. Password-change JavaScript: five scenarios passed.
- Python installer path handling: three cases passed (absent packages, existing split APKs, and stopping on query errors). Both scripts' help commands ran successfully.
- An independent read-only source review identified first-install `pm path` handling, which was corrected. Keyboard removal is explicitly called `disable`, not a complete snapshot rollback.
- Source was exported by an allowlist. The learning graph, family addresses/identifiers, real account/project links, signing materials, private evidence, backups, and binary apps/firmware were excluded. Test credentials, project IDs, and RFC1918 addresses are synthetic fixtures.

Build warnings concerned deprecated Android APIs, Gradle 10 compatibility, and unstripped Gecko native symbols. They did not prevent this debug build. This does not establish production readiness or compatibility with every system.

## Reference family device

The parent previously reported working continuous voice, photo input, guided tutoring, and the manual keyboard in portrait, using the official app and a separate child account on the Android 8.1 ARMv7 reference unit. The parent confirmed that the password-change section appeared on the phone page; this does not establish that an actual password change was completed.

Continuous voice in the older Gecko/Firefox web environment crashed. The web entry remains a fallback. Automatic sidebar expansion in landscape in the official app remains unresolved.

## Not yet validated

- Complete installation on a second, clean device; touch interaction with initial password enrollment; default-home selection; and all components resuming after restart.
- Public dashboard artwork on hardware and a full voice/photo regression after installing the public build.
- Accuracy of the new official-app usage mapping across devices.
- Pairing the restart adapter with other FreeKiosk versions. No public first-key setup wizard is currently provided.
- A universal 64-bit ROM or flashing/root/recovery success across different builds and devices.

Preparing this public release did not modify the family's device. No pre-signed APKs or full firmware images are provided. Existing-device results are not presented as proof that a fresh installation passed.
