# 安装与验收

## 适用范围

源码预览版参考 Android8.1/API27、ARMv7 的 ThinkSmart View。已有可用系统优先安装组件；本指南不自动 root、刷机、清除数据、停用系统包或设置 Device Owner。机型/系统不符时先评估移植，不套用整机镜像。

需准备：自己的电脑、USB调试、ADB、JDK17、Gradle9.8.2、SDK37.2、Node.js、Python3.9+，以及自己指定的已挂载移动硬盘。本文所有 `<设备序列号>`、`<移动硬盘备份目录>`、`<设备IP>` 都需替换。不要把真实值提交到仓库。

## 1. 检查、备份、构建

```sh
adb devices -l
gradle --no-daemon testDebugUnitTest assembleDebug
node app/src/test/js/parent-login.test.cjs
node app/src/test/js/parent-password.test.cjs
python3 -m unittest discover -s scripts/tests
python3 scripts/install.py --serial '<设备序列号>' --backup-dir '<移动硬盘备份目录>'
```

最后一条仅展示属性与安装计划。确认设备确实是目标机器、备份目录在移动硬盘上、可用空间足够后，再执行：

```sh
python3 scripts/install.py --serial '<设备序列号>' --backup-dir '<移动硬盘备份目录>' --apply
```

脚本先保存系统息屏时间和已有自研组件APK，再安装5个本地 debug APK，设30秒息屏，打开首次家长设置。每次使用新的备份目录；不会覆盖已有快照。**APK备份不包含登录、聊天、家长密码、系统分区或私有应用数据**，不等于整机恢复材料。

更新已有设备需原签名。签名/版本不匹配时脚本停止，不自动卸载或清数据；使用原密钥重新构建，或单独制定迁移方案。本地 debug key 仅适合自用测试，不作为对外分发的统一签名。`assembleRelease` 默认未签名，分发前需由维护者单独配置私有签名。

## 2. 创建家长端密码与权限

首次设置界面只由有DUMP权限的ADB操作者启动，密码由家长**直接在设备上输入**两次，12–128字符。不要把密码交给 Agent，也不把它作为 shell 参数或配置文件上传。已初始化时此界面不能覆盖密码。

需要重开界面：

```sh
adb -s '<设备序列号>' shell am start -n local.thinksmart.chatgpt/.ParentSetupActivity
```

在系统设置中给 **Learning Station 家长端** 授予“使用情况访问权限”；在Android8.1上也可明确授权：

```sh
adb -s '<设备序列号>' shell appops set local.thinksmart.chatgpt GET_USAGE_STATS allow
```

家长设置完成后，手机连接同一可信Wi-Fi，用设备显示的地址打开 `http://<设备IP>:8766`。页面可看前台时长、备用网页拦截域名、按需屏幕和修改密码。修改成功后全部家长会话失效，需要新密码重新登录。忘记密码没有默认后门；先保留原数据，由设备所有者制定重设/数据迁移方案。

屏幕查看由家长页发起授权，并在设备端接受Android系统提示；授权可能在服务结束、重启后失效，需要再次确认。先向孩子说明用途。不自动持续截屏，不把页面暴露到公网。当前HTTP访问不加密；局域网信任边界不足时先配置加密通道。

## 3. 官方App、孩子账号与音乐

- ChatGPT自行从[官方入口](https://chatgpt.com/download/)获取兼容版本，核对来源和签名。不要为安装而使用来历不明的“破解版”。系统与服务组件不兼容时先诊断，不批量补装未知Google包。
- 家长本人完成孩子独立账号的登录和家长关联；密码与验证码不由脚本读取。适用年龄、地区、家长功能和可用性遵循官方当时规则。
- 将 [辅导模板](tutoring-instructions.txt) 由家长放入适用的账号/项目指令，并检查新对话、语音和拍照题的实际表现。模板不是强制安全边界；官方App仍能切换其他页面。
- MusicFree从[上游](https://github.com/maotoumao/MusicFree)自行获取适配ARMv7的版本。桌面只提供启动入口，不自带音乐内容、歌词或搜索插件；只使用自己有权访问的音源。
- 若相机、麦克风弹权限确认，由用户本人授权。项目不修改或重签名官方App。

## 4. 手动中文键盘

从 [Fcitx5 Android上游](https://github.com/fcitx5-android/fcitx5-android)获取兼容输入法和中文拼音组件，先在系统中启用并确认中文可用。当前适配组件为 `org.fcitx.fcitx5.android/.input.FcitxInputMethodService`；其他输入法需单独适配，不直接启用本助手。

```sh
python3 scripts/keyboard.py --serial '<设备序列号>' --backup-dir '<移动硬盘备份目录>' enable
```

这会保存原相关系统设置，授予本助手 WRITE_SECURE_SETTINGS、启用空IME和辅助服务，并保留其他辅助服务。辅助服务只观察前台包名与聚焦节点身份，不读取文字，不点击聊天或菜单。首次启用后手动打开ChatGPT，点输入框，再按右侧“键盘”；“收起键盘”切回不显示输入区的IME。

关闭助手：

```sh
python3 scripts/keyboard.py --serial '<设备序列号>' --backup-dir '<移动硬盘备份目录>' disable
```

`disable` 只移除本助手的辅助服务、切回Fcitx并禁用空IME；**不是完整恢复快照**。如原来使用其他输入法，请参考私有 `keyboard-before.json` 由本人恢复默认输入法和权限。保留这个备份目录，其他设备使用不同目录。

**ChatGPT建议竖屏**：横屏打开键盘后侧栏仍可能自动展开。没有安装自动点击侧栏的补丁。Android升级或App更新后须重新实测。

## 5. 桌面、封闭模式和重启

可在系统“默认主屏幕应用”中选择 **Learning Station**。例如打开选择界面：

```sh
adb -s '<设备序列号>' shell am start -a android.settings.HOME_SETTINGS
```

桌面只改变入口，不能阻止从应用内部打开其他界面。需要系统级封闭模式时，另行配置 [FreeKiosk](https://github.com/RushB-fr/freekiosk) 的白名单/退出密码/恢复路径，保留正常登录所需组件；先测试能安全退出再锁定。该配置没有包含在本安装脚本中。切勿默认恢复出厂来申请Device Owner；先评估数据损失并单独确认。

重启按钮是现有 FreeKiosk 适配器：目标 `127.0.0.1:8767`，POST `/api/reboot`，`X-Api-Key` 为本机独立64位十六进制密钥，预期响应 `success=true` 和 `data.executed=true`。需匹配支持此API、已具备重启权限的FreeKiosk版本；以[上游REST API说明](https://github.com/RushB-fr/freekiosk/blob/main/docs/rest-api.md)核对配置。该组件当前没有公开的设备端密钥设置向导，因此**新安装默认不能重启**，仅会显示不可用提示；源码适配时不要把密钥写死、印到日志或提交Git。未完成这一步时用系统电源键重启。

## 6. 人工验收清单

分别记录“实测通过”“失败”“未测”，不要把源码测试当硬件通过：

- 桌面4入口、24小时时间、Wi-Fi断开/重连、横竖旋转。
- 竖屏中文键盘：进入新页面不弹出，点击输入框后可手动展开/收起，侧栏状态符合预期。
- 孩子独立账号：连续语音至少5分钟，退出再进入；拍题、照片方向与继续讨论。
- 要求直接给答案/计算结果时，模型是否继续分步引导；记录例外。
- 手机家长页：旧/错密码、新密码修改后的会话失效、官方App前台时间随实际操作增长；缺失时间不当作零。
- 设备端授权后按需截图、结束授权后不能继续截取；官方App内容不声称被自动记录。
- 30秒真息屏、按键唤醒、不息屏开关及恢复；当前系统不支持触摸唤醒时不要承诺支持。
- 拔掉USB、正常重启后桌面、键盘、家长服务可独立运行。截图权限如需重授应如实记录。

## 回退

先保留自己备份的APK、原签名及安装记录。停用键盘助手，恢复原默认桌面和息屏时间；有原签名且版本允许时可安装回原APK。降版本或不同签名不保证直接回退，不自动加 `-d` 或卸载。私人应用数据的恢复能力取决于事前备份；不要以清数据修复签名问题。若涉及系统刷写，按逐台制定的恢复执行单处理，见[恢复指南](FLASHING.md)。
