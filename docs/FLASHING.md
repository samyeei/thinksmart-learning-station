# ThinkSmart View 刷机、备份与恢复指南

本方案公开操作方法，不公开家庭备份、Google/ChatGPT 账号数据或一份可用于所有机器的固件。目标是让操作者能判断自己的设备是否匹配，再执行可复核的步骤。

## 1. 先分清安装组件和刷机

已经有可用 Android 的机器先走应用安装路线，不为安装桌面而重刷系统。仍运行出厂会议系统、无法安装所需应用时，才评估 Android 固件路线。Root 与刷完整系统是不同操作；本机的成功记录不能当作其他机器必然兼容的证明。

本项目参考设备是 ThinkSmart View，Android8.1/API27、32位 ARM 应用环境。SoC 支持64位不代表当前系统能运行 arm64 应用，也不证明存在可用的64位 ROM。

## 2. 只读识别

启用 USB 调试，在设备上确认自己的电脑。先运行 `adb devices -l`，从结果中选定一台设备，后续每条命令都显式指定它。下列 `DEVICE_SERIAL` 必须由操作者填写，不使用示例机器的序列号：

```sh
adb devices -l
DEVICE_SERIAL='替换为目标设备序列号'
adb -s "$DEVICE_SERIAL" shell getprop ro.product.model
adb -s "$DEVICE_SERIAL" shell getprop ro.product.device
adb -s "$DEVICE_SERIAL" shell getprop ro.build.fingerprint
adb -s "$DEVICE_SERIAL" shell getprop ro.build.version.release
adb -s "$DEVICE_SERIAL" shell getprop ro.build.version.sdk
adb -s "$DEVICE_SERIAL" shell getprop ro.product.cpu.abilist
adb -s "$DEVICE_SERIAL" shell getprop ro.boot.flash.locked
adb -s "$DEVICE_SERIAL" shell getprop ro.boot.verifiedbootstate
```

这些命令不授权重启进 EDL、解锁、擦除或写入。设备不匹配、无法确定构建版本或 ABI 时，先停止写入准备并给出检查结果。

## 3. 匹配材料与建立恢复路径

有同机型作者维护的 [Lineage15.1 安装说明](https://github.com/pgale/lineage_15.1_Installation_TSV)，可用于识别其固件路线、依赖和适用条件；不要因此把其中镜像套用于不同硬件或系统构建。高通只读识别、GPT 与分区读取可参考 [bkerler/edl 作者文档](https://github.com/bkerler/edl)。使用具体工具版本的帮助确认参数，并核对 loader、存储类型、分区表及镜像大小。

先记录 ROM、原始引导镜像、loader、分区描述文件与相关工具的来源、版本和 SHA‑256。校验包内包含恢复所需文件；“压缩包能下载”与“这台机器已验证可恢复”分开记录。

由操作者指定移动硬盘上的备份目录；确认硬盘确实挂载、容量足够且目录可写。备份应包含设备属性、分区表、可读的原始分区和恢复所需资料，另保存分区名、大小、偏移及哈希清单。配置导出或 APK 备份不等于整机备份；只有整机读取实际完成并核验，才能标记“整机备份成功”。无法读取的分区应明确列出，不能以空文件当成功。备份可能包含私人数据，不上传 GitHub。

## 4. 写入前列明具体操作

准备一份针对当前机器的执行单：目标镜像与 SHA‑256、目标分区、当前分区读取核验、是否清除数据、恢复文件和回退步骤。请求一次针对该执行单的明确确认，再执行。不要从别人的记录复制扇区地址或原始 XML，也不要把所有分区都覆盖作为默认做法。

如果采用 Magisk 路线，阅读其[作者安装说明](https://topjohnwu.github.io/Magisk/install.html)，先识别设备布局和所需原始镜像，再决定适用步骤。不要跨设备复用已经修补的引导镜像；通用文档中的 fastboot 流程也不能直接等同于本型号的 EDL 流程。没有经过验证的材料、恢复路径或确认时，不写入。

## 5. 安装与验收

刷机后先验证系统能正常启动、重启和连接 Wi‑Fi，再安装应用；官方 App 的密码和验证码由家长本人输入，Agent 不收集或打印。学习图谱不在此安装范围内。

按实际行为验收：孩子独立账号与家长关联、分步辅导、中文键盘手动展开/收起、竖屏侧栏、连续语音、拍照与照片方向、音乐、息屏、家长登录及修改密码、重启和脱离电脑后的运行。分别标注“本机实测”“家长报告”“尚未验证”，不把安装成功写成全功能通过。

失败时先保留最小诊断信息与已匹配的备份，按执行单恢复；避免连续更换不匹配固件、清除账号数据或修改当前可用的语音和相机环境来碰运气。
