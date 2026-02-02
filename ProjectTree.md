```
MetaAgent
├─ .claude
│  └─ settings.local.json
├─ app
│  ├─ libs
│  │  ├─ arsc.jar
│  │  └─ ffmpegkit.jar
│  ├─ objectbox-models
│  │  ├─ default.json
│  │  └─ default.json.bak
│  ├─ proguard-rules.pro
│  └─ src
│     ├─ androidTest
│     │  └─ java
│     │     └─ com
│     │        └─ ai
│     │           └─ assistance
│     │              └─ operit
│     │                 ├─ api
│     │                 │  └─ chat
│     │                 │     └─ enhance
│     │                 │        └─ FileBindingServiceTest.kt
│     │                 ├─ core
│     │                 │  └─ workflow
│     │                 │     └─ WorkflowExecutorAndroidTest.kt
│     │                 ├─ ExampleInstrumentedTest.kt
│     │                 └─ util
│     │                    ├─ ColorQrCodeUtilAndroidTest.kt
│     │                    ├─ stream
│     │                    │  ├─ HotStreamAndroidTest.kt
│     │                    │  ├─ plugins
│     │                    │  │  ├─ StreamJsonPluginTest.kt
│     │                    │  │  ├─ StreamMarkdownPluginTest.kt
│     │                    │  │  └─ StreamXmlPluginTest.kt
│     │                    │  ├─ StreamAndroidTest.kt
│     │                    │  ├─ StreamKmpGraphTest.kt
│     │                    │  ├─ StreamRealTimeSplitTest.kt
│     │                    │  ├─ StreamSplitByTest.kt
│     │                    │  └─ StreamTestExtensions.kt
│     │                    └─ SyntaxCheckUtilTest.kt
│     └─ main
│        ├─ aidl
│        │  ├─ android
│        │  │  └─ view
│        │  │     └─ accessibility
│        │  │        └─ AccessibilityEvent.aidl
│        │  └─ com
│        │     └─ ai
│        │        └─ assistance
│        │           └─ operit
│        │              └─ provider
│        │                 ├─ IAccessibilityEventCallback.aidl
│        │                 └─ IAccessibilityProvider.aidl
│        ├─ AndroidManifest.xml
│        ├─ assets
│        │  ├─ accessibility.apk
│        │  ├─ accessibility_version.txt
│        │  ├─ bridge
│        │  │  ├─ index.js
│        │  │  └─ spawn-helper.js
│        │  ├─ computer_desktop
│        │  │  ├─ browser.html
│        │  │  ├─ files.html
│        │  │  └─ index.html
│        │  ├─ desktop.apk
│        │  ├─ desktop_version.txt
│        │  ├─ dragonbones
│        │  │  └─ models
│        │  │     └─ .keep
│        │  ├─ emoji
│        │  │  ├─ angry
│        │  │  │  ├─ angry_1.jpg
│        │  │  │  ├─ angry_2.jpg
│        │  │  │  ├─ angry_3.jpg
│        │  │  │  ├─ angry_4.gif
│        │  │  │  ├─ angry_5.webp
│        │  │  │  └─ angry_6.gif
│        │  │  ├─ confused
│        │  │  │  ├─ confused_1.jpg
│        │  │  │  ├─ confused_2.jpg
│        │  │  │  ├─ confused_3.jpg
│        │  │  │  ├─ confused_4.webp
│        │  │  │  ├─ confused_5.png
│        │  │  │  ├─ confused_6.jpg
│        │  │  │  └─ confused_7.jpg
│        │  │  ├─ crying
│        │  │  │  ├─ crying_1.jpg
│        │  │  │  ├─ crying_2.jpg
│        │  │  │  ├─ crying_3.jpg
│        │  │  │  ├─ crying_4.webp
│        │  │  │  ├─ crying_5.webp
│        │  │  │  └─ crying_6.png
│        │  │  ├─ happy
│        │  │  │  ├─ happy_1.jpg
│        │  │  │  ├─ happy_2.jpg
│        │  │  │  ├─ happy_3.jpg
│        │  │  │  ├─ happy_4.webp
│        │  │  │  └─ happy_5.webp
│        │  │  ├─ like_you
│        │  │  │  ├─ like_you_1.jpg
│        │  │  │  ├─ like_you_2.jpg
│        │  │  │  ├─ like_you_3.jpg
│        │  │  │  ├─ like_you_4.jpg
│        │  │  │  ├─ like_you_5.webp
│        │  │  │  └─ like_you_6.gif
│        │  │  ├─ miss_you
│        │  │  │  ├─ miss_you_1.webp
│        │  │  │  ├─ miss_you_2.webp
│        │  │  │  ├─ miss_you_3.jpg
│        │  │  │  ├─ miss_you_4.jpg
│        │  │  │  ├─ miss_you_5.jpg
│        │  │  │  └─ miss_you_6.jpg
│        │  │  ├─ sad
│        │  │  │  ├─ sad_1.jpg
│        │  │  │  ├─ sad_2.jpg
│        │  │  │  ├─ sad_3.jpg
│        │  │  │  ├─ sad_4.webp
│        │  │  │  ├─ sad_5.gif
│        │  │  │  └─ sad_6.gif
│        │  │  ├─ speechless
│        │  │  │  ├─ speechless_1.jpg
│        │  │  │  ├─ speechless_2.jpg
│        │  │  │  ├─ speechless_3.jpg
│        │  │  │  ├─ speechless_4.webp
│        │  │  │  ├─ speechless_5.jpg
│        │  │  │  └─ speechless_6.jpg
│        │  │  └─ surprised
│        │  │     ├─ surprised_1.webp
│        │  │     ├─ surprised_2.webp
│        │  │     ├─ surprised_3.gif
│        │  │     ├─ surprised_4.jpg
│        │  │     └─ surprised_5.gif
│        │  ├─ jks.jks
│        │  ├─ js
│        │  │  ├─ AndroidUtils.js
│        │  │  ├─ CryptoJS.js
│        │  │  ├─ Jimp.js
│        │  │  ├─ OkHttp3.js
│        │  │  ├─ pako.js
│        │  │  └─ UINode.js
│        │  ├─ logo.svg
│        │  ├─ operit.png
│        │  ├─ operit_shell_exec
│        │  ├─ packages
│        │  │  ├─ 12306.js
│        │  │  ├─ automatic_ui_base.js
│        │  │  ├─ automatic_ui_subagent.js
│        │  │  ├─ code_runner.js
│        │  │  ├─ crossref.js
│        │  │  ├─ daily_life.js
│        │  │  ├─ duckduckgo.js
│        │  │  ├─ extended_file_tools.js
│        │  │  ├─ extended_http_tools.js
│        │  │  ├─ extended_memory_tools.js
│        │  │  ├─ ffmpeg.js
│        │  │  ├─ file_converter.js
│        │  │  ├─ github.js
│        │  │  ├─ google_search.js
│        │  │  ├─ history_chat.js
│        │  │  ├─ nanobanana_draw.js
│        │  │  ├─ openai_draw.js
│        │  │  ├─ qwen_draw.js
│        │  │  ├─ super_admin.js
│        │  │  ├─ system_tools.js
│        │  │  ├─ tasker.js
│        │  │  ├─ tavily.js
│        │  │  ├─ time.js
│        │  │  ├─ various_output.js
│        │  │  ├─ various_search.js
│        │  │  ├─ workflow.js
│        │  │  └─ xai_draw.js
│        │  ├─ pkcs12.keystore
│        │  ├─ README.md
│        │  ├─ shizuku.apk
│        │  ├─ shizuku_version.txt
│        │  ├─ tokenizer.json
│        │  └─ web
│        ├─ cpp
│        │  ├─ CMakeLists.txt
│        │  └─ streamnative
│        │     ├─ HotStream.cpp
│        │     ├─ HotStream.h
│        │     ├─ kmpREADME.md
│        │     ├─ native_markdown_parser.cpp
│        │     ├─ native_markdown_splitter.cpp
│        │     ├─ native_xml_splitter.cpp
│        │     ├─ plugins
│        │     │  ├─ BaseJsonPlugin.cpp
│        │     │  ├─ BaseJsonPlugin.h
│        │     │  ├─ StreamJsonPlugin.cpp
│        │     │  ├─ StreamJsonPlugin.h
│        │     │  ├─ StreamMarkdownPlugin.cpp
│        │     │  ├─ StreamMarkdownPlugin.h
│        │     │  ├─ StreamPlanExecutionPlugin.cpp
│        │     │  ├─ StreamPlanExecutionPlugin.h
│        │     │  ├─ StreamPlugin.h
│        │     │  ├─ StreamPureJsonPlugin.cpp
│        │     │  ├─ StreamPureJsonPlugin.h
│        │     │  ├─ StreamXmlPlugin.cpp
│        │     │  └─ StreamXmlPlugin.h
│        │     ├─ README.md
│        │     ├─ Stream.h
│        │     ├─ StreamBuilders.cpp
│        │     ├─ StreamBuilders.h
│        │     ├─ StreamGroup.h
│        │     ├─ StreamKmpGraph.h
│        │     ├─ StreamKmpMatchResult.h
│        │     ├─ StreamOperators.cpp
│        │     ├─ StreamOperators.h
│        │     ├─ StringExtensions.cpp
│        │     └─ StringExtensions.h
│        ├─ ic_launcher-playstore.png
│        ├─ java
│        │  ├─ com
│        │  │  ├─ ai
│        │  │  │  └─ assistance
│        │  │  │     └─ operit
│        │  │  │        ├─ api
│        │  │  │        │  ├─ chat
│        │  │  │        │  │  ├─ AIForegroundService.kt
│        │  │  │        │  │  ├─ enhance
│        │  │  │        │  │  │  ├─ ConversationMarkupManager.kt
│        │  │  │        │  │  │  ├─ ConversationRoundManager.kt
│        │  │  │        │  │  │  ├─ ConversationService.kt
│        │  │  │        │  │  │  ├─ FileBindingService.kt
│        │  │  │        │  │  │  ├─ InputProcessor.kt
│        │  │  │        │  │  │  ├─ MultiServiceManager.kt
│        │  │  │        │  │  │  ├─ ReferenceManager.kt
│        │  │  │        │  │  │  └─ ToolExecutionManager.kt
│        │  │  │        │  │  ├─ EnhancedAIService.kt
│        │  │  │        │  │  ├─ library
│        │  │  │        │  │  │  ├─ ProblemLibrary.kt
│        │  │  │        │  │  │  └─ ProblemLibraryTool.kt
│        │  │  │        │  │  ├─ llmprovider
│        │  │  │        │  │  │  ├─ AIService.kt
│        │  │  │        │  │  │  ├─ AIServiceFactory.kt
│        │  │  │        │  │  │  ├─ ApiKeyPoolAvailabilityTester.kt
│        │  │  │        │  │  │  ├─ ApiKeyProvider.kt
│        │  │  │        │  │  │  ├─ ClaudeProvider.kt
│        │  │  │        │  │  │  ├─ DeepseekProvider.kt
│        │  │  │        │  │  │  ├─ DoubaoAIProvider.kt
│        │  │  │        │  │  │  ├─ EndpointCompleter.kt
│        │  │  │        │  │  │  ├─ GeminiProvider.kt
│        │  │  │        │  │  │  ├─ ImageLinkParser.kt
│        │  │  │        │  │  │  ├─ LlamaProvider.kt
│        │  │  │        │  │  │  ├─ MediaLinkParser.kt
│        │  │  │        │  │  │  ├─ MistralProvider.kt
│        │  │  │        │  │  │  ├─ MNNProvider.kt
│        │  │  │        │  │  │  ├─ ModelListFetcher.kt
│        │  │  │        │  │  │  ├─ OpenAIProvider.kt
│        │  │  │        │  │  │  └─ QwenAIProvider.kt
│        │  │  │        │  │  └─ plan
│        │  │  │        │  │     ├─ PlanModels.kt
│        │  │  │        │  │     ├─ PlanModeManager.kt
│        │  │  │        │  │     ├─ PlanParser.kt
│        │  │  │        │  │     └─ TaskExecutor.kt
│        │  │  │        │  ├─ speech
│        │  │  │        │  │  ├─ DeepgramSttProvider.kt
│        │  │  │        │  │  ├─ OnnxSileroVad.kt
│        │  │  │        │  │  ├─ OpenAISttProvider.kt
│        │  │  │        │  │  ├─ PersonalWakeEnrollment.kt
│        │  │  │        │  │  ├─ PersonalWakeFeatureExtractor.kt
│        │  │  │        │  │  ├─ PersonalWakeListener.kt
│        │  │  │        │  │  ├─ SherpaMnnSpeechProvider.kt
│        │  │  │        │  │  ├─ SherpaSpeechProvider.kt
│        │  │  │        │  │  ├─ SpeechPrerollStore.kt
│        │  │  │        │  │  ├─ SpeechService.kt
│        │  │  │        │  │  └─ SpeechServiceFactory.kt
│        │  │  │        │  └─ voice
│        │  │  │        │     ├─ AccessibilityVoiceProvider.kt
│        │  │  │        │     ├─ HttpVoiceProvider.kt
│        │  │  │        │     ├─ OpenAIVoiceProvider.kt
│        │  │  │        │     ├─ SiliconFlowVoiceProvider.kt
│        │  │  │        │     ├─ TtsException.kt
│        │  │  │        │     ├─ VoiceListFetcher.kt
│        │  │  │        │     ├─ VoiceService.kt
│        │  │  │        │     └─ VoiceServiceFactory.kt
│        │  │  │        ├─ core
│        │  │  │        │  ├─ application
│        │  │  │        │  │  ├─ ActivityLifecycleManager.kt
│        │  │  │        │  │  ├─ ForegroundServiceCompat.kt
│        │  │  │        │  │  └─ OperitApplication.kt
│        │  │  │        │  ├─ avatar
│        │  │  │        │  │  ├─ common
│        │  │  │        │  │  │  ├─ control
│        │  │  │        │  │  │  │  └─ AvatarController.kt
│        │  │  │        │  │  │  ├─ factory
│        │  │  │        │  │  │  │  ├─ AvatarControllerFactory.kt
│        │  │  │        │  │  │  │  ├─ AvatarModelFactory.kt
│        │  │  │        │  │  │  │  └─ AvatarRendererFactory.kt
│        │  │  │        │  │  │  ├─ model
│        │  │  │        │  │  │  │  ├─ AvatarModel.kt
│        │  │  │        │  │  │  │  ├─ AvatarType.kt
│        │  │  │        │  │  │  │  ├─ IFrameSequenceAvatarModel.kt
│        │  │  │        │  │  │  │  └─ ISkeletalAvatarModel.kt
│        │  │  │        │  │  │  ├─ state
│        │  │  │        │  │  │  │  ├─ AvatarEmotion.kt
│        │  │  │        │  │  │  │  └─ AvatarState.kt
│        │  │  │        │  │  │  └─ view
│        │  │  │        │  │  │     └─ AvatarView.kt
│        │  │  │        │  │  └─ impl
│        │  │  │        │  │     ├─ dragonbones
│        │  │  │        │  │     │  ├─ control
│        │  │  │        │  │     │  │  └─ DragonBonesAvatarController.kt
│        │  │  │        │  │     │  ├─ model
│        │  │  │        │  │     │  │  └─ DragonBonesAvatarModel.kt
│        │  │  │        │  │     │  └─ view
│        │  │  │        │  │     │     └─ DragonBonesRenderer.kt
│        │  │  │        │  │     ├─ factory
│        │  │  │        │  │     │  ├─ AvatarControllerFactoryImpl.kt
│        │  │  │        │  │     │  ├─ AvatarModelFactoryImpl.kt
│        │  │  │        │  │     │  └─ AvatarRendererFactoryImpl.kt
│        │  │  │        │  │     └─ webp
│        │  │  │        │  │        ├─ control
│        │  │  │        │  │        │  └─ WebPAvatarController.kt
│        │  │  │        │  │        ├─ model
│        │  │  │        │  │        │  └─ WebPAvatarModel.kt
│        │  │  │        │  │        └─ view
│        │  │  │        │  │           └─ WebPRenderer.kt
│        │  │  │        │  ├─ chat
│        │  │  │        │  │  └─ AIMessageManager.kt
│        │  │  │        │  ├─ config
│        │  │  │        │  │  ├─ FunctionalPrompts.kt
│        │  │  │        │  │  ├─ SystemPromptConfig.kt
│        │  │  │        │  │  ├─ SystemToolPrompts.kt
│        │  │  │        │  │  └─ SystemToolPromptsInternal.kt
│        │  │  │        │  ├─ subpack
│        │  │  │        │  │  ├─ ApkEditor.kt
│        │  │  │        │  │  ├─ ApkReverseEngineer.kt
│        │  │  │        │  │  ├─ ExeEditor.kt
│        │  │  │        │  │  ├─ ExeIconChanger.kt
│        │  │  │        │  │  └─ KeyStoreHelper.kt
│        │  │  │        │  ├─ tools
│        │  │  │        │  │  ├─ agent
│        │  │  │        │  │  │  ├─ PhoneAgent.kt
│        │  │  │        │  │  │  ├─ PhoneAgentJobRegistry.kt
│        │  │  │        │  │  │  ├─ ShowerBinderReceiver.kt
│        │  │  │        │  │  │  ├─ ShowerBinderRegistry.kt
│        │  │  │        │  │  │  ├─ ShowerController.kt
│        │  │  │        │  │  │  ├─ ShowerServerManager.kt
│        │  │  │        │  │  │  ├─ ShowerVideoRenderer.kt
│        │  │  │        │  │  │  └─ VirtualDisplayManager.kt
│        │  │  │        │  │  ├─ AIToolHandler.kt
│        │  │  │        │  │  ├─ calculator
│        │  │  │        │  │  │  ├─ Calculator.kt
│        │  │  │        │  │  │  ├─ CalculatorTest.kt
│        │  │  │        │  │  │  ├─ ExpressionContext.kt
│        │  │  │        │  │  │  ├─ ExpressionNode.kt
│        │  │  │        │  │  │  ├─ ExpressionParser.kt
│        │  │  │        │  │  │  └─ JsCalculator.kt
│        │  │  │        │  │  ├─ condition
│        │  │  │        │  │  │  └─ ConditionEvaluator.kt
│        │  │  │        │  │  ├─ defaultTool
│        │  │  │        │  │  │  ├─ accessbility
│        │  │  │        │  │  │  │  ├─ AccessibilityDeviceInfoToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ AccessibilityFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ AccessibilitySystemOperationTools.kt
│        │  │  │        │  │  │  │  └─ AccessibilityUITools.kt
│        │  │  │        │  │  │  ├─ admin
│        │  │  │        │  │  │  │  ├─ AdminDeviceInfoToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ AdminFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ AdminSystemOperationTools.kt
│        │  │  │        │  │  │  │  └─ AdminUITools.kt
│        │  │  │        │  │  │  ├─ debugger
│        │  │  │        │  │  │  │  ├─ DebuggerDeviceInfoToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ DebuggerFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ DebuggerSystemOperationTools.kt
│        │  │  │        │  │  │  │  └─ DebuggerUITools.kt
│        │  │  │        │  │  │  ├─ PathValidator.kt
│        │  │  │        │  │  │  ├─ root
│        │  │  │        │  │  │  │  ├─ RootDeviceInfoToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ RootFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ RootSystemOperationTools.kt
│        │  │  │        │  │  │  │  └─ RootUITools.kt
│        │  │  │        │  │  │  ├─ standard
│        │  │  │        │  │  │  │  ├─ LinuxFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ MemoryQueryToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ SafFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ SSHRemoteConnectionTools.kt
│        │  │  │        │  │  │  │  ├─ StandardCalculator.kt
│        │  │  │        │  │  │  │  ├─ StandardChatManagerTool.kt
│        │  │  │        │  │  │  │  ├─ StandardDeviceInfoToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ StandardFFmpegTool.kt
│        │  │  │        │  │  │  │  ├─ StandardFileSystemTools.kt
│        │  │  │        │  │  │  │  ├─ StandardHttpTools.kt
│        │  │  │        │  │  │  │  ├─ StandardIntentToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ StandardSendBroadcastToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ StandardShellToolExecutor.kt
│        │  │  │        │  │  │  │  ├─ StandardSystemOperationTools.kt
│        │  │  │        │  │  │  │  ├─ StandardTerminalCommandExecutor.kt
│        │  │  │        │  │  │  │  ├─ StandardUITools.kt
│        │  │  │        │  │  │  │  ├─ StandardWebVisitTool.kt
│        │  │  │        │  │  │  │  └─ StandardWorkflowTools.kt
│        │  │  │        │  │  │  └─ ToolGetter.kt
│        │  │  │        │  │  ├─ javascript
│        │  │  │        │  │  │  ├─ JsAssetLoader.kt
│        │  │  │        │  │  │  ├─ JsEngine.kt
│        │  │  │        │  │  │  ├─ JsLibraries.kt
│        │  │  │        │  │  │  ├─ JsTimeoutConfig.kt
│        │  │  │        │  │  │  ├─ JsToolManager.kt
│        │  │  │        │  │  │  ├─ JsTools.kt
│        │  │  │        │  │  │  ├─ README.md
│        │  │  │        │  │  │  └─ ScriptExecutionReceiver.kt
│        │  │  │        │  │  ├─ mcp
│        │  │  │        │  │  │  ├─ MCPJson.kt
│        │  │  │        │  │  │  ├─ MCPPackage.kt
│        │  │  │        │  │  │  ├─ MCPServerConfig.kt
│        │  │  │        │  │  │  ├─ MCPTool.kt
│        │  │  │        │  │  │  ├─ MCPToolExecutor.kt
│        │  │  │        │  │  │  └─ MCPToolParameter.kt
│        │  │  │        │  │  ├─ packTool
│        │  │  │        │  │  │  └─ PackageManager.kt
│        │  │  │        │  │  ├─ skill
│        │  │  │        │  │  │  ├─ SkillManager.kt
│        │  │  │        │  │  │  └─ SkillPackage.kt
│        │  │  │        │  │  ├─ system
│        │  │  │        │  │  │  ├─ AccessibilityProviderInstaller.kt
│        │  │  │        │  │  │  ├─ action
│        │  │  │        │  │  │  │  ├─ AccessibilityActionListener.kt
│        │  │  │        │  │  │  │  ├─ ActionListener.kt
│        │  │  │        │  │  │  │  ├─ ActionListenerFactory.kt
│        │  │  │        │  │  │  │  ├─ ActionManager.kt
│        │  │  │        │  │  │  │  ├─ AdminActionListener.kt
│        │  │  │        │  │  │  │  ├─ DebuggerActionListener.kt
│        │  │  │        │  │  │  │  ├─ RootActionListener.kt
│        │  │  │        │  │  │  │  └─ StandardActionListener.kt
│        │  │  │        │  │  │  ├─ AndroidPermissionLevel.kt
│        │  │  │        │  │  │  ├─ AndroidShellExecutor.kt
│        │  │  │        │  │  │  ├─ MediaProjectionCaptureManager.kt
│        │  │  │        │  │  │  ├─ MediaProjectionHolder.kt
│        │  │  │        │  │  │  ├─ OperitTerminalManager.kt
│        │  │  │        │  │  │  ├─ RootAuthorizer.kt
│        │  │  │        │  │  │  ├─ ScreenCaptureActivity.kt
│        │  │  │        │  │  │  ├─ ScreenCaptureService.kt
│        │  │  │        │  │  │  ├─ shell
│        │  │  │        │  │  │  │  ├─ AccessibilityShellExecutor.kt
│        │  │  │        │  │  │  │  ├─ AdminShellExecutor.kt
│        │  │  │        │  │  │  │  ├─ DebuggerShellExecutor.kt
│        │  │  │        │  │  │  │  ├─ RootShellExecutor.kt
│        │  │  │        │  │  │  │  ├─ ShellExecutor.kt
│        │  │  │        │  │  │  │  ├─ ShellExecutorFactory.kt
│        │  │  │        │  │  │  │  └─ StandardShellExecutor.kt
│        │  │  │        │  │  │  ├─ ShizukuAuthorizer.kt
│        │  │  │        │  │  │  ├─ ShizukuInstaller.kt
│        │  │  │        │  │  │  ├─ shower
│        │  │  │        │  │  │  │  └─ OperitShowerShellRunner.kt
│        │  │  │        │  │  │  └─ Terminal.kt
│        │  │  │        │  │  ├─ ToolPackage.kt
│        │  │  │        │  │  ├─ ToolProgressBus.kt
│        │  │  │        │  │  ├─ ToolRegistration.kt
│        │  │  │        │  │  └─ ToolResultDataClasses.kt
│        │  │  │        │  └─ workflow
│        │  │  │        │     ├─ WorkflowExecutor.kt
│        │  │  │        │     ├─ WorkflowScheduler.kt
│        │  │  │        │     ├─ WorkflowSchedulerInitializer.kt
│        │  │  │        │     └─ WorkflowWorker.kt
│        │  │  │        ├─ data
│        │  │  │        │  ├─ api
│        │  │  │        │  │  └─ GitHubApiService.kt
│        │  │  │        │  ├─ backup
│        │  │  │        │  │  ├─ OperitBackupDirs.kt
│        │  │  │        │  │  ├─ RawSnapshotBackupManager.kt
│        │  │  │        │  │  ├─ RoomDatabaseBackupManager.kt
│        │  │  │        │  │  ├─ RoomDatabaseBackupPreferences.kt
│        │  │  │        │  │  ├─ RoomDatabaseBackupRestoreLock.kt
│        │  │  │        │  │  ├─ RoomDatabaseBackupScheduler.kt
│        │  │  │        │  │  ├─ RoomDatabaseBackupWorker.kt
│        │  │  │        │  │  └─ RoomDatabaseRestoreManager.kt
│        │  │  │        │  ├─ converter
│        │  │  │        │  │  ├─ ChatBoxConverter.kt
│        │  │  │        │  │  ├─ ChatFormat.kt
│        │  │  │        │  │  ├─ ChatFormatConverter.kt
│        │  │  │        │  │  ├─ ChatFormatDetector.kt
│        │  │  │        │  │  ├─ ChatGPTConverter.kt
│        │  │  │        │  │  ├─ GenericJsonConverter.kt
│        │  │  │        │  │  └─ MarkdownConverter.kt
│        │  │  │        │  ├─ dao
│        │  │  │        │  │  ├─ ChatDao.kt
│        │  │  │        │  │  ├─ ChatMessageCount.kt
│        │  │  │        │  │  └─ MessageDao.kt
│        │  │  │        │  ├─ db
│        │  │  │        │  │  ├─ AppDatabase.kt
│        │  │  │        │  │  ├─ ObjectBox.kt
│        │  │  │        │  │  ├─ ProblemDao.kt
│        │  │  │        │  │  └─ ProblemEntity.kt
│        │  │  │        │  ├─ exporter
│        │  │  │        │  │  ├─ HtmlExporter.kt
│        │  │  │        │  │  ├─ MarkdownExporter.kt
│        │  │  │        │  │  └─ TextExporter.kt
│        │  │  │        │  ├─ mcp
│        │  │  │        │  │  ├─ MCPLocalServer.kt
│        │  │  │        │  │  ├─ MCPRepository.kt
│        │  │  │        │  │  └─ plugins
│        │  │  │        │  │     ├─ MCPBridge.kt
│        │  │  │        │  │     ├─ MCPBridgeClient.kt
│        │  │  │        │  │     ├─ MCPCommandGenerator.kt
│        │  │  │        │  │     ├─ MCPConfigGenerator.kt
│        │  │  │        │  │     ├─ MCPDeployer.kt
│        │  │  │        │  │     ├─ MCPProjectAnalyzer.kt
│        │  │  │        │  │     ├─ MCPSharedSession.kt
│        │  │  │        │  │     ├─ MCPStarter.kt
│        │  │  │        │  │     └─ ProjectStructure.kt
│        │  │  │        │  ├─ migration
│        │  │  │        │  │  └─ ChatHistoryMigrationManager.kt
│        │  │  │        │  ├─ mnn
│        │  │  │        │  │  └─ MnnModelDownloadManager.kt
│        │  │  │        │  ├─ model
│        │  │  │        │  │  ├─ AiReference.kt
│        │  │  │        │  │  ├─ AITool.kt
│        │  │  │        │  │  ├─ ApiKeyInfo.kt
│        │  │  │        │  │  ├─ AttachmentInfo.kt
│        │  │  │        │  │  ├─ BillingMode.kt
│        │  │  │        │  │  ├─ CharacterCard.kt
│        │  │  │        │  │  ├─ CharacterCardChatStats.kt
│        │  │  │        │  │  ├─ ChatEntity.kt
│        │  │  │        │  │  ├─ ChatHistory.kt
│        │  │  │        │  │  ├─ ChatMessage.kt
│        │  │  │        │  │  ├─ ChunkReference.kt
│        │  │  │        │  │  ├─ CustomEmoji.kt
│        │  │  │        │  │  ├─ DocumentChunk.kt
│        │  │  │        │  │  ├─ DragonBones.kt
│        │  │  │        │  │  ├─ Embedding.kt
│        │  │  │        │  │  ├─ EmbeddingConverter.kt
│        │  │  │        │  │  ├─ FunctionType.kt
│        │  │  │        │  │  ├─ InputProcessingState.kt
│        │  │  │        │  │  ├─ Memory.kt
│        │  │  │        │  │  ├─ MemoryExportModel.kt
│        │  │  │        │  │  ├─ MessageEntity.kt
│        │  │  │        │  │  ├─ ModelConfigData.kt
│        │  │  │        │  │  ├─ ModelParameter.kt
│        │  │  │        │  │  ├─ OpenAIModels.kt
│        │  │  │        │  │  ├─ OperitNodeInfo.kt
│        │  │  │        │  │  ├─ PreferenceProfile.kt
│        │  │  │        │  │  ├─ PromptFunctionType.kt
│        │  │  │        │  │  ├─ PromptProfile.kt
│        │  │  │        │  │  ├─ PromptTag.kt
│        │  │  │        │  │  ├─ SerializableColorScheme.kt
│        │  │  │        │  │  ├─ SerializableTypography.kt
│        │  │  │        │  │  ├─ StandardModelParameters.kt
│        │  │  │        │  │  ├─ ToolPrompt.kt
│        │  │  │        │  │  └─ Workflow.kt
│        │  │  │        │  ├─ preferences
│        │  │  │        │  │  ├─ AgreementPreferences.kt
│        │  │  │        │  │  ├─ AndroidPermissionPreferences.kt
│        │  │  │        │  │  ├─ ApiPreferences.kt
│        │  │  │        │  │  ├─ CharacterCardBilingualData.kt
│        │  │  │        │  │  ├─ CharacterCardManager.kt
│        │  │  │        │  │  ├─ ChatAnnouncementPreferences.kt
│        │  │  │        │  │  ├─ CustomEmojiPreferences.kt
│        │  │  │        │  │  ├─ DisplayPreferencesManager.kt
│        │  │  │        │  │  ├─ EnvPreferences.kt
│        │  │  │        │  │  ├─ FreeUsagePreferences.kt
│        │  │  │        │  │  ├─ FunctionalConfigManager.kt
│        │  │  │        │  │  ├─ GitHubAuthBus.kt
│        │  │  │        │  │  ├─ GitHubAuthPreferences.kt
│        │  │  │        │  │  ├─ ModelConfigManager.kt
│        │  │  │        │  │  ├─ PersonaCardChatHistoryManager.kt
│        │  │  │        │  │  ├─ PromptBilingualData.kt
│        │  │  │        │  │  ├─ PromptPreferencesManager.kt
│        │  │  │        │  │  ├─ PromptTagManager.kt
│        │  │  │        │  │  ├─ PromptVersionManager.kt
│        │  │  │        │  │  ├─ SkillVisibilityPreferences.kt
│        │  │  │        │  │  ├─ SpeechServicesPreferences.kt
│        │  │  │        │  │  ├─ UserPreferencesManager.kt
│        │  │  │        │  │  ├─ WaifuPreferences.kt
│        │  │  │        │  │  └─ WakeWordPreferences.kt
│        │  │  │        │  ├─ repository
│        │  │  │        │  │  ├─ AvatarRepository.kt
│        │  │  │        │  │  ├─ ChatHistoryManager.kt
│        │  │  │        │  │  ├─ CustomEmojiRepository.kt
│        │  │  │        │  │  ├─ MemoryRepository.kt
│        │  │  │        │  │  ├─ UIHierarchyManager.kt
│        │  │  │        │  │  └─ WorkflowRepository.kt
│        │  │  │        │  ├─ skill
│        │  │  │        │  │  └─ SkillRepository.kt
│        │  │  │        │  └─ updates
│        │  │  │        │     ├─ PatchInstallConfirmOverlay.kt
│        │  │  │        │     ├─ PatchInstallReceiver.kt
│        │  │  │        │     ├─ PatchUpdateInstaller.kt
│        │  │  │        │     └─ UpdateManager.kt
│        │  │  │        ├─ integrations
│        │  │  │        │  ├─ intent
│        │  │  │        │  │  └─ ExternalChatReceiver.kt
│        │  │  │        │  └─ tasker
│        │  │  │        │     ├─ AIAgentTasker.kt
│        │  │  │        │     ├─ WorkflowTaskerActivity.kt
│        │  │  │        │     └─ WorkflowTaskerReceiver.kt
│        │  │  │        ├─ provider
│        │  │  │        │  ├─ MemoryDocumentsProvider.kt
│        │  │  │        │  └─ WorkspaceDocumentsProvider.kt
│        │  │  │        ├─ services
│        │  │  │        │  ├─ assistant
│        │  │  │        │  │  ├─ OperitVoiceInteractionService.kt
│        │  │  │        │  │  └─ OperitVoiceInteractionSessionService.kt
│        │  │  │        │  ├─ ChatServiceCore.kt
│        │  │  │        │  ├─ core
│        │  │  │        │  │  ├─ ApiConfigDelegate.kt
│        │  │  │        │  │  ├─ AttachmentDelegate.kt
│        │  │  │        │  │  ├─ ChatHistoryDelegate.kt
│        │  │  │        │  │  ├─ MessageCoordinationDelegate.kt
│        │  │  │        │  │  ├─ MessageProcessingDelegate.kt
│        │  │  │        │  │  └─ TokenStatisticsDelegate.kt
│        │  │  │        │  ├─ EmbeddingService.kt
│        │  │  │        │  ├─ floating
│        │  │  │        │  │  ├─ FloatingWindowManager.kt
│        │  │  │        │  │  ├─ FloatingWindowState.kt
│        │  │  │        │  │  └─ UIDebuggerWindowManager.kt
│        │  │  │        │  ├─ FloatingChatService.kt
│        │  │  │        │  ├─ notification
│        │  │  │        │  │  └─ OperitNotificationListenerService.kt
│        │  │  │        │  ├─ OnnxEmbeddingService.kt
│        │  │  │        │  ├─ ServiceLifecycleOwner.kt
│        │  │  │        │  ├─ TermuxCommandResultService.kt
│        │  │  │        │  └─ UIDebuggerService.kt
│        │  │  │        ├─ ui
│        │  │  │        │  ├─ common
│        │  │  │        │  │  ├─ animations
│        │  │  │        │  │  │  └─ SimpleAnimation.kt
│        │  │  │        │  │  ├─ displays
│        │  │  │        │  │  │  ├─ FpsCounter.kt
│        │  │  │        │  │  │  ├─ LatexCache.kt
│        │  │  │        │  │  │  ├─ MarkdownTextComposable.kt
│        │  │  │        │  │  │  ├─ MessageContentParser.kt
│        │  │  │        │  │  │  ├─ ShowerSurfaceView.kt
│        │  │  │        │  │  │  ├─ UIAutomationProgressOverlay.kt
│        │  │  │        │  │  │  ├─ UIOperationOverlay.kt
│        │  │  │        │  │  │  └─ VirtualDisplayOverlay.kt
│        │  │  │        │  │  ├─ markdown
│        │  │  │        │  │  │  ├─ CanvasMarkdownNodeRenderer.kt
│        │  │  │        │  │  │  ├─ EnhancedCodeBlock.kt
│        │  │  │        │  │  │  ├─ EnhancedTableBlock.kt
│        │  │  │        │  │  │  ├─ MarkdownImageRenderer.kt
│        │  │  │        │  │  │  ├─ MarkdownNodeGrouper.kt
│        │  │  │        │  │  │  ├─ PlanExecutionRenderer.kt
│        │  │  │        │  │  │  └─ StreamMarkdownRenderer.kt
│        │  │  │        │  │  ├─ NavItem.kt
│        │  │  │        │  │  ├─ RememberLocal.kt
│        │  │  │        │  │  └─ WaveVisualizer.kt
│        │  │  │        │  ├─ components
│        │  │  │        │  │  ├─ CustomScaffold.kt
│        │  │  │        │  │  ├─ ErrorDialog.kt
│        │  │  │        │  │  └─ ManagedDragonBonesView.kt
│        │  │  │        │  ├─ error
│        │  │  │        │  │  └─ CrashReportActivity.kt
│        │  │  │        │  ├─ features
│        │  │  │        │  │  ├─ about
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     ├─ AboutScreen.kt
│        │  │  │        │  │  │     └─ OpenSourceLicenses.kt
│        │  │  │        │  │  ├─ agreement
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     └─ AgreementScreen.kt
│        │  │  │        │  │  ├─ announcement
│        │  │  │        │  │  │  └─ ChatBindingAnnouncementDialog.kt
│        │  │  │        │  │  ├─ assistant
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  ├─ AvatarConfigSection.kt
│        │  │  │        │  │  │  │  ├─ AvatarPreviewSection.kt
│        │  │  │        │  │  │  │  ├─ HowToImportSection.kt
│        │  │  │        │  │  │  │  ├─ SettingItem.kt
│        │  │  │        │  │  │  │  └─ VoiceAutoAttachComponents.kt
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  └─ AssistantConfigScreen.kt
│        │  │  │        │  │  │  └─ viewmodel
│        │  │  │        │  │  │     └─ AssistantConfigViewModel.kt
│        │  │  │        │  │  ├─ chat
│        │  │  │        │  │  │  ├─ attachments
│        │  │  │        │  │  │  │  └─ AttachmentUtils.kt
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  ├─ AttachmentPreview.kt
│        │  │  │        │  │  │  │  ├─ attachments
│        │  │  │        │  │  │  │  │  ├─ AttachmentViewerDialog.kt
│        │  │  │        │  │  │  │  │  ├─ AudioAttachmentPlayer.kt
│        │  │  │        │  │  │  │  │  └─ VideoAttachmentPlayer.kt
│        │  │  │        │  │  │  │  ├─ AttachmentSelector.kt
│        │  │  │        │  │  │  │  ├─ CharacterSelectorPanel.kt
│        │  │  │        │  │  │  │  ├─ ChatArea.kt
│        │  │  │        │  │  │  │  ├─ ChatHeader.kt
│        │  │  │        │  │  │  │  ├─ ChatHistorySelector.kt
│        │  │  │        │  │  │  │  ├─ ChatInputSection.kt
│        │  │  │        │  │  │  │  ├─ ChatScreenContent.kt
│        │  │  │        │  │  │  │  ├─ ChatScreenHeader.kt
│        │  │  │        │  │  │  │  ├─ ChatSettingsBar.kt
│        │  │  │        │  │  │  │  ├─ config
│        │  │  │        │  │  │  │  │  └─ TokenInfoDialog.kt
│        │  │  │        │  │  │  │  ├─ ExportDialogs.kt
│        │  │  │        │  │  │  │  ├─ FullscreenInputDialog.kt
│        │  │  │        │  │  │  │  ├─ LinkPreviewDialog.kt
│        │  │  │        │  │  │  │  ├─ MemoryFolderSelectionDialog.kt
│        │  │  │        │  │  │  │  ├─ MessageEditor.kt
│        │  │  │        │  │  │  │  ├─ part
│        │  │  │        │  │  │  │  │  ├─ CustomXmlRenderer.kt
│        │  │  │        │  │  │  │  │  ├─ DetailsTagRenderer.kt
│        │  │  │        │  │  │  │  │  ├─ DialogComponents.kt
│        │  │  │        │  │  │  │  │  ├─ FileDiffDisplay.kt
│        │  │  │        │  │  │  │  │  ├─ FontTagRenderer.kt
│        │  │  │        │  │  │  │  │  ├─ ParamVisualizer.kt
│        │  │  │        │  │  │  │  │  ├─ ThinkToolsXmlNodeGrouper.kt
│        │  │  │        │  │  │  │  │  ├─ ToolDisplayComponents.kt
│        │  │  │        │  │  │  │  │  └─ ToolResultDisplay.kt
│        │  │  │        │  │  │  │  ├─ ReferencesDisplay.kt
│        │  │  │        │  │  │  │  ├─ ScrollToBottomButton.kt
│        │  │  │        │  │  │  │  ├─ SimpleLinearProgressIndicator.kt
│        │  │  │        │  │  │  │  ├─ style
│        │  │  │        │  │  │  │  │  ├─ bubble
│        │  │  │        │  │  │  │  │  │  ├─ BubbleAiMessageComposable.kt
│        │  │  │        │  │  │  │  │  │  ├─ BubbleStyleChatMessage.kt
│        │  │  │        │  │  │  │  │  │  └─ BubbleUserMessageComposable.kt
│        │  │  │        │  │  │  │  │  └─ cursor
│        │  │  │        │  │  │  │  │     ├─ AiMessageComposable.kt
│        │  │  │        │  │  │  │  │     ├─ CursorStyleChatMessage.kt
│        │  │  │        │  │  │  │  │     ├─ SummaryMessageComposable.kt
│        │  │  │        │  │  │  │  │     └─ UserMessageComposable.kt
│        │  │  │        │  │  │  │  └─ WorkspaceChangeConfirmDialog.kt
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  ├─ AIChatScreen.kt
│        │  │  │        │  │  │  │  └─ ConfigurationScreen.kt
│        │  │  │        │  │  │  ├─ util
│        │  │  │        │  │  │  │  ├─ ConfigurationStateHolder.kt
│        │  │  │        │  │  │  │  └─ MessageImageGenerator.kt
│        │  │  │        │  │  │  ├─ viewmodel
│        │  │  │        │  │  │  │  ├─ ChatViewModel.kt
│        │  │  │        │  │  │  │  ├─ FloatingWindowDelegate.kt
│        │  │  │        │  │  │  │  └─ UiStateDelegate.kt
│        │  │  │        │  │  │  └─ webview
│        │  │  │        │  │  │     ├─ computer
│        │  │  │        │  │  │     │  └─ ComputerScreen.kt
│        │  │  │        │  │  │     ├─ LocalWebServer.kt
│        │  │  │        │  │  │     ├─ WebViewHandler.kt
│        │  │  │        │  │  │     ├─ workspace
│        │  │  │        │  │  │     │  ├─ editor
│        │  │  │        │  │  │     │  │  ├─ CodeEditor.kt
│        │  │  │        │  │  │     │  │  ├─ CodeFormatter.kt
│        │  │  │        │  │  │     │  │  ├─ CodePane.kt
│        │  │  │        │  │  │     │  │  ├─ CodeParser.kt
│        │  │  │        │  │  │     │  │  ├─ CodeText.kt
│        │  │  │        │  │  │     │  │  ├─ ColorsText.kt
│        │  │  │        │  │  │     │  │  ├─ completion
│        │  │  │        │  │  │     │  │  │  ├─ CompletionPopup.kt
│        │  │  │        │  │  │     │  │  │  ├─ CompletionProvider.kt
│        │  │  │        │  │  │     │  │  │  ├─ CompletionProviderFactory.kt
│        │  │  │        │  │  │     │  │  │  ├─ DefaultCompletionProvider.kt
│        │  │  │        │  │  │     │  │  │  ├─ HtmlCompletionProvider.kt
│        │  │  │        │  │  │     │  │  │  ├─ JavaScriptCompletionProvider.kt
│        │  │  │        │  │  │     │  │  │  └─ KotlinCompletionProvider.kt
│        │  │  │        │  │  │     │  │  ├─ DpiUtils.kt
│        │  │  │        │  │  │     │  │  ├─ GrammarCheck.kt
│        │  │  │        │  │  │     │  │  ├─ HVScrollView.kt
│        │  │  │        │  │  │     │  │  ├─ language
│        │  │  │        │  │  │     │  │  │  ├─ BaseLanguageSupport.kt
│        │  │  │        │  │  │     │  │  │  ├─ HtmlSupport.kt
│        │  │  │        │  │  │     │  │  │  ├─ JavaScriptSupport.kt
│        │  │  │        │  │  │     │  │  │  ├─ KotlinSupport.kt
│        │  │  │        │  │  │     │  │  │  ├─ LanguageFactory.kt
│        │  │  │        │  │  │     │  │  │  ├─ LanguageSupport.kt
│        │  │  │        │  │  │     │  │  │  └─ LanguageSupportRegistry.kt
│        │  │  │        │  │  │     │  │  ├─ LanguageDetector.kt
│        │  │  │        │  │  │     │  │  ├─ PerformEdit.kt
│        │  │  │        │  │  │     │  │  ├─ StringBuilderMemory.kt
│        │  │  │        │  │  │     │  │  ├─ TextUtil.kt
│        │  │  │        │  │  │     │  │  ├─ theme
│        │  │  │        │  │  │     │  │  │  └─ EditorTheme.kt
│        │  │  │        │  │  │     │  │  └─ TimeDelayer.kt
│        │  │  │        │  │  │     │  ├─ FileManager.kt
│        │  │  │        │  │  │     │  ├─ process
│        │  │  │        │  │  │     │  │  ├─ GitIgnoreFilter.kt
│        │  │  │        │  │  │     │  │  └─ WorkspaceAttachmentProcessor.kt
│        │  │  │        │  │  │     │  ├─ WorkspaceBackupManager.kt
│        │  │  │        │  │  │     │  ├─ WorkspaceConfig.kt
│        │  │  │        │  │  │     │  ├─ WorkspaceManager.kt
│        │  │  │        │  │  │     │  ├─ WorkspaceScreen.kt
│        │  │  │        │  │  │     │  └─ WorkspaceSetup.kt
│        │  │  │        │  │  │     ├─ WorkspaceFileSelector.kt
│        │  │  │        │  │  │     └─ WorkspaceUtils.kt
│        │  │  │        │  │  ├─ demo
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  ├─ DialogComponents.kt
│        │  │  │        │  │  │  │  ├─ PermissionLevelCard.kt
│        │  │  │        │  │  │  │  └─ PermissionStatusItem.kt
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  └─ ShizukuDemoScreen.kt
│        │  │  │        │  │  │  ├─ state
│        │  │  │        │  │  │  │  └─ DemoStateManager.kt
│        │  │  │        │  │  │  ├─ viewmodel
│        │  │  │        │  │  │  │  └─ ShizukuDemoViewModel.kt
│        │  │  │        │  │  │  └─ wizards
│        │  │  │        │  │  │     ├─ AccessibilityWizardCard.kt
│        │  │  │        │  │  │     ├─ OperitTerminalWizardCard.kt
│        │  │  │        │  │  │     ├─ RootWizardCard.kt
│        │  │  │        │  │  │     └─ ShizukuWizardCard.kt
│        │  │  │        │  │  ├─ help
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     └─ HelpScreen.kt
│        │  │  │        │  │  ├─ memory
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  ├─ dialogs
│        │  │  │        │  │  │  │  │  ├─ DocumentViewDialog.kt
│        │  │  │        │  │  │  │  │  ├─ EditMemorySheet.kt
│        │  │  │        │  │  │  │  │  ├─ MemoryDialogs.kt
│        │  │  │        │  │  │  │  │  └─ ToolTestDialog.kt
│        │  │  │        │  │  │  │  ├─ FolderNavigator.kt
│        │  │  │        │  │  │  │  ├─ graph
│        │  │  │        │  │  │  │  │  ├─ GraphVisualizer.kt
│        │  │  │        │  │  │  │  │  └─ model
│        │  │  │        │  │  │  │  │     └─ GraphModels.kt
│        │  │  │        │  │  │  │  ├─ MemoryAppBar.kt
│        │  │  │        │  │  │  │  └─ MemoryScreen.kt
│        │  │  │        │  │  │  └─ viewmodel
│        │  │  │        │  │  │     └─ MemoryViewModel.kt
│        │  │  │        │  │  ├─ migration
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     └─ MigrationScreen.kt
│        │  │  │        │  │  ├─ packages
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  ├─ dialogs
│        │  │  │        │  │  │  │  │  ├─ actions
│        │  │  │        │  │  │  │  │  │  └─ MCPServerDetailsActions.kt
│        │  │  │        │  │  │  │  │  ├─ content
│        │  │  │        │  │  │  │  │  │  ├─ MCPServerConfigContent.kt
│        │  │  │        │  │  │  │  │  │  └─ MCPServerDetailsContent.kt
│        │  │  │        │  │  │  │  │  ├─ header
│        │  │  │        │  │  │  │  │  │  └─ MCPServerDetailsHeader.kt
│        │  │  │        │  │  │  │  │  ├─ MCPServerDetailsDialog.kt
│        │  │  │        │  │  │  │  │  └─ tabs
│        │  │  │        │  │  │  │  │     └─ MCPServerDetailsTabs.kt
│        │  │  │        │  │  │  │  ├─ EmptyState.kt
│        │  │  │        │  │  │  │  ├─ MCPInstallProgressDialog.kt
│        │  │  │        │  │  │  │  ├─ PackageItem.kt
│        │  │  │        │  │  │  │  └─ PackageTab.kt
│        │  │  │        │  │  │  ├─ dialogs
│        │  │  │        │  │  │  │  ├─ AutomationFunctionExecutionDialog.kt
│        │  │  │        │  │  │  │  ├─ AutomationPackageDetailsDialog.kt
│        │  │  │        │  │  │  │  ├─ PackageDetailsDialog.kt
│        │  │  │        │  │  │  │  └─ ScriptExecutionDialog.kt
│        │  │  │        │  │  │  ├─ lists
│        │  │  │        │  │  │  │  └─ PackageLists.kt
│        │  │  │        │  │  │  ├─ model
│        │  │  │        │  │  │  │  └─ MCPCategories.kt
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  ├─ mcp
│        │  │  │        │  │  │  │  │  ├─ components
│        │  │  │        │  │  │  │  │  │  ├─ MCPCommandsEditDialog.kt
│        │  │  │        │  │  │  │  │  │  ├─ MCPDeployConfirmDialog.kt
│        │  │  │        │  │  │  │  │  │  ├─ MCPDeployProgressDialog.kt
│        │  │  │        │  │  │  │  │  │  └─ MCPEnvironmentVariablesDialog.kt
│        │  │  │        │  │  │  │  │  └─ viewmodel
│        │  │  │        │  │  │  │  │     ├─ MCPDeployViewModel.kt
│        │  │  │        │  │  │  │  │     └─ MCPMarketViewModel.kt
│        │  │  │        │  │  │  │  ├─ MCPConfigScreen.kt
│        │  │  │        │  │  │  │  ├─ MCPManageScreen.kt
│        │  │  │        │  │  │  │  ├─ MCPMarketScreen.kt
│        │  │  │        │  │  │  │  ├─ MCPPluginDetailScreen.kt
│        │  │  │        │  │  │  │  ├─ MCPPublishScreen.kt
│        │  │  │        │  │  │  │  ├─ PackageManagerScreen.kt
│        │  │  │        │  │  │  │  ├─ skill
│        │  │  │        │  │  │  │  │  └─ viewmodel
│        │  │  │        │  │  │  │  │     └─ SkillMarketViewModel.kt
│        │  │  │        │  │  │  │  ├─ SkillDetailScreen.kt
│        │  │  │        │  │  │  │  ├─ SkillManagerScreen.kt
│        │  │  │        │  │  │  │  ├─ SkillManageScreen.kt
│        │  │  │        │  │  │  │  ├─ SkillMarketScreen.kt
│        │  │  │        │  │  │  │  └─ SkillPublishScreen.kt
│        │  │  │        │  │  │  ├─ utils
│        │  │  │        │  │  │  │  ├─ MCPPluginParser.kt
│        │  │  │        │  │  │  │  └─ SkillIssueParser.kt
│        │  │  │        │  │  │  └─ viewmodel
│        │  │  │        │  │  │     └─ MCPViewModel.kt
│        │  │  │        │  │  ├─ permission
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  └─ PermissionGuideScreen.kt
│        │  │  │        │  │  │  └─ viewmodel
│        │  │  │        │  │  │     └─ PermissionGuideViewModel.kt
│        │  │  │        │  │  ├─ settings
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  ├─ AvatarPicker.kt
│        │  │  │        │  │  │  │  ├─ BackupCards.kt
│        │  │  │        │  │  │  │  ├─ BackupDialogs.kt
│        │  │  │        │  │  │  │  ├─ BackupManagementCards.kt
│        │  │  │        │  │  │  │  ├─ BackupMemoryImportStrategyStrings.kt
│        │  │  │        │  │  │  │  ├─ BackupOperationTypes.kt
│        │  │  │        │  │  │  │  ├─ CharacterCardAssignDialog.kt
│        │  │  │        │  │  │  │  ├─ CharacterCardDialog.kt
│        │  │  │        │  │  │  │  ├─ ChatStyleOption.kt
│        │  │  │        │  │  │  │  ├─ ColorPickerDialog.kt
│        │  │  │        │  │  │  │  ├─ RoomDbBackupComponents.kt
│        │  │  │        │  │  │  │  └─ ThemeSettingsComponents.kt
│        │  │  │        │  │  │  ├─ screens
│        │  │  │        │  │  │  │  ├─ ChatBackupSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ ChatHistorySettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ ContextSummarySettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ CustomEmojiManagementScreen.kt
│        │  │  │        │  │  │  │  ├─ CustomHeadersSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ FunctionalConfigScreen.kt
│        │  │  │        │  │  │  │  ├─ GitHubAccountScreen.kt
│        │  │  │        │  │  │  │  ├─ GlobalDisplaySettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ LanguageSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ LayoutAdjustmentSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ MnnModelDownloadScreen.kt
│        │  │  │        │  │  │  │  ├─ ModelConfigScreen.kt
│        │  │  │        │  │  │  │  ├─ ModelPromptsSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ PersonaCardGenerationScreen.kt
│        │  │  │        │  │  │  │  ├─ SettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ SpeechServicesSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ TagMarketBilingualData.kt
│        │  │  │        │  │  │  │  ├─ TagMarketScreen.kt
│        │  │  │        │  │  │  │  ├─ ThemeSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ TokenUsageStatisticsComponents.kt
│        │  │  │        │  │  │  │  ├─ TokenUsageStatisticsScreen.kt
│        │  │  │        │  │  │  │  ├─ ToolPermissionSettingsScreen.kt
│        │  │  │        │  │  │  │  ├─ UserPreferencesGuideScreen.kt
│        │  │  │        │  │  │  │  ├─ UserPreferencesSettingsScreen.kt
│        │  │  │        │  │  │  │  └─ WaifuModeSettingsScreen.kt
│        │  │  │        │  │  │  ├─ sections
│        │  │  │        │  │  │  │  ├─ AdvancedSettingsSection.kt
│        │  │  │        │  │  │  │  ├─ ApiKeyVisualTransformation.kt
│        │  │  │        │  │  │  │  ├─ ModelApiSettingsSection.kt
│        │  │  │        │  │  │  │  └─ ModelParametersSection.kt
│        │  │  │        │  │  │  └─ viewmodels
│        │  │  │        │  │  │     └─ CustomEmojiViewModel.kt
│        │  │  │        │  │  ├─ startup
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  └─ AnimatedProgressBar.kt
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     ├─ LocalPluginLoadingState.kt
│        │  │  │        │  │  │     └─ PluginLoadingScreen.kt
│        │  │  │        │  │  ├─ token
│        │  │  │        │  │  │  ├─ components
│        │  │  │        │  │  │  │  └─ UrlConfigDialog.kt
│        │  │  │        │  │  │  ├─ model
│        │  │  │        │  │  │  │  └─ UrlConfig.kt
│        │  │  │        │  │  │  ├─ network
│        │  │  │        │  │  │  │  └─ DeepseekApiConstants.kt
│        │  │  │        │  │  │  ├─ preferences
│        │  │  │        │  │  │  │  └─ UrlConfigManager.kt
│        │  │  │        │  │  │  ├─ TokenConfigWebViewScreen.kt
│        │  │  │        │  │  │  └─ webview
│        │  │  │        │  │  │     ├─ DeepseekJsInterface.kt
│        │  │  │        │  │  │     ├─ JsScripts.kt
│        │  │  │        │  │  │     └─ WebViewConfig.kt
│        │  │  │        │  │  ├─ toolbox
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     ├─ apppermissions
│        │  │  │        │  │  │     │  └─ AppPermissionsScreen.kt
│        │  │  │        │  │  │     ├─ autoglm
│        │  │  │        │  │  │     │  ├─ AutoGlmOneClickToolScreen.kt
│        │  │  │        │  │  │     │  ├─ AutoGlmToolScreen.kt
│        │  │  │        │  │  │     │  ├─ AutoGlmViewModel.kt
│        │  │  │        │  │  │     │  └─ AutoGlmViewModelFactory.kt
│        │  │  │        │  │  │     ├─ defaultassistant
│        │  │  │        │  │  │     │  └─ DefaultAssistantGuideScreen.kt
│        │  │  │        │  │  │     ├─ ffmpegtoolbox
│        │  │  │        │  │  │     │  └─ FFmpegToolboxScreen.kt
│        │  │  │        │  │  │     ├─ filemanager
│        │  │  │        │  │  │     │  ├─ components
│        │  │  │        │  │  │     │  │  ├─ FileActionButton.kt
│        │  │  │        │  │  │     │  │  ├─ FileContextMenu.kt
│        │  │  │        │  │  │     │  │  ├─ FileListContent.kt
│        │  │  │        │  │  │     │  │  ├─ FileListItem.kt
│        │  │  │        │  │  │     │  │  ├─ FileListPane.kt
│        │  │  │        │  │  │     │  │  ├─ LoadingOverlay.kt
│        │  │  │        │  │  │     │  │  ├─ NewFolderDialog.kt
│        │  │  │        │  │  │     │  │  ├─ SearchDialogs.kt
│        │  │  │        │  │  │     │  │  └─ ToolbarComponents.kt
│        │  │  │        │  │  │     │  ├─ FileManagerScreen.kt
│        │  │  │        │  │  │     │  ├─ models
│        │  │  │        │  │  │     │  │  └─ FileModels.kt
│        │  │  │        │  │  │     │  ├─ utils
│        │  │  │        │  │  │     │  │  └─ FileUtils.kt
│        │  │  │        │  │  │     │  └─ viewmodel
│        │  │  │        │  │  │     │     └─ FileManagerViewModel.kt
│        │  │  │        │  │  │     ├─ htmlpackager
│        │  │  │        │  │  │     │  └─ HtmlPackagerScreen.kt
│        │  │  │        │  │  │     ├─ logcat
│        │  │  │        │  │  │     │  ├─ LogcatComponents.kt
│        │  │  │        │  │  │     │  ├─ LogcatManager.kt
│        │  │  │        │  │  │     │  ├─ LogcatScreen.kt
│        │  │  │        │  │  │     │  ├─ LogcatViewModel.kt
│        │  │  │        │  │  │     │  └─ LogModels.kt
│        │  │  │        │  │  │     ├─ processlimit
│        │  │  │        │  │  │     │  └─ ProcessLimitRemoverScreen.kt
│        │  │  │        │  │  │     ├─ ProcessLimitRemoverToolScreen.kt
│        │  │  │        │  │  │     ├─ shellexecutor
│        │  │  │        │  │  │     │  ├─ ShellCommandManager.kt
│        │  │  │        │  │  │     │  └─ ShellExecutorScreen.kt
│        │  │  │        │  │  │     ├─ speechtotext
│        │  │  │        │  │  │     │  ├─ SpeechToTextScreen.kt
│        │  │  │        │  │  │     │  └─ SpeechToTextToolScreen.kt
│        │  │  │        │  │  │     ├─ StreamMarkdownDemo.kt
│        │  │  │        │  │  │     ├─ texttospeech
│        │  │  │        │  │  │     │  ├─ TextToSpeechScreen.kt
│        │  │  │        │  │  │     │  └─ TextToSpeechToolScreen.kt
│        │  │  │        │  │  │     ├─ ToolboxScreen.kt
│        │  │  │        │  │  │     ├─ tooltester
│        │  │  │        │  │  │     │  └─ ToolTesterScreen.kt
│        │  │  │        │  │  │     └─ uidebugger
│        │  │  │        │  │  │        ├─ components
│        │  │  │        │  │  │        │  └─ ActivityMonitorPanel.kt
│        │  │  │        │  │  │        ├─ UIDebuggerComponents.kt
│        │  │  │        │  │  │        ├─ UIDebuggerScreen.kt
│        │  │  │        │  │  │        ├─ UIDebuggerState.kt
│        │  │  │        │  │  │        └─ UIDebuggerViewModel.kt
│        │  │  │        │  │  ├─ update
│        │  │  │        │  │  │  └─ screens
│        │  │  │        │  │  │     ├─ UpdateInfo.kt
│        │  │  │        │  │  │     ├─ UpdateScreen.kt
│        │  │  │        │  │  │     └─ UpdateViewModel.kt
│        │  │  │        │  │  └─ workflow
│        │  │  │        │  │     ├─ components
│        │  │  │        │  │     │  ├─ ConnectionMenu.kt
│        │  │  │        │  │     │  ├─ DraggableNodeCard.kt
│        │  │  │        │  │     │  ├─ GridWorkflowCanvas.kt
│        │  │  │        │  │     │  ├─ NodeActionMenu.kt
│        │  │  │        │  │     │  └─ ScheduleConfigDialog.kt
│        │  │  │        │  │     ├─ screens
│        │  │  │        │  │     │  ├─ WorkflowDetailScreen.kt
│        │  │  │        │  │     │  └─ WorkflowListScreen.kt
│        │  │  │        │  │     └─ viewmodel
│        │  │  │        │  │        └─ WorkflowViewModel.kt
│        │  │  │        │  ├─ floating
│        │  │  │        │  │  ├─ FloatContext.kt
│        │  │  │        │  │  ├─ FloatingChatWindow.kt
│        │  │  │        │  │  ├─ FloatingMode.kt
│        │  │  │        │  │  ├─ FloatingWindowTheme.kt
│        │  │  │        │  │  ├─ ui
│        │  │  │        │  │  │  ├─ ball
│        │  │  │        │  │  │  │  ├─ BallParticles.kt
│        │  │  │        │  │  │  │  ├─ FloatingChatBallMode.kt
│        │  │  │        │  │  │  │  ├─ FloatingResultDisplay.kt
│        │  │  │        │  │  │  │  ├─ FloatingVoiceBallMode.kt
│        │  │  │        │  │  │  │  └─ SiriBall.kt
│        │  │  │        │  │  │  ├─ fullscreen
│        │  │  │        │  │  │  │  ├─ components
│        │  │  │        │  │  │  │  │  ├─ BottomControlBar.kt
│        │  │  │        │  │  │  │  │  ├─ EditPanel.kt
│        │  │  │        │  │  │  │  │  ├─ GlassyChip.kt
│        │  │  │        │  │  │  │  │  ├─ MessageDisplay.kt
│        │  │  │        │  │  │  │  │  └─ WaveVisualizerSection.kt
│        │  │  │        │  │  │  │  ├─ FloatingFullscreenMode.kt
│        │  │  │        │  │  │  │  ├─ screen
│        │  │  │        │  │  │  │  │  └─ FloatingFullscreenScreen.kt
│        │  │  │        │  │  │  │  ├─ viewmodel
│        │  │  │        │  │  │  │  │  └─ FloatingFullscreenModeViewModel.kt
│        │  │  │        │  │  │  │  └─ XmlTextProcessor.kt
│        │  │  │        │  │  │  ├─ pet
│        │  │  │        │  │  │  │  └─ AvatarEmotionManager.kt
│        │  │  │        │  │  │  ├─ screenocr
│        │  │  │        │  │  │  │  ├─ FloatingScreenOcrMode.kt
│        │  │  │        │  │  │  │  └─ screen
│        │  │  │        │  │  │  │     └─ FloatingScreenOcrScreen.kt
│        │  │  │        │  │  │  └─ window
│        │  │  │        │  │  │     ├─ components
│        │  │  │        │  │  │     │  ├─ AttachmentPanel.kt
│        │  │  │        │  │  │     │  ├─ ChatIndicators.kt
│        │  │  │        │  │  │     │  ├─ ChatMessages.kt
│        │  │  │        │  │  │     │  └─ FloatingChatWindowInputControls.kt
│        │  │  │        │  │  │     ├─ models
│        │  │  │        │  │  │     │  └─ ChatModels.kt
│        │  │  │        │  │  │     ├─ screen
│        │  │  │        │  │  │     │  └─ FloatingChatWindowScreen.kt
│        │  │  │        │  │  │     └─ viewmodel
│        │  │  │        │  │  │        └─ FloatingChatWindowViewModel.kt
│        │  │  │        │  │  └─ voice
│        │  │  │        │  │     └─ SpeechInteractionManager.kt
│        │  │  │        │  ├─ main
│        │  │  │        │  │  ├─ components
│        │  │  │        │  │  │  ├─ AppContent.kt
│        │  │  │        │  │  │  ├─ DrawerContent.kt
│        │  │  │        │  │  │  ├─ LocalAppBarContentColor.kt
│        │  │  │        │  │  │  └─ NavigationComponents.kt
│        │  │  │        │  │  ├─ layout
│        │  │  │        │  │  │  ├─ PhoneLayout.kt
│        │  │  │        │  │  │  └─ TabletLayout.kt
│        │  │  │        │  │  ├─ MainActivity.kt
│        │  │  │        │  │  ├─ OperitApp.kt
│        │  │  │        │  │  ├─ screens
│        │  │  │        │  │  │  └─ OperitScreens.kt
│        │  │  │        │  │  └─ SharedFileHandler.kt
│        │  │  │        │  ├─ permissions
│        │  │  │        │  │  ├─ PermissionRequestOverlay.kt
│        │  │  │        │  │  ├─ ToolPermissionDialog.kt
│        │  │  │        │  │  └─ ToolPermissionSystem.kt
│        │  │  │        │  └─ theme
│        │  │  │        │     ├─ Color.kt
│        │  │  │        │     ├─ Theme.kt
│        │  │  │        │     ├─ ThemeUtils.kt
│        │  │  │        │     └─ Type.kt
│        │  │  │        ├─ util
│        │  │  │        │  ├─ AnrMonitor.kt
│        │  │  │        │  ├─ AppLogger.kt
│        │  │  │        │  ├─ ArchiveUtil.kt
│        │  │  │        │  ├─ ChatMarkupRegex.kt
│        │  │  │        │  ├─ ChatUtils.kt
│        │  │  │        │  ├─ ColorQrCodeUtil.kt
│        │  │  │        │  ├─ DocumentConversionUtil.kt
│        │  │  │        │  ├─ exceptions
│        │  │  │        │  │  └─ UserCancellationException.kt
│        │  │  │        │  ├─ FFmpegUtil.kt
│        │  │  │        │  ├─ FileUtils.kt
│        │  │  │        │  ├─ GithubReleaseUtil.kt
│        │  │  │        │  ├─ GlobalExceptionHandler.kt
│        │  │  │        │  ├─ HtmlParserUtil.kt
│        │  │  │        │  ├─ HttpMultiPartDownloader.kt
│        │  │  │        │  ├─ ImageBitmapLimiter.kt
│        │  │  │        │  ├─ ImagePoolManager.kt
│        │  │  │        │  ├─ IntRangeSerializer.kt
│        │  │  │        │  ├─ LocalDateTimeSerializer.kt
│        │  │  │        │  ├─ LocaleUtils.kt
│        │  │  │        │  ├─ LocationUtils.kt
│        │  │  │        │  ├─ markdown
│        │  │  │        │  │  ├─ MarkdownProcessor.kt
│        │  │  │        │  │  └─ SmartString.kt
│        │  │  │        │  ├─ MediaBase64Limiter.kt
│        │  │  │        │  ├─ MediaPoolManager.kt
│        │  │  │        │  ├─ NetworkUtils.kt
│        │  │  │        │  ├─ OCRUtils.kt
│        │  │  │        │  ├─ OperitPaths.kt
│        │  │  │        │  ├─ PathMapper.kt
│        │  │  │        │  ├─ SerializationSetup.kt
│        │  │  │        │  ├─ SkillRepoZipPoolManager.kt
│        │  │  │        │  ├─ stream
│        │  │  │        │  │  ├─ HotStream.kt
│        │  │  │        │  │  ├─ kmpREADME.md
│        │  │  │        │  │  ├─ plugins
│        │  │  │        │  │  │  ├─ BaseJsonPlugin.kt
│        │  │  │        │  │  │  ├─ StreamJsonPlugin.kt
│        │  │  │        │  │  │  ├─ StreamMarkdownPlugin.kt
│        │  │  │        │  │  │  ├─ StreamPlanExecutionPlugin.kt
│        │  │  │        │  │  │  ├─ StreamPlugin.kt
│        │  │  │        │  │  │  ├─ StreamPureJsonPlugin.kt
│        │  │  │        │  │  │  └─ StreamXmlPlugin.kt
│        │  │  │        │  │  ├─ README.md
│        │  │  │        │  │  ├─ Stream.kt
│        │  │  │        │  │  ├─ StreamBuilders.kt
│        │  │  │        │  │  ├─ StreamGroup.kt
│        │  │  │        │  │  ├─ StreamKmpGraph.kt
│        │  │  │        │  │  ├─ StreamKmpMatchResult.kt
│        │  │  │        │  │  ├─ StreamOperators.kt
│        │  │  │        │  │  └─ StringExtensions.kt
│        │  │  │        │  ├─ StreamingJsonXmlConverter.kt
│        │  │  │        │  ├─ streamnative
│        │  │  │        │  │  ├─ NativeMarkdownParser.kt
│        │  │  │        │  │  ├─ NativeMarkdownSplitter.kt
│        │  │  │        │  │  ├─ NativeMarkdownStreamOperators.kt
│        │  │  │        │  │  └─ NativeXmlSplitter.kt
│        │  │  │        │  ├─ SyntaxCheckUtil.kt
│        │  │  │        │  ├─ TextSegmenter.kt
│        │  │  │        │  ├─ TokenCacheManager.kt
│        │  │  │        │  ├─ TtsCleaner.kt
│        │  │  │        │  ├─ UriSerializer.kt
│        │  │  │        │  ├─ vector
│        │  │  │        │  │  ├─ IndexItem.kt
│        │  │  │        │  │  └─ VectorIndexManager.kt
│        │  │  │        │  └─ WaifuMessageProcessor.kt
│        │  │  │        └─ widget
│        │  │  │           ├─ VoiceAssistantGlanceWidget.kt
│        │  │  │           └─ VoiceAssistantWidgetReceiver.kt
│        │  │  └─ k2fsa
│        │  │     └─ sherpa
│        │  │        ├─ mnn
│        │  │        │  ├─ FeatureConfig.kt
│        │  │        │  ├─ OnlineRecognizer.kt
│        │  │        │  ├─ OnlineStream.kt
│        │  │        │  └─ Vad.kt
│        │  │        └─ ncnn
│        │  │           └─ SherpaNcnn.kt
│        │  └─ kotlin
│        │     └─ uuid
│        │        └─ Uuid.kt
│        └─ res
│           ├─ drawable
│           │  ├─ ic_launcher_background.xml
│           │  ├─ ic_launcher_foreground.xml
│           │  ├─ ic_launcher_monochrome.xml
│           │  ├─ ic_microphone.xml
│           │  └─ ic_voice_assistant_widget_preview.xml
│           ├─ mipmap-anydpi-v26
│           │  ├─ ic_launcher.xml
│           │  └─ ic_launcher_round.xml
│           ├─ playstore-icon.png
│           ├─ values
│           │  ├─ colors.xml
│           │  ├─ ic_launcher_background.xml
│           │  ├─ strings.xml
│           │  └─ themes.xml
│           ├─ values-en
│           │  └─ strings.xml
│           ├─ values-es
│           │  └─ strings.xml
│           ├─ values-night
│           │  └─ themes.xml
│           └─ xml
│              ├─ accessibility_service_config.xml
│              ├─ backup_rules.xml
│              ├─ data_extraction_rules.xml
│              ├─ file_paths.xml
│              ├─ interaction_service.xml
│              ├─ locales_config.xml
│              ├─ network_security_config.xml
│              └─ voice_assistant_widget_info.xml
├─ chinese_strings_detailed.txt
├─ docs
│  ├─ assets
│  │  ├─ 41ebc2ec5278bd28e8361e3eb72128d.jpg
│  │  ├─ 73a602a713cff3f840efaba543465b03.png
│  │  ├─ 84ea63a7437eae374f53c5b64f52c24d.png
│  │  ├─ 88a7b7520e4628682a849cc00716c8de.jpg
│  │  ├─ 9036f349c25888d357de5ce34580176d.jpg
│  │  ├─ 9f85b39450c8616909039b66d15a475a.jpg
│  │  ├─ d12038f26df3f814b4e3ce967537f039.jpg
│  │  ├─ deepseek_API_step
│  │  │  ├─ 1.png
│  │  │  ├─ 2.png
│  │  │  ├─ 3.png
│  │  │  ├─ 4.png
│  │  │  ├─ 5.png
│  │  │  └─ 9.png
│  │  ├─ expamle
│  │  │  ├─ 065e5ca8a8036c51a7905d206bbb56c.jpg
│  │  │  ├─ 3ddebdde4958ac152eeca436e39c0f6.jpg
│  │  │  ├─ 5fff4b49db78ec01e189658de8ea997.jpg
│  │  │  ├─ 615cf7a99e421356b6d22bb0b9cc87b.jpg
│  │  │  ├─ 6f81901ae47f5a3584167148017d132.jpg
│  │  │  ├─ 71fd917c5310c1cebaa1abb19882a6d.jpg
│  │  │  ├─ 731f67e3d7494886c1c1f8639216bf2.jpg
│  │  │  ├─ 759d86a7d74351675b32acb6464585d.jpg
│  │  │  ├─ 90a1778510df485d788b80d4bc349f9.jpg
│  │  │  └─ f9b8aeba4878775d1252ad8d5d8620a.jpg
│  │  ├─ floating_and_attach.jpg
│  │  ├─ game_maker_chat.jpg
│  │  ├─ game_maker_packer.jpg
│  │  ├─ game_maker_show.jpg
│  │  ├─ model
│  │  │  ├─ 1.jpg
│  │  │  ├─ 2.jpg
│  │  │  ├─ 3.jpg
│  │  │  └─ 4.jpg
│  │  ├─ package_or_MCP
│  │  │  ├─ 0946d845d9adad20bbd039a93d1196f.jpg
│  │  │  ├─ 1.jpg
│  │  │  ├─ 10.jpg
│  │  │  ├─ 2.jpg
│  │  │  ├─ 3.jpg
│  │  │  ├─ 4.jpg
│  │  │  ├─ 401cda27abf79b9d0311816947b1bdd.jpg
│  │  │  ├─ 5.jpg
│  │  │  ├─ 7.jpg
│  │  │  ├─ 7b8ec8ba567c3c670d6a063121614fe.jpg
│  │  │  ├─ 8.jpg
│  │  │  └─ 9.jpg
│  │  ├─ teach_step
│  │  │  ├─ 1-1.png
│  │  │  ├─ 1-2.png
│  │  │  ├─ 1-3.jpg
│  │  │  └─ 1-4.jpg
│  │  ├─ user_step
│  │  │  ├─ step_for_frist_1.jpg
│  │  │  ├─ step_for_frist_2.jpg
│  │  │  ├─ step_for_frist_3.jpg
│  │  │  └─ step_for_frist_4.jpg
│  │  ├─ webdev
│  │  │  ├─ 519137715fc99270d97fd42086119b5.jpg
│  │  │  ├─ 6b0f3650dd4c5709069d2e4201d3cb9.jpg
│  │  │  ├─ 9e43331c5f055b1bd82cd0f7d74704d.jpg
│  │  │  └─ c851e530a258bbbbf41f87dcb907b14.png
│  │  └─ web_developer.jpg
│  ├─ BUILDING.md
│  ├─ chat_import_markdown.md
│  ├─ chat_import_markdown_example.md
│  ├─ CONTRIBUTING.md
│  ├─ deepseek_thinking.md
│  ├─ DEFAULT_TOOLS_ARCH.md
│  ├─ external_intent_chat.md
│  ├─ package_dev
│  │  ├─ android.md
│  │  ├─ core.md
│  │  ├─ cryptojs.md
│  │  ├─ ffmpeg.md
│  │  ├─ files.md
│  │  ├─ index.md
│  │  ├─ jimp.md
│  │  ├─ memory.md
│  │  ├─ network.md
│  │  ├─ okhttp.md
│  │  ├─ results.md
│  │  ├─ system.md
│  │  ├─ tasker.md
│  │  ├─ tool-types.md
│  │  ├─ ui.md
│  │  └─ workflow.md
│  ├─ RENDERER_ARCH.md
│  ├─ SCRIPT_DEV_GUIDE.md
│  ├─ tool_permissions.md
│  └─ workflow_intent_trigger.md
├─ dragonbones
│  ├─ CMakeLists.txt
│  ├─ cpp
│  │  ├─ dragonBones
│  │  │  ├─ animation
│  │  │  │  ├─ Animation.cpp
│  │  │  │  ├─ Animation.h
│  │  │  │  ├─ AnimationState.cpp
│  │  │  │  ├─ AnimationState.h
│  │  │  │  ├─ BaseTimelineState.cpp
│  │  │  │  ├─ BaseTimelineState.h
│  │  │  │  ├─ IAnimatable.h
│  │  │  │  ├─ TimelineState.cpp
│  │  │  │  ├─ TimelineState.h
│  │  │  │  ├─ WorldClock.cpp
│  │  │  │  └─ WorldClock.h
│  │  │  ├─ armature
│  │  │  │  ├─ Armature.cpp
│  │  │  │  ├─ Armature.h
│  │  │  │  ├─ Bone.cpp
│  │  │  │  ├─ Bone.h
│  │  │  │  ├─ Constraint.cpp
│  │  │  │  ├─ Constraint.h
│  │  │  │  ├─ DeformVertices.cpp
│  │  │  │  ├─ DeformVertices.h
│  │  │  │  ├─ IArmatureProxy.h
│  │  │  │  ├─ Slot.cpp
│  │  │  │  ├─ Slot.h
│  │  │  │  ├─ TransformObject.cpp
│  │  │  │  └─ TransformObject.h
│  │  │  ├─ core
│  │  │  │  ├─ BaseObject.cpp
│  │  │  │  ├─ BaseObject.h
│  │  │  │  ├─ DragonBones.cpp
│  │  │  │  └─ DragonBones.h
│  │  │  ├─ DragonBonesHeaders.h
│  │  │  ├─ event
│  │  │  │  ├─ EventObject.cpp
│  │  │  │  ├─ EventObject.h
│  │  │  │  └─ IEventDispatcher.h
│  │  │  ├─ factory
│  │  │  │  ├─ BaseFactory.cpp
│  │  │  │  └─ BaseFactory.h
│  │  │  ├─ geom
│  │  │  │  ├─ ColorTransform.h
│  │  │  │  ├─ Matrix.h
│  │  │  │  ├─ Point.cpp
│  │  │  │  ├─ Point.h
│  │  │  │  ├─ Rectangle.h
│  │  │  │  ├─ Transform.cpp
│  │  │  │  └─ Transform.h
│  │  │  ├─ model
│  │  │  │  ├─ AnimationConfig.cpp
│  │  │  │  ├─ AnimationConfig.h
│  │  │  │  ├─ AnimationData.cpp
│  │  │  │  ├─ AnimationData.h
│  │  │  │  ├─ ArmatureData.cpp
│  │  │  │  ├─ ArmatureData.h
│  │  │  │  ├─ BoundingBoxData.cpp
│  │  │  │  ├─ BoundingBoxData.h
│  │  │  │  ├─ CanvasData.cpp
│  │  │  │  ├─ CanvasData.h
│  │  │  │  ├─ ConstraintData.cpp
│  │  │  │  ├─ ConstraintData.h
│  │  │  │  ├─ DisplayData.cpp
│  │  │  │  ├─ DisplayData.h
│  │  │  │  ├─ DragonBonesData.cpp
│  │  │  │  ├─ DragonBonesData.h
│  │  │  │  ├─ SkinData.cpp
│  │  │  │  ├─ SkinData.h
│  │  │  │  ├─ TextureAtlasData.cpp
│  │  │  │  ├─ TextureAtlasData.h
│  │  │  │  ├─ UserData.cpp
│  │  │  │  └─ UserData.h
│  │  │  └─ parser
│  │  │     ├─ BinaryDataParser.cpp
│  │  │     ├─ BinaryDataParser.h
│  │  │     ├─ DataParser.cpp
│  │  │     ├─ DataParser.h
│  │  │     ├─ JSONDataParser.cpp
│  │  │     └─ JSONDataParser.h
│  │  ├─ JniBridge.cpp
│  │  ├─ JniBridge.h
│  │  ├─ opengl
│  │  │  ├─ OpenGLFactory.cpp
│  │  │  ├─ OpenGLFactory.h
│  │  │  ├─ OpenGLSlot.cpp
│  │  │  └─ OpenGLSlot.h
│  │  ├─ rapidjson
│  │  │  ├─ allocators.h
│  │  │  ├─ document.h
│  │  │  ├─ encodedstream.h
│  │  │  ├─ encodings.h
│  │  │  ├─ error
│  │  │  │  ├─ en.h
│  │  │  │  └─ error.h
│  │  │  ├─ filereadstream.h
│  │  │  ├─ filewritestream.h
│  │  │  ├─ fwd.h
│  │  │  ├─ internal
│  │  │  │  ├─ biginteger.h
│  │  │  │  ├─ diyfp.h
│  │  │  │  ├─ dtoa.h
│  │  │  │  ├─ ieee754.h
│  │  │  │  ├─ itoa.h
│  │  │  │  ├─ meta.h
│  │  │  │  ├─ pow10.h
│  │  │  │  ├─ regex.h
│  │  │  │  ├─ stack.h
│  │  │  │  ├─ strfunc.h
│  │  │  │  ├─ strtod.h
│  │  │  │  └─ swap.h
│  │  │  ├─ istreamwrapper.h
│  │  │  ├─ memorybuffer.h
│  │  │  ├─ memorystream.h
│  │  │  ├─ msinttypes
│  │  │  │  ├─ inttypes.h
│  │  │  │  └─ stdint.h
│  │  │  ├─ ostreamwrapper.h
│  │  │  ├─ pointer.h
│  │  │  ├─ prettywriter.h
│  │  │  ├─ rapidjson.h
│  │  │  ├─ reader.h
│  │  │  ├─ schema.h
│  │  │  ├─ stream.h
│  │  │  ├─ stringbuffer.h
│  │  │  └─ writer.h
│  │  └─ thirdParty
│  │     └─ stb
│  │        └─ stb_image.h
│  ├─ README.md
│  └─ src
│     └─ main
│        ├─ AndroidManifest.xml
│        └─ java
│           └─ com
│              └─ dragonbones
│                 ├─ DragonBonesController.kt
│                 ├─ DragonBonesView.kt
│                 └─ JniBridge.kt
├─ examples
│  ├─ .env.local.example
│  ├─ 12306.js
│  ├─ 12306.ts
│  ├─ ai_chat.js
│  ├─ ai_chat.ts
│  ├─ automatic_baidu_map_assistant.js
│  ├─ automatic_baidu_map_assistant.ts
│  ├─ automatic_bilibili_assistant.js
│  ├─ automatic_bilibili_assistant.ts
│  ├─ automatic_ui_base.js
│  ├─ automatic_ui_base.ts
│  ├─ automatic_ui_subagent.js
│  ├─ automatic_ui_subagent.ts
│  ├─ automatic_xiaohongshu_assistant.js
│  ├─ automatic_xiaohongshu_assistant.ts
│  ├─ baidu_map.js
│  ├─ baidu_map.ts
│  ├─ beeimg_image_uploader.js
│  ├─ beeimg_image_uploader.ts
│  ├─ bilibili_tools.js
│  ├─ bilibili_tools.ts
│  ├─ code_runner.js
│  ├─ code_runner.ts
│  ├─ crossref.js
│  ├─ crossref.ts
│  ├─ daily_life.js
│  ├─ daily_life.ts
│  ├─ douyin_download.js
│  ├─ douyin_download.ts
│  ├─ duckduckgo.js
│  ├─ duckduckgo.ts
│  ├─ extended_file_tools.js
│  ├─ extended_file_tools.ts
│  ├─ extended_http_tools.js
│  ├─ extended_http_tools.ts
│  ├─ extended_memory_tools.js
│  ├─ extended_memory_tools.ts
│  ├─ ffmpeg.js
│  ├─ ffmpeg.ts
│  ├─ file_converter.js
│  ├─ file_converter.ts
│  ├─ github
│  │  ├─ package.json
│  │  ├─ src
│  │  │  ├─ github
│  │  │  │  ├─ api.js
│  │  │  │  ├─ api.ts
│  │  │  │  ├─ branches.js
│  │  │  │  ├─ branches.ts
│  │  │  │  ├─ contents.js
│  │  │  │  ├─ contents.ts
│  │  │  │  ├─ issues.js
│  │  │  │  ├─ issues.ts
│  │  │  │  ├─ patch.js
│  │  │  │  ├─ patch.ts
│  │  │  │  ├─ pulls.js
│  │  │  │  ├─ pulls.ts
│  │  │  │  ├─ repos.js
│  │  │  │  └─ repos.ts
│  │  │  ├─ index.js
│  │  │  ├─ index.ts
│  │  │  ├─ local
│  │  │  │  ├─ fileApply.js
│  │  │  │  ├─ fileApply.ts
│  │  │  │  ├─ terminal.js
│  │  │  │  └─ terminal.ts
│  │  │  ├─ tools.js
│  │  │  ├─ tools.ts
│  │  │  └─ utils
│  │  │     ├─ base64.js
│  │  │     ├─ base64.ts
│  │  │     ├─ wrap.js
│  │  │     └─ wrap.ts
│  │  └─ tsconfig.json
│  ├─ github.js
│  ├─ google_search.js
│  ├─ google_search.ts
│  ├─ history_chat.js
│  ├─ history_chat.ts
│  ├─ jmcomic.js
│  ├─ jmcomic.ts
│  ├─ nanobanana_draw.js
│  ├─ nanobanana_draw.ts
│  ├─ network_test.js
│  ├─ network_test.ts
│  ├─ openai_draw.js
│  ├─ openai_draw.ts
│  ├─ operit-tester.js
│  ├─ operit-tester.ts
│  ├─ qq_intelligent.js
│  ├─ qq_intelligent.ts
│  ├─ quick_start.js
│  ├─ quick_start.ts
│  ├─ qwen_draw.js
│  ├─ qwen_draw.ts
│  ├─ reader.js
│  ├─ reader.ts
│  ├─ README.md
│  ├─ super_admin.js
│  ├─ super_admin.ts
│  ├─ system_tools.js
│  ├─ system_tools.ts
│  ├─ tasker.js
│  ├─ tasker.ts
│  ├─ tavily.js
│  ├─ tavily.ts
│  ├─ time.js
│  ├─ time.ts
│  ├─ tsconfig.json
│  ├─ types
│  │  ├─ android.d.ts
│  │  ├─ chat.d.ts
│  │  ├─ core.d.ts
│  │  ├─ cryptojs.d.ts
│  │  ├─ ffmpeg.d.ts
│  │  ├─ files.d.ts
│  │  ├─ index.d.ts
│  │  ├─ jimp.d.ts
│  │  ├─ memory.d.ts
│  │  ├─ network.d.ts
│  │  ├─ okhttp.d.ts
│  │  ├─ pako.d.ts
│  │  ├─ results.d.ts
│  │  ├─ system.d.ts
│  │  ├─ tasker.d.ts
│  │  ├─ tool-types.d.ts
│  │  ├─ ui.d.ts
│  │  └─ workflow.d.ts
│  ├─ various_output.js
│  ├─ various_output.ts
│  ├─ various_search.js
│  ├─ various_search.ts
│  ├─ workflow.js
│  ├─ workflow.ts
│  ├─ xai_draw.js
│  └─ xai_draw.ts
├─ gradle
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
├─ LICENSE
├─ llama
│  ├─ CMakeLists.txt
│  ├─ consumer-rules.pro
│  └─ src
│     └─ main
│        ├─ AndroidManifest.xml
│        ├─ cpp
│        │  └─ llama_jni_stub.cpp
│        └─ java
│           └─ com
│              └─ ai
│                 └─ assistance
│                    └─ llama
│                       ├─ LlamaLibraryLoader.kt
│                       ├─ LlamaNative.kt
│                       └─ LlamaSession.kt
├─ mnn
│  ├─ CMakeLists.txt
│  ├─ consumer-rules.pro
│  ├─ proguard-rules.pro
│  └─ src
│     └─ main
│        ├─ AndroidManifest.xml
│        ├─ cpp
│        │  ├─ mnnllmnative.cpp
│        │  ├─ mnnmodulennative.cpp
│        │  ├─ mnnnetnative.cpp
│        │  └─ mnnportraitnative.cpp
│        ├─ java
│        │  └─ com
│        │     └─ ai
│        │        └─ assistance
│        │           └─ mnn
│        │              ├─ MNNForwardType.kt
│        │              ├─ MNNImageProcess.kt
│        │              ├─ MNNLibraryLoader.kt
│        │              ├─ MNNLlmNative.kt
│        │              ├─ MNNLlmSession.kt
│        │              ├─ MNNModule.kt
│        │              ├─ MNNModuleNative.kt
│        │              ├─ MNNNetInstance.kt
│        │              └─ MNNNetNative.kt
│        └─ jniLibs
│           └─ arm64-v8a
│              └─ libsherpa-mnn-jni.so
├─ package.json
├─ packages_whitelist.txt
├─ README(E).md
├─ README.md
├─ showerclient
│  ├─ README.md
│  └─ src
│     └─ main
│        ├─ AndroidManifest.xml
│        ├─ assets
│        │  └─ shower-server.jar
│        └─ java
│           └─ com
│              └─ ai
│                 └─ assistance
│                    ├─ shower
│                    │  ├─ IShowerService.java
│                    │  ├─ IShowerVideoSink.java
│                    │  └─ ShowerBinderContainer.java
│                    └─ showerclient
│                       ├─ ShellEnvironment.kt
│                       ├─ ShowerBinderRegistry.kt
│                       ├─ ShowerController.kt
│                       ├─ ShowerServerManager.kt
│                       ├─ ShowerVideoRenderer.kt
│                       └─ ui
│                          └─ ShowerSurfaceView.kt
├─ sync_example_packages.py
└─ tools
   ├─ desktop
   │  ├─ app
   │  │  ├─ proguard-rules.pro
   │  │  └─ src
   │  │     ├─ androidTest
   │  │     │  └─ java
   │  │     │     └─ com
   │  │     │        └─ ai
   │  │     │           └─ assistance
   │  │     │              └─ operit
   │  │     │                 └─ desktop
   │  │     │                    └─ ExampleInstrumentedTest.kt
   │  │     ├─ main
   │  │     │  ├─ AndroidManifest.xml
   │  │     │  ├─ java
   │  │     │  │  └─ com
   │  │     │  │     └─ ai
   │  │     │  │        └─ assistance
   │  │     │  │           └─ operit
   │  │     │  │              └─ desktop
   │  │     │  │                 ├─ MainActivity.kt
   │  │     │  │                 └─ ui
   │  │     │  │                    └─ theme
   │  │     │  │                       ├─ Color.kt
   │  │     │  │                       ├─ Theme.kt
   │  │     │  │                       └─ Type.kt
   │  │     │  └─ res
   │  │     │     ├─ drawable
   │  │     │     │  ├─ ic_launcher_background.xml
   │  │     │     │  ├─ ic_launcher_foreground.xml
   │  │     │     │  └─ ic_launcher_monochrome.xml
   │  │     │     ├─ mipmap-anydpi-v26
   │  │     │     │  ├─ ic_launcher.xml
   │  │     │     │  └─ ic_launcher_round.xml
   │  │     │     ├─ mipmap-hdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-mdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-xhdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-xxhdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-xxxhdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ values
   │  │     │     │  ├─ colors.xml
   │  │     │     │  ├─ strings.xml
   │  │     │     │  └─ themes.xml
   │  │     │     └─ xml
   │  │     │        ├─ backup_rules.xml
   │  │     │        └─ data_extraction_rules.xml
   │  │     └─ test
   │  │        └─ java
   │  │           └─ com
   │  │              └─ ai
   │  │                 └─ assistance
   │  │                    └─ operit
   │  │                       └─ desktop
   │  │                          └─ ExampleUnitTest.kt
   │  ├─ gradle
   │  │  ├─ libs.versions.toml
   │  │  └─ wrapper
   │  │     ├─ gradle-wrapper.jar
   │  │     └─ gradle-wrapper.properties
   │  ├─ gradle.properties
   │  ├─ gradlew
   │  └─ gradlew.bat
   ├─ execute_js.bat
   ├─ execute_js.sh
   ├─ github
   │  ├─ commit_ai.py
   │  ├─ import_anthropic_skills.py
   │  ├─ issues_open.md
   │  ├─ issues_open_ai.md
   │  ├─ list_issues.py
   │  └─ __pycache__
   │     └─ import_anthropic_skills.cpython-312.pyc
   ├─ hotbuild
   │  ├─ apply_patch.py
   │  ├─ install_from_adb.bat
   │  └─ nightly_auto.py
   ├─ JS_ADB_README.md
   ├─ kill_shower_server.bat
   ├─ mcp_bridge
   │  ├─ index.ts
   │  ├─ package.json
   │  ├─ README.md
   │  ├─ spawn-helper.ts
   │  └─ tsconfig.json
   ├─ run_shower_server.bat
   ├─ shell_identity_launcher
   │  ├─ CMakeLists.txt
   │  ├─ native-lib.cpp
   │  └─ push_android.bat
   ├─ shower
   │  ├─ app
   │  │  ├─ proguard-rules.pro
   │  │  └─ src
   │  │     ├─ androidTest
   │  │     │  └─ java
   │  │     │     └─ com
   │  │     │        └─ ai
   │  │     │           └─ assistance
   │  │     │              └─ shower
   │  │     │                 └─ ExampleInstrumentedTest.kt
   │  │     ├─ main
   │  │     │  ├─ AndroidManifest.xml
   │  │     │  ├─ java
   │  │     │  │  └─ com
   │  │     │  │     └─ ai
   │  │     │  │        └─ assistance
   │  │     │  │           └─ shower
   │  │     │  │              ├─ InputController.java
   │  │     │  │              ├─ IShowerService.java
   │  │     │  │              ├─ IShowerVideoSink.java
   │  │     │  │              ├─ Main.java
   │  │     │  │              ├─ MainActivity.kt
   │  │     │  │              ├─ shell
   │  │     │  │              │  ├─ FakeContext.java
   │  │     │  │              │  └─ Workarounds.java
   │  │     │  │              ├─ ShowerBinderContainer.java
   │  │     │  │              ├─ ui
   │  │     │  │              │  └─ theme
   │  │     │  │              │     ├─ Color.kt
   │  │     │  │              │     ├─ Theme.kt
   │  │     │  │              │     └─ Type.kt
   │  │     │  │              └─ wrappers
   │  │     │  │                 ├─ ActivityManager.java
   │  │     │  │                 ├─ ServiceManager.java
   │  │     │  │                 └─ WindowManager.java
   │  │     │  └─ res
   │  │     │     ├─ drawable
   │  │     │     │  ├─ ic_launcher_background.xml
   │  │     │     │  └─ ic_launcher_foreground.xml
   │  │     │     ├─ mipmap-anydpi-v26
   │  │     │     │  ├─ ic_launcher.xml
   │  │     │     │  └─ ic_launcher_round.xml
   │  │     │     ├─ mipmap-hdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-mdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-xhdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-xxhdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ mipmap-xxxhdpi
   │  │     │     │  ├─ ic_launcher.webp
   │  │     │     │  └─ ic_launcher_round.webp
   │  │     │     ├─ values
   │  │     │     │  ├─ colors.xml
   │  │     │     │  ├─ strings.xml
   │  │     │     │  └─ themes.xml
   │  │     │     └─ xml
   │  │     │        ├─ backup_rules.xml
   │  │     │        └─ data_extraction_rules.xml
   │  │     └─ test
   │  │        └─ java
   │  │           └─ com
   │  │              └─ ai
   │  │                 └─ assistance
   │  │                    └─ shower
   │  │                       └─ ExampleUnitTest.kt
   │  ├─ gradle
   │  │  ├─ libs.versions.toml
   │  │  └─ wrapper
   │  │     ├─ gradle-wrapper.jar
   │  │     └─ gradle-wrapper.properties
   │  ├─ gradle.properties
   │  ├─ gradlew
   │  └─ gradlew.bat
   ├─ shower_ws_client.py
   └─ string
      ├─ add_string.py
      ├─ check_strings.py
      ├─ count_ui_strings.py
      └─ search_string.py

```