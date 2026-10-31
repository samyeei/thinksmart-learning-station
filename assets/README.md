# 项目展示图片

[Chinese](README.md) | [English](README.en.md)

## 来源与真实性

| 文件 | 类型 | 用途 |
| --- | --- | --- |
| `generated/project-hero.jpg` | 内置 GPT 图片生成工具创作 | 项目封面；参照实拍竖屏机身和语音界面合成，不是原始照片或公开版桌面截图。 |
| `generated/guided-learning.jpg` | 内置 GPT 图片生成工具创作 | 表达提问、探索、解释的辅导理念；不作为功能验证证据。 |
| `generated/home-connection.jpg` | 内置 GPT 图片生成工具创作 | 表达手机与学习终端在家中Wi-Fi连接的关系；不声称传输加密或公网访问。 |
| `photos/voice-mode.jpg` | 项目作者提供的参考设备实拍 | 官方 ChatGPT 语音界面和手动键盘按钮。照片本身不能证明一次长时间语音测试的结果。 |
| `photos/music-player.jpg` | 项目作者提供的参考设备实拍 | MusicFree播放器界面；不提供音乐文件或音源插件。 |

实拍仅做格式转换与缩小，未用 AI 修改界面；原始 HEIC 和其位置/拍摄设备元数据未上传。发布 JPEG 经重新编码检查不含 GPS、Exif、IPTC、TIFF 信息块。含个人昵称或第三方学习图谱的其他照片未发布。

生成图统一使用深蓝、青色与柔和暖光，单独标注为概念图；机器按实物的竖屏比例、边框、摄像头与下方音箱绘制，屏幕参照实拍的语音/音乐界面。AI合成可能有局部文字、比例或细节差异，不能替代实拍与功能验收；手机端画面仅为示意。图片中的第三方应用界面、产品外观与标识仍属于各自权利人；自研源码的MIT许可不代表第三方内容被重新授权。

## 生成提示词

使用内置 `image_gen`，未调用外部付费API或CLI。以下记录生成时的核心提示词与约束，便于维护者沿用视觉风格：

### 项目封面（按实物修订版）

> Use the provided device photograph as authoritative hardware AND screen reference. Preserve navy/cyan background and ThinkSmart / Learning Station typography. Replace invented device/dashboard with the actual tall portrait ThinkSmart View: thick matte black frame, top-left camera, actual portrait screen ratio, broad lower fabric speaker grille and vertical ThinkSmart marking. Match the photographed white ChatGPT voice screen, pale blue orb, right-side keyboard button, menu controls, bottom input and close button. No futuristic stand, thin bezels, invented dashboard, family name or graph app. Promotional composition, not an unedited photograph.

### 引导式学习（按实物修订版）

> Keep Make room for thinking. and Ask / Explore / Explain, warm stepping stones and paper. Replace the wide display with the exact tall portrait black device in the reference. Fit its real shape without compression. Match camera, thick bezel, fabric grille and actual white ChatGPT voice UI with blue orb and keyboard button. No invented waveform/question-mark screen; learning motifs stay outside the device. No personal content or graph app.

### 家庭局域网（按实物修订版）

> Keep Connected at home., Local parent tools. Independent learning., Trusted home Wi-Fi, phone concept and house outline. Replace generic display with the real tall portrait ThinkSmart View, matching frame, camera and lower fabric grille. Match actual MusicFree interface: search bar, four square shortcuts, playlist and bottom player bar. Do not invent waveform or dashboard screens. Phone stays a generic concept, not a real parent dashboard. No encryption or Internet-access claims.
