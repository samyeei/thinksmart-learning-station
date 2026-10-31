# Installation and maintenance instructions for agents

[English](AGENTS.en.md) | [Chinese](AGENTS.md)

Read README.en.md, docs/INSTALL.en.md, docs/FLASHING.en.md, and docs/VALIDATION.en.md first.

- Specify the owner's selected serial explicitly for every ADB action. Never infer the target environment from the author's configuration.
- Start with read-only inspection. Report model, API, ABI, installed signing identity, recovery prerequisites, and missing items. Save backups on the owner's chosen external drive after checking its mount and free space.
- The installer defaults to plan mode. Simulation, unit tests, or an APK installation do not establish hardware acceptance.
- Do not automatically flash, root, unlock, wipe data, change signing identities, request Device Owner, or remove system packages. First present device-matched materials, SHA-256 hashes, partitions, data impact, and a recovery plan; obtain explicit approval before a separate execution step.
- Do not request passwords, verification codes, or chat content. Let the owner privately enter initial parent credentials and authenticate on the device. Do not put secrets in command arguments, logs, Git, screenshots, or reports.
- Do not automatically click official-app chats, menus, or sidebars, or send messages. Preserve voice and photo behavior. Use the validated portrait layout rather than silently adding automatic clicks.
- Do not install or export the third-party learning graph. Do not export family IPs, account/project IDs, device identifiers, signing material, credential hashes, backups, or private camera/chat content.
- Use a separate child account. Tutoring instructions cannot guarantee that the model never provides answers; the official app is not forcibly locked to a project. Explain these limitations.
- The child must know about parental supervision. Keep viewing within the authorized scope and on demand; do not run covert or continuous monitoring.
- At completion, distinguish source tests, installation results, user-verified hardware behavior, and untested items. On failure, preserve minimal diagnostics instead of repeatedly requesting credentials or wiping data speculatively.
