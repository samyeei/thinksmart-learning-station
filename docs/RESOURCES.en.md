# Downloads, root references, and music plugins

[English](RESOURCES.en.md) | [Chinese](RESOURCES.md) | [Project home](../README.en.md)

## How complete is the root guide?

The [flashing, backup, and recovery guide](FLASHING.en.md) covers read-only identification, matched images, external-drive backups, write approval, and recovery planning. **There is no complete, copy-and-run ThinkSmart View root command sequence validated for every build, and no one-click root script.** Upstream references must not be presented as a fully validated root tutorial provided by this repository.

| Purpose | Author's resource |
| --- | --- |
| Device-specific Android / Lineage 15.1 reference | [pgale/lineage_15.1_Installation_TSV](https://github.com/pgale/lineage_15.1_Installation_TSV) |
| Qualcomm EDL, GPT, and partition reading | [bkerler/edl](https://github.com/bkerler/edl) |
| Magisk downloads | [Official releases](https://github.com/topjohnwu/Magisk/releases) |
| Original-image identification, patching, and installation | [Magisk installation documentation](https://topjohnwu.github.io/Magisk/install.html) |

Recommended sequence:

1. Identify the model, build, API, ABI, boot state, and partition layout.
2. Match recovery firmware, loader, and original boot materials; record sources and SHA-256 hashes.
3. Read and verify available raw partitions and a manifest onto the selected external drive. List unreadable partitions explicitly.
4. Determine the image to patch from the actual layout. Use that device's matching original, never someone else's patched image.
5. Document target partitions, data impact, and recovery steps. Obtain the owner's explicit approval before patching/writing.
6. After restart, verify root permissions and existing accounts, dashboard, continuous voice, photos, keyboard, and parent service.

Generic Magisk instructions assume bootloader unlocking; their fastboot examples must not be substituted for this model's EDL route. Root is not an automatic prerequisite for installing these apps. Preserve a working system whenever possible.

## Codex runs on the installation computer

Follow [OpenAI's official setup documentation](https://developers.openai.com/codex/quickstart), or the [official Codex CLI guide](https://learn.chatgpt.com/docs/cli) for terminal use. Codex assists with device inspection, building, and installation. The child uses the official ChatGPT app; Codex does not need to be installed on the ThinkSmart.

Open this repository and give your agent the [copy-and-share installation prompt](../README.en.md), specifying the target device and external drive. Enter account credentials privately on the device yourself.

## MusicFree and plugins

| Purpose | Link |
| --- | --- |
| MusicFree source | [Author's repository](https://github.com/maotoumao/MusicFree) |
| Android downloads | [Author's releases](https://github.com/maotoumao/MusicFree/releases); check OS and ABI requirements, ARMv7 for the reference device |
| Illustrated installation steps | [Author's plugin guide](https://musicfree.catcat.work/usage/mobile/install-plugin.html) |
| Plugin source repository | [MusicFreePlugins](https://github.com/maotoumao/MusicFreePlugins) |
| Readable catalog | [plugins.json](https://github.com/maotoumao/MusicFreePlugins/blob/master/plugins.json) |
| Raw catalog for network import | [GitHub Raw JSON](https://raw.githubusercontent.com/maotoumao/MusicFreePlugins/master/plugins.json) |
| Catalog mirror linked by the author's README | [GitCode JSON](https://raw.gitcode.com/maotoumao/MusicFreePlugins/raw/master/plugins.json) |
| Geciwang lyrics source | [geciwang/index.js](https://github.com/maotoumao/MusicFreePlugins/blob/master/dist/geciwang/index.js) |
| Geciqianxun lyrics source | [geciqianxun/index.js](https://github.com/maotoumao/MusicFreePlugins/blob/master/dist/geciqianxun/index.js) |

MusicFree is a player without built-in audio sources. The author's catalog includes various services and examples: **it is not a child-approved list, nor a guarantee of searchable songs, free full playback, or available lyrics.** Select only needed music/lyrics plugins rather than importing the entire catalog by default. This project does not turn external plugins into system-wide content filters.

To import: side menu > plugin settings > '+' > install from network. Paste a trusted raw `.json` or `.js` URL, not a GitHub `blob` web page. Alternatively, review and download a local plugin file. For an individual lyrics plugin, use Raw on the linked source page to obtain its import URL. Test search, playback, and lyrics search/association separately; support depends on each plugin and service.

These are references for owner-managed configuration, not bundled plugin code or family settings. No modified plugins for bypassing payment/access restrictions are provided. Links were checked on 2026-10-31; this is not a claim that every plugin's live service was tested on a fresh device.
