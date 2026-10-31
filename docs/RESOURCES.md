# 下载、Root 与音乐插件资源

[Chinese](RESOURCES.md) | [English](RESOURCES.en.md) | [项目首页](../README.md)

## Root 教程目前提供到哪一步？

本仓库有[刷机、备份与恢复指南](FLASHING.md)，涵盖只读识别、匹配镜像、移动硬盘备份、写入前确认及恢复路径；也提供下面的作者资料。**目前没有针对每个系统构建验证过、可以照抄执行的完整 ThinkSmart View root 命令教程，也没有一键 root 脚本。** 不应把参考链接写成“本仓库已提供完整实机 root 教程”。

| 用途 | 作者资料 |
| --- | --- |
| 同机型 Android / Lineage 15.1 安装参考 | [pgale/lineage_15.1_Installation_TSV](https://github.com/pgale/lineage_15.1_Installation_TSV) |
| 高通 EDL、GPT 与分区读取工具 | [bkerler/edl](https://github.com/bkerler/edl) |
| Magisk 下载 | [官方 Releases](https://github.com/topjohnwu/Magisk/releases) |
| Magisk 原始镜像识别、修补与安装 | [作者安装文档](https://topjohnwu.github.io/Magisk/install.html) |

建议执行顺序：

1. 明确型号、构建、API、ABI、引导状态及分区布局。
2. 匹配恢复固件、loader、原始引导镜像，并记录来源和 SHA-256。
3. 将可读取的原始分区及清单备份到指定移动硬盘并核验；列出无法备份的部分。
4. 根据真实布局判断需要修补哪个镜像，使用与该设备匹配的原始文件；不复用他人的已修补镜像。
5. 先写出目标分区、数据影响和恢复步骤，由所有者明确确认后，才进入实际修补/写入操作。
6. 重启后核对 root 状态与权限，再检查原账号、桌面、连续语音、拍照、键盘和家长服务。

Magisk 通用文档有引导解锁前提，不能把它的 fastboot 示例直接套进本型号的 EDL 路线。本项目不要求每位使用者为了安装应用先 root；能正常安装的系统优先保留。

## Codex：用在安装电脑上

按 [OpenAI 官方使用说明](https://developers.openai.com/codex/quickstart)安装/打开适合自己电脑的入口；终端用户可看 [Codex CLI 官方文档](https://learn.chatgpt.com/docs/cli)。它用于协助检查设备、构建和安装项目；孩子日常使用的是官方 ChatGPT App，不需要把 Codex 安装进 ThinkSmart。

打开本仓库后，将首页的[一句话安装任务](../README.md)复制给自己的 Agent，指定目标设备和移动硬盘。账号认证和密码由本人在设备上输入。

## 音乐软件与插件

| 用途 | 链接 |
| --- | --- |
| MusicFree 源码 | [作者仓库](https://github.com/maotoumao/MusicFree) |
| Android 安装包 | [作者 Releases](https://github.com/maotoumao/MusicFree/releases)，按系统要求选择，参考设备需要 ARMv7 |
| 插件安装图文步骤 | [作者安装指南](https://musicfree.catcat.work/usage/mobile/install-plugin.html) |
| 插件源码与目录 | [MusicFreePlugins](https://github.com/maotoumao/MusicFreePlugins) |
| 可查看的插件目录清单 | [plugins.json](https://github.com/maotoumao/MusicFreePlugins/blob/master/plugins.json) |
| 从网络导入的原始目录地址 | [GitHub Raw JSON](https://raw.githubusercontent.com/maotoumao/MusicFreePlugins/master/plugins.json) |
| 作者 README 提供的目录镜像 | [GitCode JSON](https://raw.gitcode.com/maotoumao/MusicFreePlugins/raw/master/plugins.json) |
| 歌词网插件源码 | [geciwang/index.js](https://github.com/maotoumao/MusicFreePlugins/blob/master/dist/geciwang/index.js) |
| 歌词千寻插件源码 | [geciqianxun/index.js](https://github.com/maotoumao/MusicFreePlugins/blob/master/dist/geciqianxun/index.js) |

MusicFree 本身是播放器，不自带音源。其作者提供的目录包含多种服务和示例插件，**不等于都适合孩子，也不保证每首歌都可搜索、免费完整播放或获取歌词**。按需挑选音乐与歌词插件，不默认整包导入；本项目没有把第三方插件变成系统级内容过滤器。

安装方式：侧边栏 → 插件设置 → “+” → 从网络安装插件；粘贴可信的原始 `.json` 或 `.js` 地址，而不是 GitHub 的 `blob` 网页链接。也可在核对源码后下载为本地文件导入。直接导入某个歌词插件时，可从上方源码页点击 Raw 获取原始地址。插件导入后分别测试歌曲搜索、播放、歌词搜索/关联；能力取决于插件及其服务。

目录与源码链接用于自行检查和配置，仓库不复制插件代码或家庭配置，也不提供绕过付费或访问限制的修改版。链接清单在2026-10-31核对；不代表每个插件的实时服务均已在新设备实测。
