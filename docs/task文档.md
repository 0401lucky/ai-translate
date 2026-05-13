# Task 文档

## Task 001：第一版文本翻译 App

### 目标

从空目录创建 Android App 第一版，实现文本翻译主流程、云端 OpenAI 兼容配置、离线模型下载状态、历史记录和基础设置。

### 范围

- 创建 Android 工程骨架。
- 生成并保存 UI 设计图。
- 实现 Compose 页面。
- 实现云端翻译接口。
- 实现离线模型下载、删除和状态管理。
- 实现历史记录保存、展示和清空。
- 写入悬浮窗后续规划文档。

### 不包含

- 第一版不实现悬浮窗。
- 第一版不实现跨 App 划词。
- 第一版不把 600MB 模型内置进 APK。
- 第一版不强行接入未验证稳定的 GGUF 推理内核。

### 完成标准

- Debug APK 能成功构建。
- UI 设计图已生成并保存到 `docs/ui/`。
- App 能打开首页、设置页、模型页和历史页。
- 云端配置缺失时给出明确提示。
- 空输入不会触发翻译请求。
- 离线模型未下载时阻止离线翻译并提示用户。
- 模型下载、删除和状态展示流程可用。
- 历史记录能保存、展示、删除和清空。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 当前未连接真机或模拟器，UI 点击验证和离线推理真机验证待设备可用后执行。
- AGP 9.1.0 + KSP 当前需要 `android.disallowKotlinSourceSets=false` 兼容开关，后续升级工具链时复查。

## Task 002：接入离线 GGUF 推理

### 目标

修复“模型已下载但离线翻译不可用”的问题，让 App 能调用已下载的 Hy-MT GGUF 模型执行本地翻译。

### 范围

- 接入 Android 可用的 llama.cpp 封装库。
- 替换 `OfflineTranslationEngine` 的占位错误。
- 使用 HY-MT 官方推荐 prompt 和推理参数。
- 保持现有模型下载路径不变，继续使用 App 私有目录中的 GGUF 文件。
- 增加基础测试，验证离线 prompt 和模型路径逻辑。

### 完成标准

- Debug APK 能成功构建。
- 离线模式不再直接提示“推理内核未接入”。
- 模型未下载时仍能明确提示先下载。
- 模型已下载时会尝试加载本地 GGUF 并生成译文。
- 真机安装启动后无明显崩溃。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66`。
- 已确认手机内模型文件完整，大小为 `600534880` 字节，与 Hugging Face `X-Linked-Size` 一致。
- 已确认新包包含 `libllama.so`、`libllama_jni.so` 等 native 推理库。
- 真机离线加载失败：通用 Llamatik/llama.cpp 无法加载 Hy-MT 2bit GGUF。
- 结论：当前公开推理内核暂不支持该模型的专用 2bit 量化；需等待腾讯/AngelSlim 发布专用 llama.cpp Android kernel 后继续接入。

## Task 003：API 配置页获取模型列表

### 目标

在设置页增加“通过 Base URL 和 API Key 获取实际模型”的能力，减少用户手动填写模型名称的出错概率。

### 范围

- 调用 OpenAI 兼容的 `/v1/models` 接口。
- 校验 Base URL 和 API Key 不能为空。
- 解析接口返回的模型 `id` 列表。
- 在设置页展示拉取中、成功、失败和空列表状态。
- 支持点击已获取模型后回填并保存到模型名称。
- UI 调整前生成新的设置页设计图并保存到 `docs/ui/`。

### 完成标准

- Debug APK 能成功构建。
- 单元测试覆盖模型接口地址解析和模型列表解析。
- 设置页可以点击“获取模型”，并能把返回模型写入当前配置。
- 请求失败时有明确错误提示。
- Base URL 或 API Key 缺失时不发起网络请求。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-fetch-models-design.png`。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已安装到真机设备 `1fd9b66` 并启动应用。

## Task 004：切换离线模型到 Hy-MT 1.25bit

### 目标

将离线模型下载目标从当前不可加载的 2bit GGUF 切换到 AngelSlim 已发布 STQ1_0 kernel 路线的 Hy-MT 1.25bit GGUF。

### 范围

- 更新模型文件名、下载地址、预期大小和基础校验阈值。
- 更新模型页文案，避免继续展示 2bit/601MB。
- 更新离线加载失败提示，明确 1.25bit 依赖 STQ1_0 kernel。
- 保留旧 2bit 文件，不在未经确认时自动删除用户手机上的模型文件。

### 完成标准

- Debug APK 能成功构建。
- 模型页显示 1.25bit、约 440MB。
- App 不再把旧 2bit 文件识别为当前可用离线模型。
- 如当前 APK 内核仍无法加载，提示需接入 STQ1_0 Android 内核。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已确认手机 App 私有目录仍存在旧 2bit 文件：`Hy-MT1.5-1.8B-2bit.gguf`，大小 `600534880` 字节。
- 当前版本使用新的 1.25bit 文件名，因此旧文件不会被识别为当前离线模型。
- 用户已确认删除旧模型，旧 2bit 文件已从手机 App 私有目录删除。

## Task 005：切换到标准 Q4_K_M 离线模型

### 目标

绕过 1.25bit STQ1_0 内核尚未集成的问题，切换到当前 Llamatik/llama.cpp 内核可识别的标准 Hy-MT Q4_K_M GGUF，让离线翻译优先恢复可运行。

### 范围

- 更新模型下载目标为腾讯官方 `HY-MT1.5-1.8B-Q4_K_M.gguf`。
- 更新模型页文案为 Q4_K_M、约 1.13GB。
- 更新离线加载错误提示，不再指向 STQ1_0。
- 保留已下载的 1.25bit 文件，不未经确认自动删除。
- 生成新的模型页 UI 设计图。

### 完成标准

- Debug APK 能成功构建。
- 模型页显示 Q4_K_M 和约 1.13GB。
- 当前 APK 不再把 1.25bit 文件识别为当前模型。
- 手机安装新版后可下载或放置 Q4_K_M 模型。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/model-q4km-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已将 Q4_K_M 模型放入手机 App 私有目录：`HY-MT1.5-1.8B-Q4_K_M.gguf`。
- 模型文件大小为 `1133080512` 字节，SHA256 为 `4383AC0C3C8E476DE98FF979C2A3F069F8C4FB385E7860CF2D28DA896CC477C7`。
- 真机离线翻译验证通过：输入 `hi`，离线模式输出 `嗨`，界面提示“已使用离线翻译”。
- 用户已确认删除旧 1.25bit 文件，手机 App 私有目录目前仅保留 Q4_K_M 模型。

## Task 006：API 模型下拉搜索与手动添加

### 目标

优化设置页 API 模型选择体验，将现有模型按钮列表升级为可搜索下拉列表，并支持手动添加适合翻译的模型。

### 范围

- 使用下拉选择组件展示模型候选，容纳更多模型。
- 支持在下拉列表内搜索模型。
- 支持手动添加模型名称。
- 手动添加的模型持久化保存到 DataStore。
- 获取到的模型与手动添加模型合并去重。
- 添加模型后自动选中并保存为当前模型。
- UI 调整前生成新的设置页设计图。

### 完成标准

- Debug APK 能成功构建。
- 单元测试覆盖自定义模型序列化和去重逻辑。
- 设置页可搜索模型并从下拉列表选中。
- 设置页可手动添加模型并自动选中。
- 重启 App 后手动添加模型仍保留。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-model-dropdown-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已打开设置页确认可见 `翻译模型`、`获取模型`、`已获取 0 个模型，已添加 0 个`。

## Task 007：模块化设置页重新设计

### 目标

参考当前手机中 FluxDo 的 `AI 模型服务` 设置结构，重新设计本 App 的设置页，将 API 配置升级为独立的“AI 模型服务”模块，并让设置页更适合后续扩展。

### 参考观察

- FluxDo 的 `AI 模型服务` 不是直接暴露一堆输入框，而是拆成 `供应商`、`模型配置`、`聊天记录`、`快捷词管理`、`高级设置`。
- `供应商` 页面展示供应商名称、协议类型和模型数量。
- `模型配置` 页面按用途配置默认模型，例如聊天模型、图像模型、标题生成模型等，并支持重置为自动推断。

### 范围

- 先生成新的设置页 UI 设计图。
- 设置页按模块组织：AI 模型服务、离线模型、翻译偏好、历史与数据、关于与许可。
- AI 模型服务模块展示服务状态、供应商数量、当前翻译模型、模型获取/搜索/添加入口。
- 暂不直接进入 Compose 实现，先完成设计图确认。

### 完成标准

- 新设计图保存到 `docs/ui/`。
- 设计图能清楚体现模块分组。
- API 配置不再表现为散落字段，而是归入 `AI 模型服务`。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-modular-ai-service-design.png`。
- 已对照 FluxDo 参考结构，把 API 配置改为 `AI 模型服务` 模块，并拆出供应商配置、翻译模型、获取模型、添加自定义模型和高级参数入口。

## Task 008：实现模块化设置页

### 目标

按 `docs/ui/settings-modular-ai-service-design.png` 实现设置页模块化 UI，让设置页从单列表单升级为可扩展的模块入口。

### 范围

- 设置页拆为 `AI 模型服务`、`离线模型`、`翻译偏好`、`历史与数据`、`关于与许可`。
- `AI 模型服务` 展示供应商、配置状态、当前翻译模型、模型数量。
- 供应商配置通过底部面板编辑 Base URL 和 API Key。
- 翻译模型、添加自定义模型复用现有可搜索模型选择器。
- 获取模型保留现有接口调用能力。
- 离线模型模块跳转到模型页。
- 历史与数据模块显示历史条数，并支持清空历史。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 真机安装后设置页显示模块化结构。
- 供应商配置底部面板可打开并编辑。
- 模型选择底部面板可打开。

### 验证记录

- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 真机 UI dump 一度被系统锁屏/遮罩挡住，模块化设置页继续在 Task 009 中补充验证。

## Task 009：多供应商与模型图标美化

### 目标

继续优化设置页的 `AI 模型服务` 模块，把供应商从单一 `OpenAI 兼容` 展示升级为多供应商列表，并为供应商和模型加入更清晰的品牌图标徽章。

### 参考观察

- 之前探索到 FluxDo 的 `AI 模型服务` 入口按 `供应商`、`模型配置`、`高级设置` 等模块组织。
- `供应商` 不是一个固定接口，而应支持多个服务商配置。
- 图标参考开源 AI/LLM 图标库路线：Lobe Icons 更贴合模型/供应商场景，Simple Icons 可作为品牌图标兜底。

### 范围

- 更新新的设置页设计图，体现多个供应商和模型图标。
- 数据层增加多供应商配置列表和当前选中供应商。
- 设置页 `AI 模型服务` 显示多个供应商卡片，可切换当前供应商。
- 供应商配置面板支持新增供应商、编辑供应商名称、Base URL 和 API Key。
- 模型选择列表增加模型品牌徽章。
- 保留现有 OpenAI 兼容翻译、获取模型、手动添加模型能力。

### 完成标准

- 新设计图保存到 `docs/ui/`。
- Debug APK 能成功构建。
- 单元测试通过。
- 设置页能看到多个供应商入口和当前选中供应商。
- 模型列表能显示模型品牌徽章。
- 真机安装后设置页可打开，供应商配置面板可进入。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-multi-provider-icons-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已通过真机 UI dump 确认供应商配置面板可打开，并能看到 `OpenAI`、`DeepSeek`、`OpenRouter`、`自定义兼容接口`。
- 已确认供应商/模型品牌徽章可见，例如 `O`、`DS`、`OR`。

## Task 010：收敛设置页供应商入口

### 目标

修正 `AI 模型服务` 模块中“外层供应商卡片”和“供应商配置入口”重复的问题。设置页外层只展示概览和入口，多供应商列表只放在供应商配置面板内。

### 范围

- 重新生成设置页设计图，体现外层精简、内层管理。
- 移除设置页外层的多供应商卡片列表。
- 外层保留当前供应商、配置状态、当前模型、模型数量和操作入口。
- 供应商配置面板继续支持多供应商切换、添加和编辑。
- 保留供应商与模型品牌徽章。

### 完成标准

- 新设计图保存到 `docs/ui/`。
- Debug APK 能成功构建。
- 单元测试通过。
- 真机设置页外层不再直接展示多个供应商卡片。
- 点击 `供应商配置` 后仍能看到多个供应商并切换。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-provider-contained-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已通过真机 UI dump 确认设置页外层不再显示 `DeepSeek` 和 `OpenRouter` 供应商卡片。
- 已通过真机 UI dump 确认 `供应商配置` 面板内仍显示 `OpenAI`、`DeepSeek`、`OpenRouter`、`自定义兼容接口`。

## Task 011：接入 Lobe Icons 大模型图标

### 目标

将当前供应商和模型的字母徽章替换为 `lobehub/lobe-icons` 开源图标资源，让设置页与模型选择页的 AI 模型标识更专业。

### 参考资料

- `lobehub/lobe-icons`：AI/LLM 品牌 SVG/PNG/WebP 图标集合。
- 许可：MIT License。
- Android 方案：使用静态 PNG 本地资源，避免运行时网络依赖和前端包接入复杂度。

### 范围

- 下载常用图标：OpenAI、DeepSeek、OpenRouter、Claude、Gemini、Qwen，必要时补充 Hunyuan。
- 保存到 Android `drawable-nodpi` 资源目录。
- 设置页供应商图标优先使用 Lobe Icons 图片。
- 模型选择列表和当前模型摘要优先使用 Lobe Icons 图片。
- 未匹配模型仍保留简洁文字兜底徽章。
- 补充第三方图标来源记录。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 真机设置页能看到真实模型/供应商图标，不再只显示 O/DS/OR 字母徽章。
- 供应商配置面板仍可正常打开。

### 验证记录

- 已下载 Lobe Icons 静态 PNG 图标到 `app/src/main/res/drawable-nodpi/`。
- 已新增图标来源记录：`docs/Lobe Icons 图标来源.md`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已通过真机 UI dump 确认设置页图标由 `ImageView` 承载，不再以 `O` / `DS` / `OR` 文本显示。
- 已确认 `供应商配置` 面板可打开，`OpenAI`、`DeepSeek`、`OpenRouter` 仍正常显示。

## Task 012：设置页收敛 AI 模型服务卡片并上浮反馈

### 目标

用户反馈设置页信息重复、层级混乱，把 `AI 模型服务` 模块瘦身为"概览 + 两个入口"，并整理高级参数、反馈 Banner 与模型获取入口的归属。

### 问题盘点

- `AI 模型服务` 卡内部连续出现 3 个 `SummaryItem`、`CurrentProviderSummary`、当前模型行、模型计数文案，同一份信息重复三遍。
- `获取模型`、`添加自定义模型`、`高级参数` 与 `供应商配置`、`翻译模型` 平级，但实际不是同一层级。
- `MessageBanner` 嵌在 `AI 模型服务` 卡内部，反馈长度变化会把卡片撑乱，不够显眼。
- `SettingsSummaryItem` 固定 `width = 96.dp`，中文供应商名容易被截断。

### 范围

- 精简 `AI 模型服务` 卡：只保留 `CurrentProviderSummary`（合并供应商状态 + 当前模型）+ `供应商配置`、`翻译模型` 两个入口。
- `获取模型` 作为工具按钮迁入 `ModelPickerSheet` 顶部标题行；`添加自定义模型` 原本就在面板底部，取消设置页上的冗余入口。
- 删除 `高级参数` 底部面板，改为独立 `网络与性能` `SettingsModule`，与翻译偏好、历史与数据平级。
- 把 `modelFetchMessage` / `modelFetchError` Banner 上浮到设置页标题下方；`ModelPickerSheet` 内也显示该 Banner，保证获取模型操作能在面板内看到反馈。
- 删除 `SettingsSummaryItem`、`AdvancedSettingsSheet` 两个不再使用的组件。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 设置页 `AI 模型服务` 卡只有概览卡 + 2 个 Action 行。
- `网络与性能` 作为独立模块出现。
- `获取模型` 按钮出现在模型选择面板标题行。
- 模型获取反馈同时显示在设置页顶部和模型面板内。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 本次改动仅影响设置页 Compose 层，未改动数据层、ViewModel 的模型获取流程或网络行为，真机交互验证待设备就绪后补充。
- 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用。

## Task 013：历史记录折叠预览 + 详情面板

### 目标

用户反馈历史记录条目文本太长时整张卡被撑开。把列表改为单行预览，点击后用底部面板显示完整原文与译文。

### 范围

- `HistoryItem` 改为单行原文、单行译文的紧凑卡片，整卡可点击。
- 新增 `HistoryDetailSheet`：展示模式徽章、语言方向、时间、完整原文、完整译文，提供复制译文与删除入口。
- `TranslateUiState` 增加 `selectedHistory` 字段；`TranslateViewModel` 增加 `openHistoryDetail` / `closeHistoryDetail`，并在 `deleteHistory` 时清空选中。
- `AiTranslateContent` 顶层挂 `HistoryDetailSheet`（与语言/模型选择面板同级）。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 历史列表每条只显示一行原文和一行译文。
- 点击列表项弹出详情面板；面板内可复制译文、删除并关闭。
- 删除当前详情对应的条目时，面板自动关闭。

### 验证记录

- `.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用，真机点击验证待补充。

## Task 014：系统划词 / 分享翻译入口 + App 内迷你翻译面板

### 目标

先实现低权限、高频使用的跨 App 文本入口，并在 App 内提供迷你翻译面板承接外部文本，作为后续悬浮窗翻译的前置能力。

### 范围

- 支持 Android 系统 `ACTION_PROCESS_TEXT` 划词入口，读取外部选中文本并进入翻译流程。
- 支持 Android 系统 `ACTION_SEND` 文本分享入口，读取分享文本并进入翻译流程。
- 从外部入口进入时打开 App 内迷你翻译面板，而不是直接打断主翻译页结构。
- 迷你面板展示外部原文、当前语言方向、模式选择提示、翻译状态、译文、复制译文和转到完整翻译页入口。
- 迷你面板复用现有翻译仓库、设置、历史记录和云端/离线/自动模式逻辑。
- 其余暂不实现的增强能力写入后续规划文档。

### 不包含

- 本任务不申请悬浮窗权限。
- 本任务不实现无障碍划词。
- 本任务不做 OCR、语音朗读、术语表、历史收藏导出等增强能力。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- Manifest 注册系统划词与文本分享入口。
- App 能从 `ACTION_PROCESS_TEXT` 和 `ACTION_SEND` Intent 中提取文本。
- 外部文本进入后自动打开迷你翻译面板。
- 迷你面板可触发翻译、展示译文、复制译文，并可把文本带入完整翻译页。
- 空文本或非文本分享不会触发异常。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/mini-translate-panel-design.png`。
- 新增纯 Kotlin 单元测试 `ExternalTextInputTest`，覆盖系统划词、文本分享、非文本分享和空白文本。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已检查 debug merged manifest，确认包含 `android.intent.action.PROCESS_TEXT`、`android.intent.action.SEND` 和 `singleTop`。
- 已定位 adb 路径，但当前 `adb devices` 无在线设备，真机系统菜单点击和 Intent 启动验证待设备连接后补充。

## Task 015：剪贴板快捷翻译提示

### 目标

补足用户复制文本后进入 App 的快捷路径：当 App 打开或回到前台时，如果剪贴板中存在文本，先弹出确认提示，用户确认后再打开快速翻译面板。

### 范围

- App 前台时读取系统剪贴板文本。
- 文本为空、重复文本或当前已有快速翻译面板时不重复提示。
- 提示用户“检测到剪贴板文本，是否快速翻译？”。
- 用户点击确认后复用现有 App 内迷你翻译面板。
- 用户点击忽略后，本次剪贴板内容不再重复打扰。
- 继续遵守隐私边界：不后台读取剪贴板，不自动提交翻译。

### 不包含

- 不做后台剪贴板监听。
- 不做自动翻译。
- 不读取图片、链接预览或其他非文本内容。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 打开或回到 App 前台时能识别剪贴板文本。
- 识别到文本后弹出确认提示。
- 确认后打开快速翻译面板，忽略后不重复弹同一段文本。
- 系统划词 / 分享入口仍可正常打开快速翻译面板。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/clipboard-quick-translate-design.png`。
- 新增 `ExternalTextInput.extractClipboardText`，并补充剪贴板文本清理和空白过滤单元测试。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 当前 `adb devices` 无在线设备，未能安装到真机补充前台剪贴板提示验证。

## Task 016：悬浮球半自动剪贴板翻译

### 目标

实现低风险的跨 App 快捷翻译：用户复制文本后点击悬浮球，App 显示悬浮迷你翻译窗，并尝试读取剪贴板文本进行翻译。

### 范围

- 申请并引导用户开启 `SYSTEM_ALERT_WINDOW` 悬浮窗权限。
- 在设置页新增 `悬浮翻译` 模块，显示权限状态，并提供开启/关闭悬浮球入口。
- 实现悬浮球 Service，悬浮球可拖拽、可关闭。
- 点击悬浮球后展示悬浮迷你翻译窗。
- 悬浮迷你翻译窗读取剪贴板文本，展示原文、翻译状态、译文、复制译文和关闭按钮。
- 翻译逻辑复用现有 `TranslationRepository`，语言方向使用自动检测到简体中文，模式使用当前默认翻译模式。

### 不包含

- 不实现无障碍自动监听。
- 不后台自动读取剪贴板。
- 不自动弹窗打断用户，必须由用户点击悬浮球触发。
- 不做前台服务常驻通知，当前版本为用户手动开启后的普通悬浮服务，后续可按稳定性再升级。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- Manifest 声明悬浮窗权限和悬浮服务。
- 设置页可以引导用户授权悬浮窗。
- 授权后可以开启和关闭悬浮球。
- 点击悬浮球后能弹出悬浮迷你翻译窗。
- 剪贴板为空或系统限制读取时有明确提示。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/floating-translate-design.png`。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已检查 debug merged manifest，确认包含 `SYSTEM_ALERT_WINDOW` 权限和 `FloatingTranslateService`。
- 当前 `adb devices` 无在线设备，未能安装到真机补充悬浮窗授权、悬浮球拖拽和点击翻译验证。

## Task 017：更新项目 README

### 目标

检索当前项目结构、核心代码和历史任务记录，更新根目录 README，使其准确反映当前 App 能力、技术栈、构建方式和验证状态。

### 范围

- 梳理当前 Android 工程结构和关键模块。
- 汇总云端翻译、离线翻译、历史记录、系统划词/分享、剪贴板快捷翻译和悬浮球能力。
- 补充环境要求、构建命令、测试命令和文档入口。
- 明确离线模型不内置 APK，采用首次下载或放置到 App 私有目录的方案。

### 完成标准

- README 不再停留在第一版描述。
- README 能说明当前主要功能、技术栈、项目结构和常用命令。
- README 中的模型、权限、跨 App 入口信息与当前代码一致。
- 完成后至少运行一次文档内容检索，确认关键章节已写入。

### 验证记录

- 已更新根目录 `README.md`。
- 已检索 README 关键章节，确认包含当前能力、技术栈、常用命令、离线模型说明、权限与系统入口、项目结构和当前验证状态。
- 本次仅修改文档，未运行 Android 构建。

## Task 018：Cloudflare R2 模型与更新包分发

### 目标

使用 Cloudflare R2 承载 App 更新包和 Hy-MT 离线模型文件，降低用户下载模型时对 Hugging Face 和代理环境的依赖。

### 范围

- 使用 Wrangler 创建 R2 bucket。
- 上传当前 Q4_K_M GGUF 模型文件。
- 生成并上传模型 SHA256 校验文件和 `models.json` manifest。
- 开启公开访问，优先使用自定义域名；未确定域名时先使用 R2 dev URL 做临时验证。
- 将 App 离线模型下载地址切换到 R2 分发地址。
- 记录 R2 bucket、对象路径、公开访问地址和验证结果。

### 完成标准

- R2 bucket 创建成功。
- 模型文件、SHA256 文件和 manifest 上传成功。
- 公开 URL 可访问 manifest。
- App 中模型下载地址指向 R2 分发地址。
- 单元测试通过。

### 验证记录

- R2 bucket 已创建：`ai-translate-assets`。
- 已开启临时公开访问：`https://pub-e16b86eab02f4594aaa4fd358cf6151e.r2.dev`。
- 已上传 `models/models.json`、`models/HY-MT1.5-1.8B-Q4_K_M.gguf.sha256` 和 `releases/latest.json`。
- Wrangler 单文件上传远端 R2 上限为 300MiB，Q4_K_M 模型已改为 6 个分片上传到 `models/parts/`。
- 公开访问校验通过：manifest 返回 200，6 个分片 HEAD 返回 200，`part00` Range 请求返回 206。
- App 离线模型下载已切换到 R2 分片下载，拼接后校验总大小和 SHA256。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：命令超过 60 秒上限；测试结果 XML 已生成，4 个测试类共 18 个测试均为 0 failure / 0 error。
- 待绑定自定义域名后，将临时 R2 dev URL 替换为正式下载域名。

## Task 019：绑定 R2 正式下载域名

### 目标

将 R2 临时 `r2.dev` 下载地址切换为正式自定义域名 `download.204152.xyz`，用于 App 更新包和离线模型分片分发。

### 范围

- 查询 `204152.xyz` 在 Cloudflare 中的 Zone ID。
- 将 `download.204152.xyz` 绑定到 R2 bucket `ai-translate-assets`。
- 更新 App 里的模型分片基础地址。
- 更新 `docs/r2/models.json` 中的公开下载地址。
- 上传更新后的 `models.json` 到 R2。
- 验证自定义域名下 manifest、分片 HEAD 和 Range 请求可访问。

### 完成标准

- `download.204152.xyz` 绑定成功。
- App 模型下载地址不再使用临时 `r2.dev` 地址。
- R2 manifest 中模型分片 URL 指向正式域名。
- 公开访问验证通过。
- Kotlin 编译通过。

### 验证记录

- `204152.xyz` Zone ID：`fe23f737002048c62ab39874b2b03222`。
- 已绑定 R2 自定义域名：`https://download.204152.xyz`。
- R2 域名状态：`ownership_status = active`，`ssl_status = active`，`min_tls_version = 1.2`。
- 已将 App 模型下载基础地址改为 `https://download.204152.xyz`。
- 已更新并上传 R2 `models/models.json`，分片 URL 均指向正式域名。
- 正式域名访问验证通过：manifest GET 返回 200，6 个模型分片 HEAD 返回 200，`part00` Range 请求返回 206。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
