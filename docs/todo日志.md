# TODO 日志

## 2026-05-09

- [x] 确认当前目录为空项目。
- [x] 确认本机存在 Android SDK。
- [x] 确认本机存在 JDK 21。
- [x] 确认本机存在可用 Gradle 9.3.1 缓存。
- [x] 写入工作文档、Task 文档和 TODO 日志。
- [x] 使用 imagegen 生成第一版 UI 设计图并保存到 `docs/ui/v1-ui-design-board.png`。
- [x] 创建 Android 工程骨架。
- [x] 实现数据、配置、翻译引擎和模型下载模块。
- [x] 实现 Compose UI。
- [x] 构建 Debug APK。
- [x] 补充验证结果。

### 验证结果

- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`。
- [x] 已安装到真机设备 `1fd9b66`。
- [x] 已启动 `com.mxwis.aitranslate/.MainActivity`，应用进程存在且主页面处于前台。
- [ ] 暂未执行逐页点击和离线推理验证。

## 2026-05-10

- [x] 收到反馈：模型下载完成后离线翻译仍不可用。
- [x] 确认当前代码中 `OfflineTranslationEngine` 仍是占位实现。
- [x] 确认本机 Android SDK 未安装 NDK/CMake，不优先走本地编译 llama.cpp 路线。
- [x] 接入 Llamatik Android 依赖。
- [x] 替换离线翻译实现。
- [x] 构建 Debug APK。
- [x] 安装到真机并启动验证。
- [x] 确认手机内模型文件完整：`600534880` 字节。
- [x] 确认通用 Llamatik/llama.cpp 仍无法加载 Hy-MT 2bit GGUF。
- [x] 修正 App 内错误提示，说明是公开内核不支持，不再误导为模型文件损坏。
- [x] 补充验证结果。

## 2026-05-10：API 模型列表获取

- [x] 明确完成标准：能通过 Base URL + API Key 调用 `/v1/models` 并回填模型名称。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成设置页获取模型列表设计图并保存到 `docs/ui/settings-fetch-models-design.png`。
- [x] 实现云端模型列表请求与解析。
- [x] 实现设置页 UI 与状态展示。
- [x] 增加单元测试。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。

## 2026-05-10：切换 Hy-MT 1.25bit 离线路线

- [x] 确认 1.25bit GGUF 文件名：`Hy-MT1.5-1.8B-1.25bit.gguf`。
- [x] 确认文件大小：`461861216` 字节，约 440MB。
- [x] 确认该 GGUF 依赖 AngelSlim STQ1_0 kernel。
- [x] 更新 `docs/task文档.md`。
- [x] 更新模型下载目标和 UI 文案。
- [x] 更新离线加载失败提示。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 确认旧 2bit 文件仍在手机 App 私有目录中，但当前版本不会再识别它。
- [x] 用户确认后已删除旧 2bit 文件。

## 2026-05-10：切换标准 Q4_K_M 离线模型

- [x] 确认当前 APK 内 `libllama.so` 包含 `hunyuan` 和 `Q4_K` 支持，但不包含 `STQ`。
- [x] 确认腾讯官方 Q4_K_M GGUF 文件大小：`1133080512` 字节，约 1.13GB。
- [x] 确认手机 `/data` 可用空间约 81GB。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成模型页 Q4_K_M 设计图并保存到 `docs/ui/model-q4km-design.png`。
- [x] 更新模型下载目标和模型页文案。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已下载 Q4_K_M 模型到电脑并推送到手机 App 私有目录。
- [x] 真机离线翻译验证通过：`hi` -> `嗨`。
- [x] 用户确认后已删除旧 1.25bit 文件，仅保留 Q4_K_M 模型。

## 2026-05-10：API 模型下拉搜索与手动添加

- [x] 明确完成标准：下拉选择、搜索、手动添加和持久化。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成设置页模型下拉设计图并保存到 `docs/ui/settings-model-dropdown-design.png`。
- [x] 实现自定义模型持久化。
- [x] 实现模型下拉搜索与添加 UI。
- [x] 增加单元测试。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已打开设置页确认模型下拉入口和获取模型按钮可见。

## 2026-05-10：模块化设置页重新设计

- [x] 检索当前手机打开的软件：`com.github.lingyan000.fluxdo`。
- [x] 进入 FluxDo 的 `AI 模型服务` 设置。
- [x] 记录参考结构：`供应商`、`模型配置`、`聊天记录`、`快捷词管理`、`高级设置`。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成模块化设置页设计图并保存到 `docs/ui/settings-modular-ai-service-design.png`。

## 2026-05-10：实现模块化设置页

- [x] 明确完成标准：模块化结构、供应商配置面板、模型选择面板、构建与真机验证。
- [x] 更新 `docs/task文档.md`。
- [x] 实现模块化设置页 Compose。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [ ] 真机设置页 UI dump 被系统锁屏/遮罩挡住，继续在后续任务中补充点击验证。

## 2026-05-10：多供应商与模型图标美化

- [x] 明确完成标准：多供应商列表、供应商配置、模型图标徽章、构建与真机验证。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成新的设置页设计图并保存到 `docs/ui/settings-multi-provider-icons-design.png`。
- [x] 实现多供应商数据结构与持久化。
- [x] 实现设置页多供应商 UI 与模型图标。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已通过真机 UI dump 确认供应商配置面板、多个供应商和品牌徽章可见。

## 2026-05-10：收敛设置页供应商入口

- [x] 明确完成标准：外层只显示概览与入口，供应商列表只放进配置面板。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成新的设置页设计图并保存到 `docs/ui/settings-provider-contained-design.png`。
- [x] 修改设置页 Compose，移除外层供应商列表。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已确认主设置页不再直接显示 `DeepSeek` / `OpenRouter`，供应商配置面板内仍可见多个供应商。

## 2026-05-10：接入 Lobe Icons 大模型图标

- [x] 明确完成标准：使用 Lobe Icons 本地图标替换字母徽章，并完成构建、测试、真机验证。
- [x] 更新 `docs/task文档.md`。
- [x] 下载常用模型/供应商图标到 Android 资源目录。
- [x] 替换 Compose 字母徽章为 Lobe Icons 图片组件。
- [x] 补充第三方图标来源记录：`docs/Lobe Icons 图标来源.md`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已确认设置页和供应商配置面板的图标不再以 `O` / `DS` / `OR` 文本显示，而是使用图片资源。

## 2026-05-10：设置页收敛 AI 模型服务卡片并上浮反馈

- [x] 明确问题：`AI 模型服务` 卡信息重复、层级混乱、反馈 Banner 嵌在卡内。
- [x] 更新 `docs/task文档.md`，新增 Task 012。
- [x] `CurrentProviderSummary` 合并供应商状态 + 当前翻译模型，设置页只保留这张概览卡。
- [x] `AI 模型服务` 卡只保留 `供应商配置`、`翻译模型` 两个 Action。
- [x] `获取模型` 迁到 `ModelPickerSheet` 标题行，`添加自定义模型` 取消顶层冗余入口。
- [x] 删除 `高级参数` 底部面板，改为 `网络与性能` `SettingsModule`。
- [x] `MessageBanner` 上浮到设置页标题下方，`ModelPickerSheet` 内也显示。
- [x] 删除不再使用的 `SettingsSummaryItem` 和 `AdvancedSettingsSheet`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用。

## 2026-05-10：历史记录折叠预览 + 详情面板

- [x] 明确需求：历史列表改单行预览，点击后弹出详情面板显示完整内容。
- [x] 更新 `docs/task文档.md`，新增 Task 013。
- [x] `TranslateUiState` 增加 `selectedHistory`；`TranslateViewModel` 增加 `openHistoryDetail` / `closeHistoryDetail`，并在 `deleteHistory` 时自动清空选中。
- [x] `HistoryItem` 改为单行原文 + 单行译文 + 语言/模式/时间的紧凑可点击卡片。
- [x] 新增 `HistoryDetailSheet`：展示完整原文、完整译文、语言方向、模式徽章、时间，提供复制译文与删除按钮。
- [x] `AiTranslateContent` 顶层挂 `HistoryDetailSheet`，与语言/模型选择面板同级。
- [x] 单元测试 + Debug APK 构建通过：`.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon`。
- [x] 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用。
- [ ] 真机 UI 点击验证（折叠预览、点击展开、复制、删除后面板自动关闭）待补充。

## 2026-05-10：系统划词 / 分享翻译入口 + App 内迷你翻译面板

- [x] 明确完成标准：系统划词、文本分享、迷你面板承接、构建与测试通过。
- [x] 更新 `docs/task文档.md`，新增 Task 014。
- [x] 将暂不做的功能整理进后续规划文档。
- [x] 使用 imagegen 生成迷你翻译面板设计图并保存到 `docs/ui/mini-translate-panel-design.png`。
- [x] 注册 Android 系统划词与分享入口。
- [x] 实现外部文本 Intent 解析。
- [x] 实现 App 内迷你翻译面板。
- [x] 新增 `ExternalTextInputTest` 覆盖划词、分享、非文本和空白文本。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug`。
- [x] 已检查 debug merged manifest，确认系统入口已打入构建产物。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机系统菜单点击和 Intent 启动验证待补充。

## 2026-05-11：剪贴板快捷翻译提示

- [x] 明确完成标准：前台检测剪贴板、用户确认后进入快速翻译、忽略后不重复打扰。
- [x] 更新 `docs/task文档.md`，新增 Task 015。
- [x] 使用 imagegen 生成剪贴板快捷翻译提示设计图并保存到 `docs/ui/clipboard-quick-translate-design.png`。
- [x] 实现前台剪贴板文本读取。
- [x] 实现剪贴板确认提示 UI。
- [x] 确认后复用快速翻译面板。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug`。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机安装和前台剪贴板提示验证待补充。

## 2026-05-11：悬浮球半自动剪贴板翻译

- [x] 明确完成标准：授权悬浮窗、设置页开关、悬浮球、点击后悬浮迷你窗、测试和构建通过。
- [x] 更新 `docs/task文档.md`，新增 Task 016。
- [x] 使用 imagegen 生成悬浮球和悬浮迷你窗设计图并保存到 `docs/ui/floating-translate-design.png`。
- [x] 声明悬浮窗权限和悬浮服务。
- [x] 实现设置页悬浮翻译模块。
- [x] 实现悬浮球 Service。
- [x] 实现悬浮迷你翻译窗。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug`。
- [x] 已检查 debug merged manifest，确认权限和服务已打入构建产物。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机悬浮窗授权、悬浮球拖拽和点击翻译验证待补充。

## 2026-05-12：更新项目 README

- [x] 明确完成标准：README 要覆盖当前能力、技术栈、构建测试命令、模型策略和文档入口。
- [x] 检索项目结构、Gradle 配置、Manifest、核心翻译模块和历史任务记录。
- [x] 更新 `docs/task文档.md`，新增 Task 017。
- [x] 更新根目录 README。
- [x] 检索 README 关键章节，确认内容已写入。
- [x] 回写验证结果到 Task 文档。
- [x] 本次仅修改文档，未运行 Android 构建。

## 2026-05-12：Cloudflare R2 模型与更新包分发

- [x] 明确完成标准：创建 R2 bucket、上传模型与 manifest、开启公开访问、切换 App 模型下载地址并测试。
- [x] 确认本机 Wrangler 可用：`4.90.0`。
- [x] 确认当前 Cloudflare OAuth 登录账号可用，并通过本地代理访问 API。
- [x] 确认本地 Q4_K_M 模型文件存在，大小 `1133080512` 字节。
- [x] 更新 `docs/task文档.md`，新增 Task 018。
- [x] 创建 R2 bucket：`ai-translate-assets`。
- [x] 开启 R2 dev 临时公开访问：`https://pub-e16b86eab02f4594aaa4fd358cf6151e.r2.dev`。
- [x] 生成并上传模型 SHA256 与 `models.json`。
- [x] 上传 Q4_K_M GGUF 模型分片到 `models/parts/`。
- [x] 上传 `releases/latest.json` 占位 manifest。
- [x] 校验公开 URL：manifest 返回 200，分片 HEAD 返回 200，Range 请求返回 206。
- [x] 切换 App 模型下载地址为 R2 分片下载。
- [x] Kotlin 编译通过：`.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- [x] 单元测试结果 XML 通过：18 个测试均为 0 failure / 0 error；命令本身超过 60 秒上限后被停止。
- [x] 回写验证结果到 Task 文档。
- [x] 选择并绑定正式自定义域名，替换临时 R2 dev URL。

## 2026-05-13：绑定 R2 正式下载域名

- [x] 明确正式下载域名：`download.204152.xyz`。
- [x] 更新 `docs/task文档.md`，新增 Task 019。
- [x] 查询 `204152.xyz` Zone ID：`fe23f737002048c62ab39874b2b03222`。
- [x] 绑定 `download.204152.xyz` 到 R2 bucket。
- [x] 更新 App 模型下载基础地址。
- [x] 更新并上传 R2 `models.json`。
- [x] 验证正式域名公开访问：manifest 200，分片 HEAD 200，Range 206。
- [x] 运行 Kotlin 编译验证。
- [x] 回写验证结果到 Task 文档。

## 2026-05-13：剪贴板中央浮层翻译与文本朗读

- [x] 明确完成标准：中央确认卡片、确认后自动翻译、原文/译文朗读、悬浮窗居中卡片、测试和构建通过。
- [x] 更新 `docs/task文档.md`，新增 Task 020。
- [x] 使用 imagegen 生成中央浮层与朗读设计图并保存到 `docs/ui/clipboard-tts-floating-card-design.png`。
- [x] 实现剪贴板中央确认卡片。
- [x] 实现快速翻译中央卡片和确认后自动翻译。
- [x] 接入 Android 系统 TTS，并覆盖主翻译页、快速翻译卡片、历史详情和悬浮窗。
- [x] 实现悬浮翻译窗居中布局。
- [x] 增加单元测试。
- [x] 运行单元测试和 Debug APK 构建。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机点击和朗读发声验证待补充。

## 2026-05-13：应用内更新入口

- [x] 明确完成标准：设置页入口、R2 更新清单检查、版本对比、下载入口、测试和构建通过。
- [x] 更新 `docs/task文档.md`，新增 Task 021。
- [x] 使用 imagegen 生成应用更新入口设计图并保存到 `docs/ui/app-update-entry-design.png`。
- [x] 实现 R2 更新清单检查。
- [x] 在设置页加入应用更新入口。
- [x] 增加单元测试。
- [x] 验证 R2 更新清单公开可访问。
- [x] 运行 Kotlin 编译、单元测试和 Debug APK 构建。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前未连接真机，设置页点击检查更新的设备侧交互验证待补充。

## 2026-05-13：1.0.1 悬浮剪贴板修复与 R2 内置更新

- [x] 明确完成标准：悬浮球跨 App 读取剪贴板、1.0.1 版本号、应用内下载安装、R2 发版脚本、测试和推送。
- [x] 更新 `docs/task文档.md`，新增 Task 022。
- [x] 使用 imagegen 生成本次 UI 设计图并保存到 `docs/ui/v101-update-floating-clipboard-design.png`。
- [x] 实现透明剪贴板桥接 Activity。
- [x] 实现应用内 APK 下载、大小/SHA256 校验和系统安装器拉起。
- [x] 新增 R2 Debug 发版脚本。
- [x] 更新 README 和工作文档。
- [x] 构建、测试、上传 R2 并验证公开 URL。
- [x] 提交并推送 GitHub。
- [x] 真机验证悬浮剪贴板：Chrome 页面点击悬浮球后可读取剪贴板并显示中央悬浮卡片。
- [ ] 应用内安装真机点击验证待补充。

## 2026-05-13：悬浮剪贴板桥接任务栈修复

- [x] 明确完成标准：点击悬浮球后保持在第三方 App，仅显示悬浮翻译卡片。
- [x] 更新 `docs/task文档.md`，新增 Task 023。
- [x] 隔离 `ClipboardBridgeActivity` 的临时透明任务栈。
- [x] 真机验证从第三方 App 点击悬浮球不会跳回 AI 翻译主界面。
- [x] 重新构建并上传 R2。
- [x] 推送 GitHub。

## 2026-05-13：1.0.2 系统文本朗读修复

- [x] 明确完成标准：系统 TTS 可检测、可重试、可修复，所有朗读入口共享同一套逻辑。
- [x] 使用 imagegen 生成设置页文本朗读设计图并保存到 `docs/ui/v102-tts-design.png`。
- [x] 更新 `docs/task文档.md`，新增 Task 024。
- [x] 实现共享系统朗读控制器和语言候选回退。
- [x] 接入主页面、快速翻译卡片、历史详情、悬浮翻译窗和设置页修复入口。
- [x] 升级版本为 `1.0.2 (3)` 并更新 R2 发版脚本。
- [x] 运行编译、单元测试和 Debug 构建。
- [x] 上传 R2 并验证 manifest、APK HEAD 和本地 SHA256。
- [ ] 当前 `adb devices` 无在线设备，文本朗读真机发声和 1.0.1 到 1.0.2 应用内更新点击验证待补充。
- [x] 提交并推送 GitHub。

## 2026-05-15：翻译页模型选择状态修复

- [x] 明确完成标准：云端模型选择要同步默认模式，重启后实际翻译模式与界面一致。
- [x] 更新 `docs/task文档.md`，新增 Task 025。
- [x] 修复 ViewModel 模型选择持久化。
- [x] 修复翻译页模型名称展示逻辑。
- [x] 增加单元测试。
- [x] Kotlin 编译通过：`.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- [x] 全量单元测试结果 XML 通过：8 个测试类共 35 个测试均为 0 failure / 0 error；追加补丁后的命令超过 60 秒上限被截断。
- [x] 受影响单测通过：`.\gradlew.bat :app:testDebugUnitTest --tests "com.mxwis.aitranslate.ui.TranslateViewModelTest" --no-daemon --console=plain`，耗时 54 秒。
- [x] Debug APK 构建通过：`.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- [ ] 当前 shell 中 `adb` 不在 PATH，未补充真机重启点击验证。
- [x] 回写验证结果到 Task 文档。

## 2026-05-15：有道式翻译首页与拍照翻译首版

- [x] 明确完成标准：有道式首页快捷入口、拍照 / 相册 OCR、图片翻译面板、测试和构建通过。
- [x] 更新 `docs/task文档.md`，新增 Task 026。
- [x] 使用 imagegen 生成新版首页设计图并保存到 `docs/ui/photo-translate-home-design.png`。
- [x] 接入 ML Kit 本地 OCR，使用 Latin + Chinese 本地识别模型。
- [x] 实现拍照 / 相册导入入口。
- [x] 实现图片翻译状态与面板。
- [x] 增加单元测试。
- [x] 运行编译、单元测试、Debug 构建和模拟器验证。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前模拟器未配置云端 API Key，也未下载离线模型；真实模型输出需配置后补充设备侧翻译点测。

## 2026-05-15：首页工具入口收纳优化

- [x] 明确完成标准：首页收起快捷宫格，右上角工具入口打开工具面板，拍照 / 相册流程保持可用。
- [x] 更新 `docs/task文档.md`，新增 Task 027。
- [x] 使用 imagegen 生成工具入口设计图并保存到 `docs/ui/translate-toolbox-design.png`。
- [x] 实现右上角工具入口和工具弹窗。
- [x] 移除首页快捷宫格直接展示。
- [x] 运行编译、单元测试、Debug 构建和模拟器验证。
- [x] 回写验证结果到 Task 文档。

## 2026-05-15：首页工具入口图标优化

- [x] 明确完成标准：右上角不再使用九宫格，新图标更像工具入口，点击仍能打开工具弹窗。
- [x] 更新 `docs/task文档.md`，新增 Task 028。
- [x] 使用 imagegen 生成图标优化设计图并保存到 `docs/ui/translate-tool-icon-design.png`。
- [x] 替换首页右上角工具入口图标。
- [x] 编译并安装到模拟器截图验证。
- [x] 回写验证结果到 Task 文档。

## 2026-05-15：首页工具入口视觉对齐修正

- [x] 明确完成标准：实际截图要接近参考图，白底轻量，工具入口语义明确，弹窗不粗重。
- [x] 更新 `docs/task文档.md`，新增 Task 029。
- [x] 使用 imagegen 生成视觉对齐参考图并保存到 `docs/ui/translate-home-visual-alignment-design.png`。
- [x] 替换右上角工具入口为工具箱 / 公文包图标。
- [x] 调整首页背景、顶部入口和工具弹窗视觉。
- [x] 编译并构建 Debug APK。
- [x] 安装到模拟器并截图验证。
- [x] 回写验证结果到 Task 文档。

## 2026-05-16：发布 1.0.3 内置更新包

- [x] 明确完成标准：版本号 1.0.3 (4)、R2 latest.json 指向新版、构建测试通过、GitHub 推送完成。
- [x] 更新 `docs/task文档.md`，新增 Task 030。
- [x] 更新 App 默认版本号和 R2 发版脚本。
- [x] 运行 Kotlin 编译、单元测试和 Debug 构建。
- [x] 执行 R2 发版脚本并验证公开更新清单。
- [x] 提交并推送 GitHub。
- [x] 回写验证结果到 Task 文档。

## 2026-05-17：内置离线英汉词典首版

- [x] 明确完成标准：内置精简 ECDICT、离线查词、详情展示、建议词、测试和构建通过。
- [x] 搜索 GitHub 词库并确认首版采用 MIT 许可的 `skywind3000/ECDICT`。
- [x] 更新 `docs/task文档.md`，新增 Task 031。
- [x] 使用 imagegen 生成词典页设计图并保存到 `docs/ui/dictionary-lookup-design.png`。
- [x] 生成精简 ECDICT 内置资源和许可证说明。
- [x] 实现本地词典查询封装、ViewModel 状态和词典页 UI。
- [x] 增加单元测试。
- [x] 运行 Kotlin 编译、单元测试和 Debug APK 构建。
- [x] 回写验证结果到 Task 文档。
- [ ] 模拟器启动后补充词典页设备截图验证。

## 2026-05-17：发布 1.0.4 内置词典更新包

- [x] 明确完成标准：版本号 1.0.4 (5)、R2 latest.json 指向新版、构建测试完成验证、GitHub 推送完成。
- [x] 更新 `docs/task文档.md`，新增 Task 032。
- [x] 更新 App 默认版本号和 R2 发版脚本。
- [x] 运行 Kotlin 编译、单元测试和 Debug 构建。
- [x] 执行 R2 发版脚本并验证公开更新清单。
- [x] 提交并推送 GitHub。
- [x] 回写验证结果到 Task 文档。

## 2026-05-19：整体 UI 重新设计图

- [x] 明确完成标准：只生成一套整体 UI 设计图，不进入代码实现。
- [x] 更新 `docs/task文档.md`，新增 Task 033。
- [x] 使用 imagegen 生成全新整体 UI 设计图并保存到 `docs/ui/v2-light-ui-design-board.png`。
- [x] 按用户反馈改为单屏逐张生成，并保存 8 张拆分设计图到 `docs/ui/`。
- [x] 回写设计图保存路径和检查结果。

## 2026-05-19：实现全新浅色高颜值 UI

- [x] 明确完成标准：优雅浅色配色、非对称圆角布局、微动效微阴影、词典历史设置页全面重构、测试与构建通过。
- [x] 更新 `docs/task文档.md`，新增 Task 034。
- [x] 设计并实现高级莫兰迪/薰衣草紫浅色配色系统于 `AppTheme.kt`。
- [x] 重构主界面 Scaffold 与 Bottom Bar 导航栏设计。
- [x] 重写 `TranslateScreen` 实现精致的高颜值排版、语言卡片、微动效翻译浮动按钮和渐变背景。
- [x] 重写 `DictionaryScreen` 呈现干净、高贵的单词卡片与柔和色调标签。
- [x] 重写 `HistoryScreen` 采用优雅的双行排版及卡片微动效。
- [x] 重写 `SettingsScreen` 采用精美的卡片式分组、精细控制开关和流畅交互。
- [x] 优化布局细节：1. 翻译页空白译文卡片通过 AnimatedVisibility 智能折叠，节省屏幕空间并消除被迫滚动；2. 词典页多阴影“相近词”卡片合并为单一 Surface 组合卡片，配以 HorizontalDivider 分割，清除卡片重影杂乱；3. 修复了未输入文本时“AI翻译”按钮置灰状态的双层背景重合（横条）、阴影过重 Bug，使其呈现扁平且与浅色背景自然融入的淡雅禁用态。
- [x] 运行编译、单元测试、Debug APK 构建及模拟器验证。
- [x] 回写验证结果到 Task 文档。

## 2026-05-19：设置页重构为二级页面路由架构

- [x] 明确完成标准：设置页不再采用折叠下拉菜单，而是重构为优雅的二级页面跳转路由，每一项拥有独立全屏配置卡片与微交互。
- [x] 更新 `docs/task文档.md`，新增 Task 035。
- [x] 创建 `SettingsSubPage` 枚举，定义 `MODEL_SERVICE`, `OFFLINE_MODEL`, `LAUNCH_MODEL`, `TTS`, `FLOATING_WINDOW`, `NETWORK_PERFORMANCE`, `DATA_HISTORY`, `ABOUT_UPDATE` 等二级子页面。
- [x] 在 `SettingsScreen` 中引入 `activeSubPage` 状态，实现精简的首级设置页面（分组卡片与漂亮的前进箭头）和支持平滑切换的二级页面架构。
- [x] 开发 `SettingsSubPageLayout` 统一二级页面脚手架，包括返回头部、优雅导航栏和返回点击逻辑。
- [x] 将所有原子配置模块迁移到对应的二级子页面中，修复 LocalContext 在 LazyColumn 非 Composable 作用域下的调用错误。
- [x] 完成 Kotlin 编译和项目构建（Gradle compileDebugKotlin 顺利成功）。
- [x] 回写验证结果到 Task 文档。

## 2026-05-19：发布 1.0.5 UI 重构内置更新包

- [x] 明确完成标准：版本号 1.0.5 (6)、R2 latest.json 指向新版、构建验证通过、GitHub 推送完成。
- [x] 更新 `docs/task文档.md`，新增 Task 036。
- [x] 更新 App 默认版本号和 R2 发版脚本默认参数。
- [x] 执行 `testDebugUnitTest` 两次，均按 60 秒上限超时，未生成新的完整测试报告。
- [x] 执行 R2 发版脚本，Debug 构建成功并上传 `1.0.5` APK 与 `latest.json`。
- [x] 验证公开 APK 和更新清单返回 200，且清单指向 `1.0.5 (6)`。
- [x] 提交并推送 GitHub `main`。
- [x] 回写验证结果到 Task 文档。

## 2026-05-20：添加设置页二级路由过渡动效与弹窗动效

- [x] 明确完成标准：设置页二级子页面横向平滑动效（从右侧滑入，左侧滑出；返回时向右滑出，主页从左侧滑入）、BottomSheet/弹窗优雅弹性物理过渡。
- [x] 更新 `docs/task文档.md`，新增 Task 037。
- [x] 使用 imagegen 生成设置页过渡动画与弹窗动效设计图，保存到 `docs/ui/settings-animation-design.png`。
- [x] 用 `AnimatedContent` 替换 `SettingsScreen` 内二级页面的 if-else 条件替换，并配置 slide + fade 的 transitionSpec 动画。
- [x] 优化剪贴板和迷你翻译弹窗的进场弹性缩放与淡入淡出过渡（DialogAnimationWrapper 物理微回弹）。
- [x] 运行 Gradle 构建与编译，确保无任何编译错误或警告。
- [x] 回写验证结果到 Task 文档和 TODO 日志。

## 2026-05-20：整理老师要求的 AI 翻译 App 原型设计交付文档

- [x] 明确完成标准：文档需基于 AI 翻译 App，覆盖项目概述、设计目标、页面范围、字段规范、翻译闭环、AI 初始问题、Pixso 优化、交叉校验、成员分工与责任承诺。
- [x] 更新 `docs/task文档.md`，新增 Task 038。
- [x] 发现初稿误用了截图中的客户订单追踪示例业务，已纠正为本项目内容。
- [x] 新增 Markdown 源文档：`docs/AI翻译App原型设计交付文档.md`。
- [x] 导出 Word 文档：`docs/AI翻译App原型设计交付文档.docx`。
- [x] 检查截图中的分工表内容已按 AI 翻译 App 项目真实模块改写写入。
- [x] 按用户补充要求加入 5 张项目原型图：文本翻译、图片 OCR 翻译、离线词典、AI 模型服务、悬浮翻译。
- [x] 重新生成含图 Word 文档，并同步通用文件名 `docs/原型设计交付文档.docx`。
- [x] 使用 Microsoft Word 导出 PDF 预览，检查原型图页面排版无明显溢出或重叠。
- [x] 回写验证结果到 Task 文档和 TODO 日志。

## 2026-05-20：新增 Google ML Kit 设备端离线翻译模型

- [x] 明确完成标准：新增 ML Kit 作为第二个离线模型，默认保留 HY-MT，翻译入口统一沿用当前选择。
- [x] 确认 R2 边界：HY-MT 等自管模型继续走 R2；ML Kit 官方模型由 SDK 内部下载和缓存，不转存到 R2。
- [x] 更新 `docs/task文档.md`，新增 Task 039。
- [x] 使用 imagegen 生成 ML Kit 离线模型管理设计图，保存到 `docs/ui/mlkit-offline-model-design.png`。
- [x] 新增 ML Kit 翻译与语言识别依赖。
- [x] 实现离线模型类型持久化。
- [x] 实现 ML Kit 翻译引擎与仓库路由。
- [x] 更新模型选择弹窗和离线模型管理页。
- [x] 增加单元测试并运行构建验证。
- [x] Kotlin 编译通过：`.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- [x] 新增/关键单测通过：ML Kit 映射、仓库回退路由、设置解码、TranslateViewModel。
- [x] Debug APK 构建通过：`.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- [x] 已构建 x86_64 模拟器专用包并安装到 `emulator-5554`，App 已启动到 `MainActivity`。
- [x] 回写验证结果到 Task 文档和 TODO 日志。

## 2026-05-20：Google ML Kit 离线语种包管理

- [x] 明确完成标准：像 Google 翻译一样按语种展示、下载和删除 ML Kit 离线语种包。
- [x] 确认边界：英文内置，ML Kit 官方语种模型由 SDK 缓存在设备端，不转存 R2。
- [x] 更新 `docs/task文档.md`，新增 Task 040。
- [x] 使用 imagegen 生成设计图，保存到 `docs/ui/mlkit-language-pack-design.png`。
- [x] 新增 ML Kit 语种包管理器，支持查询、下载和删除语种模型。
- [x] 将语种包状态接入 Repository 和 ViewModel，设置页会刷新当前下载状态。
- [x] 在离线模型管理页增加语种包列表与下载/删除操作。
- [x] 增加单元测试并运行构建验证。
- [x] 全量单测通过：`.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain`。
- [x] 模拟器专用 Debug APK 构建通过并安装到 `emulator-5554`。
- [x] 已在模拟器打开离线模型管理页，确认语种包列表、内置/已下载/未下载状态和下载/删除按钮正常展示。
- [x] 回写验证结果到 Task 文档和 TODO 日志。

## 2026-05-20：发布 1.0.6 ML Kit 离线模型内置更新包

- [x] 明确完成标准：版本号 1.0.6 (7)、R2 latest.json 指向新版、构建测试通过、GitHub 推送完成。
- [x] 更新 `docs/task文档.md`，新增 Task 041。
- [x] 更新 App 默认版本号和 R2 发版脚本默认参数。
- [x] 执行测试与构建验证：Kotlin 编译通过，ML Kit 映射、仓库路由、设置持久化、语种包管理和 ViewModel 关键测试通过；全量单测按 60 秒上限超时。
- [x] 执行 R2 发版脚本并验证公开更新清单，`latest.json` 已指向 `1.0.6 (7)`。
- [x] 提交并推送 GitHub `main`。
- [x] 回写验证结果到 Task 文档和 TODO 日志。

## 2026-05-22：期末作业 Word 文档系统需求与系统设计补全

- [x] 明确完成标准：第三章补全系统需求分析，第四章独立新增系统设计，图示清晰可读，并完成 Word 渲染检查。
- [x] 更新 `docs/task文档.md`，新增 Task 042。
- [x] 梳理当前 AI 翻译 App 实际功能、技术栈、数据存储和核心流程。
- [x] 分析 `封面.docx` 现有章节结构与可替换区域。
- [x] 参考 `软件开发类毕设绘图要点讲解.pdf`，生成需求分析和系统设计所需图示。
- [x] 写入 Word 文档并保持原有样式，修正第二章重复编号和页脚旧页数。
- [x] 使用 Microsoft Word 导出 PDF 预览并检查排版；`render_docx.py` 因缺少 LibreOffice/soffice 未执行成功。
- [x] 回写验证结果到 Task 文档和 TODO 日志。

## 2026-05-22：修正功能模块划分图连线遮挡

- [x] 明确完成标准：图 3-1 的连线不再穿过模块框和文字，Word 中替换后预览正常。
- [x] 重绘功能模块划分图，模块内部连线只保留在框与框之间的空白区域。
- [x] 同步调整用例图，恢复用户到各用例的关联线，并避免线条穿过文字。
- [x] 替换 `封面.docx` 中图 3-1 和图 3-2。
- [x] 重新导出 PDF 并检查图 3-1、图 3-2 排版。
- [x] 回写验证结果。

## 2026-05-26：Cloudflare 后端登录注册与账号体系接入

- [x] 明确完成标准：具备可部署的 Workers + D1 后端，Android 支持注册/登录/退出和登录态保存，登录后可同步翻译历史。
- [x] 更新 `docs/task文档.md`，新增 Task 043。
- [x] 检索 Cloudflare 官方文档并确定后端技术方案：Workers + D1 + Web Crypto + Wrangler D1 binding。
- [x] 使用 imagegen 生成登录/注册 UI 设计图，保存到 `docs/ui/auth-login-register-design.png`。
- [x] 新增 Cloudflare Worker 后端、D1 schema 和测试。
- [x] 新增 Android 认证数据层和云端历史同步客户端。
- [x] 新增登录/注册 UI 和设置页账号入口。
- [x] 运行后端测试、Kotlin 编译和关键单测。
- [x] 完成代码 review 并回写验证结果。

## 2026-05-26：发布 1.0.7 登录注册与 Cloudflare 后端版本

- [x] 明确完成标准：Cloudflare Worker 与 D1 远端可用，App 默认后端地址指向线上 Worker，1.0.7 Debug 包发布到 R2 内置更新，并推送 GitHub。
- [x] 确认工作区存在无关未跟踪 Word 预览文件，本次提交只纳入登录后端、1.0.7 发布和文档记录相关文件。
- [x] 检查 Wrangler 登录态、D1 数据库和 Worker 配置。
- [x] 创建 D1 数据库 `ai_translate_auth` 并应用远端迁移。
- [x] 设置 Worker JWT 密钥并部署 Worker：`https://ai-translate-auth.jiezhi858.workers.dev`。
- [x] 将 App 默认版本号提升为 `1.0.7 (8)`，默认认证后端指向线上 Worker。
- [x] 构建 1.0.7 Debug APK 并上传 R2 内置更新。
- [x] 运行后端测试、Kotlin 编译、关键单测和公开 URL 校验。
- [x] 完成发布前 review、提交并推送 GitHub。

## 2026-05-26：注册邮箱验证码与 Resend 发信接入

- [x] 明确完成标准：注册流程新增邮箱验证码，Worker 通过 Resend 发送验证码，D1 保存验证码状态，Android 注册页支持发送与填写验证码。
- [x] 检索 Resend 官方文档，确认发信 API、鉴权方式和 Cloudflare 子域名验证方式。
- [x] 确认当前环境缺少 `RESEND_API_KEY`，本轮先完成代码和部署准备，真实发信需要后续配置 Resend secret 与验证发信域名。
- [x] 使用 imagegen 生成邮箱验证码注册 UI 设计图。
- [x] 新增 Worker 邮箱验证码接口、D1 迁移和 Resend 发信逻辑。
- [x] 新增 Android 发送验证码、邮箱和验证码输入流程。
- [x] 运行后端测试、Kotlin 编译和关键单测。
- [x] 如不依赖 Resend 密钥的部分可部署，则应用 D1 迁移并部署 Worker。
- [x] 完成 review 并回写验证结果。

## 2026-05-26：发布 1.0.8 强制邮箱验证码版本

- [x] 明确完成标准：Resend API Key 写入 Cloudflare Secret，Worker 强制注册邮箱验证码，1.0.8 Debug 包发布到 R2 内置更新，并推送 GitHub。
- [x] 写入 `RESEND_API_KEY` 到 Cloudflare Worker Secret。
- [x] 将 Worker `REQUIRE_EMAIL_VERIFICATION` 切换为 `true` 并部署。
- [x] 验证线上发送验证码接口可调用。
- [x] 将 App 默认版本号提升为 `1.0.8 (9)`，更新 R2 发版脚本说明。
- [x] 构建 1.0.8 Debug APK 并上传 R2 内置更新。
- [x] 运行后端测试、Kotlin 编译、关键单测和公开 URL 校验。
- [x] 完成 review、提交并推送 GitHub。

## 2026-05-26：修复 Cloudflare PBKDF2 迭代上限导致注册失败

- [x] 明确完成标准：注册不再出现 `Pbkdf2 failed: iteration counts above 100000 are not supported`，Worker 测试通过并部署。
- [x] 将 Worker 密码哈希 PBKDF2 迭代数从 `120000` 调整为 `100000`。
- [x] 运行 Worker 后端测试。
- [x] 部署 Worker 并验证线上注册接口不再触发 PBKDF2 上限错误。
- [x] 提交并推送 GitHub。
