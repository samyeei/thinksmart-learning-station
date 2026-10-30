# 第三方与来源

本仓库 MIT 许可只覆盖本仓库作者有权许可的自研源码；依赖与外部应用不因此改为 MIT。

| 名称 | 使用方式与来源 | 许可/权利边界 |
| --- | --- | --- |
| GeckoView | Gradle依赖 `org.mozilla.geckoview:geckoview`；[Mozilla文档](https://mozilla.github.io/geckoview/)、[源码](https://searchfox.org/mozilla-central/source/mobile/android/geckoview) | Mozilla Public License 2.0及上游各文件许可；本仓库不含引擎源码修改。再分发含引擎的APK时应附上其许可、上游版权和对应源码获取信息。 |
| JUnit4 | 单元测试依赖；[上游](https://github.com/junit-team/junit4) | Eclipse Public License 1.0；测试使用，不随应用运行。 |
| JSON-java | JVM测试依赖；[上游](https://github.com/stleary/JSON-java) | 核对锁定版本的上游LICENSE；Android运行时使用系统JSON类。 |
| Android SDK / Gradle / AGP | 外部构建工具与系统API | 遵循各自许可；不把整个工具链重新授权为MIT。 |
| ChatGPT | 用户自行安装的官方App；[官方下载](https://chatgpt.com/download/) | OpenAI的应用、服务、商标及条款；本项目不修改、重签名或分发官方App。 |
| Fcitx5 for Android | 外部中文输入法；[上游](https://github.com/fcitx5-android/fcitx5-android) | 不包含其源码/APK；遵循其版本与组件的上游许可。 |
| MusicFree | 外部音乐播放器入口；[上游](https://github.com/maotoumao/MusicFree) | AGPL-3.0；本仓库不分发播放器、音乐内容或插件。音乐可用性及授权由所用服务决定。 |
| FreeKiosk | 外部封闭模式/本地重启API；[上游](https://github.com/RushB-fr/freekiosk) | 本仓库不含FreeKiosk代码或APK；使用者核对版本的许可与API说明。 |
| 固件与恢复工具 | 仅在文档链接同机型说明、edl和Magisk上游 | 未分发ROM、loader、Google组件、修补镜像或私人分区备份。 |

公开版桌面背景和图标由本仓库 Canvas 代码绘制，无家庭设备的位图素材。每日句子为短篇古典语句/通行格言；不包含现代作品的大段摘录。第三方学习图谱及早期未验收浏览器/限制层实验不在发布范围内。
