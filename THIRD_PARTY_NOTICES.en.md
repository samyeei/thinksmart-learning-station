# Third-party notices and sources

[English](THIRD_PARTY_NOTICES.en.md) | [Chinese](THIRD_PARTY_NOTICES.md) | [Project home](README.en.md)

The repository's MIT license covers original code the authors are entitled to license. It does not relicense dependencies or external applications.

| Project | Use and source | License or ownership boundary |
| --- | --- | --- |
| GeckoView | Gradle dependency `org.mozilla.geckoview:geckoview`; [Mozilla documentation](https://mozilla.github.io/geckoview/) and [source](https://searchfox.org/mozilla-central/source/mobile/android/geckoview) | MPL 2.0 and upstream file-specific licenses. No engine source modifications are included. When redistributing APKs containing the engine, include required licenses, notices, and corresponding source access information. |
| JUnit 4 | Test dependency; [upstream](https://github.com/junit-team/junit4) | Eclipse Public License 1.0; used for tests, not app runtime. |
| JSON-java | JVM test dependency; [upstream](https://github.com/stleary/JSON-java) | Check the pinned version's upstream LICENSE. Android runtime uses system JSON classes. |
| Android SDK / Gradle / AGP | External build tools and system APIs | Their own licenses apply; the toolchain is not relicensed under MIT. |
| ChatGPT | User-installed official app; [download](https://chatgpt.com/download/) | OpenAI's app, service, trademarks, and terms. No modification, re-signing, or distribution of the official app. |
| Fcitx5 for Android | External Chinese IME; [upstream](https://github.com/fcitx5-android/fcitx5-android) | No source/APK bundled; follow the licenses of the selected release and its components. |
| MusicFree | External music player shortcut; [upstream](https://github.com/maotoumao/MusicFree) | AGPL-3.0. No player binaries, music, or plugins bundled. Availability and authorization depend on the source service. |
| FreeKiosk | External kiosk policy/local restart API; [upstream](https://github.com/RushB-fr/freekiosk) | No code or APK bundled. Check the chosen version's license and API documentation. |
| Firmware and recovery tools | Links to device-specific instructions, edl, and Magisk | No ROMs, loaders, Google components, patched images, or private partition backups are distributed. |

The public dashboard's background and icons are drawn in Canvas code, without the family device's bitmap assets. Daily quotations are short classical/public-domain texts and common sayings, not extensive extracts from modern works. The third-party learning graph and earlier unvalidated browser/guard experiments are excluded.

README image sources and generation methods are documented in [assets/README.en.md](assets/README.en.md). AI concept images and real device photographs are clearly distinguished. They contain no third-party learning graph or private conversations. Showing third-party app interfaces or product marks does not change their ownership.
