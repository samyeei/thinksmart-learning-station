# ThinkSmart View flashing, backup, and recovery

[English](FLASHING.en.md) | [Chinese](FLASHING.md) | [Project home](../README.en.md)

This project documents a method, not a universal firmware image. Family backups and Google/ChatGPT account data are not distributed. Determine whether your own device matches the requirements before following a reviewable procedure.

## 1. App installation is different from flashing

If Android already works, install the app components first; do not reflash just to add a dashboard. Consider a firmware route only when the factory conferencing system prevents the required installation. Rooting and replacing the whole OS are separate operations. Success on the reference unit does not prove another unit is compatible.

The reference environment is ThinkSmart View with Android 8.1 / API 27 and 32-bit ARM apps. A 64-bit-capable SoC does not mean the installed OS runs arm64 apps or that a compatible 64-bit ROM exists.

## 2. Read-only identification

Enable USB debugging and authorize your computer on the device. Run `adb devices -l`, select one target, and specify it explicitly in subsequent commands. Replace the placeholder yourself:

```sh
adb devices -l
DEVICE_SERIAL='replace-with-your-target-serial'
adb -s "$DEVICE_SERIAL" shell getprop ro.product.model
adb -s "$DEVICE_SERIAL" shell getprop ro.product.device
adb -s "$DEVICE_SERIAL" shell getprop ro.build.fingerprint
adb -s "$DEVICE_SERIAL" shell getprop ro.build.version.release
adb -s "$DEVICE_SERIAL" shell getprop ro.build.version.sdk
adb -s "$DEVICE_SERIAL" shell getprop ro.product.cpu.abilist
adb -s "$DEVICE_SERIAL" shell getprop ro.boot.flash.locked
adb -s "$DEVICE_SERIAL" shell getprop ro.boot.verifiedbootstate
```

These checks do not authorize entering EDL, unlocking, erasing, or writing. If hardware/build/ABI matching is uncertain, stop write preparation and report what was found.

## 3. Match materials and establish recovery

The device-specific [Lineage 15.1 installation guide](https://github.com/pgale/lineage_15.1_Installation_TSV) helps identify a firmware route, dependencies, and conditions. Do not transplant its images to different hardware or builds. For Qualcomm identification, GPT, and partition reads, consult the [bkerler/edl author documentation](https://github.com/bkerler/edl). Check the actual tool version's help and verify the loader, storage type, partition layout, and image sizes.

Record the sources, versions, and SHA-256 hashes of the ROM, original boot image, loader, partition descriptors, and tools. Verify that recovery files are actually present. A downloadable archive is not proof that recovery on this device has been validated.

Have the owner choose a directory on an external drive. Verify the mount, free space, and write access. Save device properties, partition tables, readable raw partitions, recovery materials, and a manifest of partition names, sizes, offsets, and hashes. Configuration exports or APK copies are not full-device backups. Mark a full backup successful only after actual reads and verification; list unreadable partitions explicitly and do not count empty files as successful reads. Backups may contain private data: never publish them on GitHub.

## 4. Review a specific write plan

Before writing, prepare a plan for this device: target images and SHA-256 hashes, target partitions, verification of existing partition reads, data-wipe impact, recovery files, and rollback steps. Obtain explicit approval for that plan before executing it. Do not copy raw sector offsets or XML from another owner's notes, and do not overwrite every partition by default.

For a Magisk route, read the [author's installation instructions](https://topjohnwu.github.io/Magisk/install.html), identify the layout and required original image, and then choose applicable steps. Never reuse another device's patched boot image. Generic fastboot instructions are not interchangeable with this model's EDL procedure. Without verified materials, recovery, and approval, do not write.

## 5. Install and validate

After flashing, verify normal boot, restart, and Wi-Fi before installing apps. The parent enters official-app passwords and verification codes privately; agents must not collect or print them. The learning graph is outside this installation scope.

Test the separate child account and parent linking, guided tutoring, manual keyboard show/hide, portrait sidebar behavior, continuous voice, photo input/orientation, music, timeout, parent sign-in/password change, restart, and operation without a computer. Distinguish **tested on this hardware**, **parent-reported**, and **untested**. Successful installation is not complete feature acceptance.

If something fails, preserve minimal diagnostics and matched backups, then follow the recovery plan. Avoid trying unrelated firmware, clearing accounts, or changing working voice/camera components without evidence.

For root-guide scope and upstream references, see [Resources](RESOURCES.en.md).
