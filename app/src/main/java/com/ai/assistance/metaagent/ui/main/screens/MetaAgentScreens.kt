package com.ai.assistance.metaagent.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ai.assistance.metaagent.R
import com.ai.assistance.metaagent.ui.common.NavItem
import com.ai.assistance.metaagent.ui.features.about.screens.AboutScreen
import com.ai.assistance.metaagent.ui.features.assistant.screens.AssistantConfigScreen
import com.ai.assistance.metaagent.ui.features.chat.screens.AIChatScreen
import com.ai.assistance.metaagent.ui.features.home.screens.MetaAgentHomeScreen
import com.ai.assistance.metaagent.ui.features.home.screens.CourseSpaceScreen
import com.ai.assistance.metaagent.ui.features.home.screens.FlashReviewScreen
import com.ai.assistance.metaagent.ui.features.home.screens.CourseDetailScreen
import com.ai.assistance.metaagent.ui.features.home.screens.TaskCenterScreen
import com.ai.assistance.metaagent.ui.features.home.screens.TaskDetailScreen
import com.ai.assistance.metaagent.ui.features.home.screens.CrossDeviceExecutionScreen
import com.ai.assistance.metaagent.ui.features.home.data.CourseRagChatBindingStore
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import com.ai.assistance.metaagent.ui.features.home.data.toMetaConversation
import com.ai.assistance.metaagent.ui.features.demo.screens.ShizukuDemoScreen
import com.ai.assistance.metaagent.ui.features.help.screens.HelpScreen
import com.ai.assistance.metaagent.ui.features.memory.screens.MemoryScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.PackageManagerScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.MCPMarketScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.MCPManageScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.MCPPublishScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.MCPPluginDetailScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.SkillDetailScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.SkillMarketScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.SkillManageScreen
import com.ai.assistance.metaagent.ui.features.packages.screens.SkillPublishScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ChatBackupSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ChatHistorySettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ContextSummarySettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.FunctionalConfigScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.GlobalDisplaySettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.GitHubAccountScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.LanguageSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.LayoutAdjustmentSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ModelConfigScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ModelPromptsSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.PcAgentConnectionSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.RemoteControlSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.TagMarketScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.SettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.SpeechServicesSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ThemeSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.ToolPermissionSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.UserPreferencesGuideScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.UserPreferencesSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.CustomHeadersSettingsScreen
import com.ai.assistance.metaagent.ui.features.settings.screens.TokenUsageStatisticsScreen
import com.ai.assistance.metaagent.ui.features.token.TokenConfigWebViewScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.AppPermissionsToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.FileManagerToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.LogcatToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.ShellExecutorToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.StreamMarkdownDemoScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.TerminalAutoConfigToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.TerminalToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.ToolboxScreen
import com.ai.assistance.metaagent.ui.common.composedsl.ToolPkgComposeDslToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.UIDebuggerToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.DefaultAssistantGuideToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.ProcessLimitRemoverToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.sqlviewer.SqlViewerToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.ffmpegtoolbox.FFmpegToolboxScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.htmlpackager.HtmlPackagerScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.speechtotext.SpeechToTextToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.texttospeech.TextToSpeechToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.tooltester.ToolTesterScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.autoglm.AutoGlmOneClickToolScreen
import com.ai.assistance.metaagent.ui.features.toolbox.screens.autoglm.AutoGlmToolScreen
import com.ai.assistance.metaagent.ui.features.update.screens.UpdateScreen
import com.ai.assistance.metaagent.ui.features.workflow.screens.WorkflowListScreen
import com.ai.assistance.metaagent.ui.features.workflow.screens.WorkflowDetailScreen
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.ai.assistance.metaagent.data.model.ChatMessage
import com.ai.assistance.metaagent.data.preferences.GitHubAuthPreferences
import com.ai.assistance.metaagent.data.preferences.UserPreferencesManager
import com.ai.assistance.metaagent.data.repository.ChatHistoryManager
import kotlinx.coroutines.launch

// 璺敱閰嶇疆绫?
typealias ScreenNavigationHandler = (Screen) -> Unit

typealias NavItemChangeHandler = (NavItem) -> Unit

// 閲嶆瀯鐨凷creen绫伙紝娣诲姞浜嗚矾鐢辩浉鍏冲睘鎬у拰鍐呭娓叉煋鍑芥暟
sealed class Screen(
        // 鎸囧畾鐖跺睆骞曪紝鐢ㄤ簬杩斿洖瀵艰埅
        open val parentScreen: Screen? = null,
        // 瀵瑰簲鐨勫鑸」锛岀敤浜庝晶杈规爮楂樹寒鏄剧ず
        open val navItem: NavItem? = null,
        // 灞忓箷鏍囬璧勬簮ID
        open val titleRes: Int? = null
) {
    // 灞忓箷鍐呭娓叉煋鍑芥暟
    @Composable
    open fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
    ) {
        // 瀛愮被瀹炵幇鍏蜂綋鍐呭
    }

    // Main screens (primary)
    data object MetaHome : Screen(navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            // 浠?CompositionLocal 鑾峰彇鎶藉眽鎵撳紑鍥炶皟
            val openDrawer = com.ai.assistance.metaagent.ui.main.components.LocalDrawerOpener.current
            val context = LocalContext.current
            val chatHistoryManager = com.ai.assistance.metaagent.data.repository.ChatHistoryManager.getInstance(context)
            val displayPreferencesManager = com.ai.assistance.metaagent.data.preferences.DisplayPreferencesManager.getInstance(context)
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            // 收集用户全局头像 URI
            val globalUserAvatarUriString by displayPreferencesManager.globalUserAvatarUri.collectAsState(initial = null)
            val avatarUri = remember(globalUserAvatarUriString) {
                globalUserAvatarUriString?.let { android.net.Uri.parse(it) }
            }

            // 收集真实聊天历史
            val chatHistories by chatHistoryManager.chatHistoriesFlow.collectAsState(initial = emptyList())

            var lastMessagePreviews by remember { mutableStateOf(mapOf<String, String>()) }
            LaunchedEffect(chatHistories) {
                val previews = mutableMapOf<String, String>()
                for (chat in chatHistories) {
                    try {
                        val lastMessages = chatHistoryManager.loadChatMessages(chat.id, order = "desc", limit = 1)
                        previews[chat.id] = lastMessages.firstOrNull()?.content?.take(80)?.replace("\n", " ") ?: ""
                    } catch (_: Exception) {
                        previews[chat.id] = ""
                    }
                }
                lastMessagePreviews = previews
            }

            val conversations = remember(chatHistories, lastMessagePreviews) {
                chatHistories.map { chat ->
                    chat.toMetaConversation(lastMessagePreview = lastMessagePreviews[chat.id] ?: "")
                }
            }

            MetaAgentHomeScreen(
                    conversations = conversations,
                    avatarUri = avatarUri,
                    onConversationClick = { chatId ->
                        scope.launch {
                            chatHistoryManager.setCurrentChatId(chatId)
                        }
                        navigateTo(AiChat)
                        updateNavItem(NavItem.AiChat)
                    },
                    onOpenFlashReview = {
                        navigateTo(FlashReview)
                        updateNavItem(NavItem.MetaHome)
                    },
                    onMenuClick = openDrawer,
                    onDeleteChat = { chatId ->
                        scope.launch {
                            chatHistoryManager.deleteChatHistory(chatId)
                        }
                    },
                    onBottomNavClick = { route ->
                        when (route) {
                            "meta_home" -> { /* already here */ }
                            "course_space" -> {
                                navigateTo(CourseSpace)
                                updateNavItem(NavItem.MetaHome)
                            }
                            "task_center" -> {
                                navigateTo(TaskCenter)
                                updateNavItem(NavItem.MetaHome)
                            }
                            "ai_chat" -> {
                                scope.launch {
                                    UserPreferencesManager.getInstance(context)
                                        .saveThemeSettings(customChatTitle = "")
                                }
                                navigateTo(AiChat)
                                updateNavItem(NavItem.AiChat)
                            }
                            "assistant_config" -> {
                                navigateTo(AssistantConfig)
                                updateNavItem(NavItem.AssistantConfig)
                            }
                            "toolbox" -> {
                                navigateTo(Toolbox)
                                updateNavItem(NavItem.Toolbox)
                            }
                            "packages" -> {
                                navigateTo(Packages)
                                updateNavItem(NavItem.Packages)
                            }
                            "memory_base" -> {
                                navigateTo(MemoryBase)
                                updateNavItem(NavItem.MemoryBase)
                            }
                            "shizuku_commands" -> {
                                navigateTo(ShizukuCommands)
                                updateNavItem(NavItem.ShizukuCommands)
                            }
                            "settings" -> {
                                navigateTo(Settings)
                                updateNavItem(NavItem.Settings)
                            }
                            "about" -> {
                                navigateTo(About)
                                updateNavItem(NavItem.About)
                            }
                            else -> {
                                // MoreBottomSheet etc
                                navigateTo(AiChat)
                                updateNavItem(NavItem.AiChat)
                            }
                        }
                    },
                    onNewChatClick = {
                        scope.launch {
                            UserPreferencesManager.getInstance(context)
                                .saveThemeSettings(customChatTitle = "")
                        }
                        navigateTo(AiChat)
                        updateNavItem(NavItem.AiChat)
                    },
                    onAvatarClick = {
                        navigateTo(UserProfile)
                        updateNavItem(NavItem.MetaHome)
                    }
            )
        }
    }

    data object UserProfile : Screen(navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            val context = LocalContext.current
            val displayPreferencesManager = com.ai.assistance.metaagent.data.preferences.DisplayPreferencesManager.getInstance(context)
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            // 收集当前已保存的头像 URI
            val globalUserAvatarUriString by displayPreferencesManager.globalUserAvatarUri.collectAsState(initial = null)
            val avatarUri = remember(globalUserAvatarUriString) {
                globalUserAvatarUriString?.let { android.net.Uri.parse(it) }
            }

            com.ai.assistance.metaagent.ui.features.home.screens.UserProfileScreen(
                    onClose = onGoBack,
                    avatarUri = avatarUri,
                    onAvatarChanged = { croppedUri ->
                        scope.launch {
                            try {
                                // 将裁剪结果复制到 app 内部私有目录，避免缓存文件被清理
                                val destFile = java.io.File(
                                    context.filesDir,
                                    "user_avatar_${System.currentTimeMillis()}.jpg"
                                )
                                context.contentResolver.openInputStream(croppedUri)?.use { input ->
                                    destFile.outputStream().use { output -> input.copyTo(output) }
                                }
                                // 删除旧头像文件（避免无限累积）
                                globalUserAvatarUriString?.let { oldUriStr ->
                                    val oldFile = java.io.File(android.net.Uri.parse(oldUriStr).path ?: "")
                                    if (oldFile.exists() && oldFile.absolutePath.startsWith(context.filesDir.absolutePath)) {
                                        oldFile.delete()
                                    }
                                }
                                // 持久化保存到 DataStore
                                displayPreferencesManager.saveDisplaySettings(
                                    globalUserAvatarUri = android.net.Uri.fromFile(destFile).toString()
                                )
                            } catch (e: Exception) {
                                com.ai.assistance.metaagent.util.AppLogger.e("UserProfile", "保存头像失败", e)
                            }
                        }
                    },
                    onNavigate = { route ->
                        when (route) {
                            "settings" -> { navigateTo(Settings); updateNavItem(NavItem.Settings) }
                            else -> { /* TODO */ }
                        }
                    }
            )
        }
    }

    data object CourseSpace : Screen(navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            CourseSpaceScreen(
                    onCourseClick = { courseId ->
                        navigateTo(CourseDetail(courseId))
                    }
            )
        }
    }

    data object FlashReview : Screen(navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            FlashReviewScreen(
                onGoBack = onGoBack,
                onOpenReviewDemo = { /* TODO */ },
                onOpenCourseDemo = { navigateTo(CourseSpace) },
                onOpenTaskDemo = { navigateTo(TaskCenter) }
            )
        }
    }

    data object TaskCenter : Screen(navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TaskCenterScreen(
                    onTaskClick = { taskId ->
                        navigateTo(TaskDetail(taskId))
                    },
                    onCrossDeviceClick = {
                        navigateTo(CrossDeviceExecution)
                    }
            )
        }
    }

    data object CrossDeviceExecution : Screen(parentScreen = TaskCenter, navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            CrossDeviceExecutionScreen()
        }

        @Composable
        override fun getTitle(): String = "跨端执行"
    }

        data class CourseDetail(val courseId: String) :
            Screen(parentScreen = CourseSpace, navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            CourseDetailScreen(
                    courseId = courseId,
                    onAskAiClick = { targetCourseId, courseName ->
                        scope.launch {
                            val chatHistoryManager = ChatHistoryManager.getInstance(context)
                            val preferencesManager = UserPreferencesManager.getInstance(context)

                            val newChat = chatHistoryManager.createNewChat(setAsCurrentChat = true)
                            chatHistoryManager.updateChatTitle(newChat.id, "$courseName · 课程问答")
                            CourseRagChatBindingStore.bindChatToCourse(
                                context = context,
                                chatId = newChat.id,
                                courseId = targetCourseId,
                                courseName = courseName,
                                folderPath = StudyModuleStore.getCourseRagFolderPath(targetCourseId),
                                boundAt = System.currentTimeMillis().toString()
                            )
                            chatHistoryManager.addMessage(
                                newChat.id,
                                ChatMessage(
                                    sender = "ai",
                                    content = "已进入《${courseName}》课程问答，课程知识库已加载。你可以直接提问重点、实验或章节内容。"
                                )
                            )
                            preferencesManager.saveThemeSettings(customChatTitle = "课程问答")

                            navigateTo(AiChat)
                            updateNavItem(NavItem.AiChat)
                        }
                    },
                    onDeleteCourse = onGoBack
            )
        }

        @Composable
        override fun getTitle(): String = "课程详情"
    }

    data class TaskDetail(val taskId: String) :
            Screen(parentScreen = TaskCenter, navItem = NavItem.MetaHome) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TaskDetailScreen(taskId = taskId)
        }

        @Composable
        override fun getTitle(): String = "任务详情"
    }

    data object AiChat : Screen(navItem = NavItem.AiChat) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            AIChatScreen(
                    padding = PaddingValues(0.dp),
                    viewModel = null,
                    isFloatingMode = false,
                    hasBackgroundImage = hasBackgroundImage,
                    onNavigateToTokenConfig = { navigateTo(TokenConfig) },
                    onNavigateToSettings = {
                        navigateTo(Settings)
                        updateNavItem(NavItem.Settings)
                    },
                    onNavigateToUserPreferences = { navigateTo(UserPreferencesSettings) },
                    onNavigateToModelConfig = { navigateTo(ModelConfig) },
                    onNavigateToModelPrompts = { navigateTo(ModelPromptsSettings) },
                    onNavigateToPackageManager = { navigateTo(Packages) },
                    onLoading = onLoading,
                    onError = onError,
                    onGestureConsumed = onGestureConsumed
            )
        }
    }

    data object MemoryBase : Screen(navItem = NavItem.MemoryBase, titleRes = R.string.screen_title_memory_base) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            MemoryScreen()
        }
    }

    data object Packages : Screen(navItem = NavItem.Packages) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            PackageManagerScreen(
                onNavigateToMCPMarket = { navigateTo(MCPMarket) },
                onNavigateToSkillMarket = { navigateTo(SkillMarket) }
            )
        }
    }

    data object SkillMarket : Screen(parentScreen = Packages, navItem = NavItem.Packages, titleRes = R.string.screen_title_skill_market) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SkillMarketScreen(
                onNavigateBack = onGoBack,
                onNavigateToPublish = { navigateTo(SkillPublish) },
                onNavigateToManage = { navigateTo(SkillManage) },
                onNavigateToDetail = { issue ->
                    navigateTo(SkillDetail(issue))
                }
            )
        }
    }

    data object SkillManage : Screen(parentScreen = SkillMarket, navItem = NavItem.Packages, titleRes = R.string.screen_title_skill_manage) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SkillManageScreen(
                onNavigateBack = onGoBack,
                onNavigateToEdit = { issue ->
                    navigateTo(SkillEdit(issue))
                },
                onNavigateToPublish = { navigateTo(SkillPublish) },
                onNavigateToDetail = { issue ->
                    navigateTo(SkillDetail(issue))
                }
            )
        }
    }

    data object SkillPublish : Screen(parentScreen = SkillMarket, navItem = NavItem.Packages, titleRes = R.string.screen_title_skill_publish) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SkillPublishScreen(onNavigateBack = onGoBack)
        }
    }

    data class SkillEdit(val editingIssue: com.ai.assistance.metaagent.data.api.GitHubIssue) :
            Screen(parentScreen = SkillMarket, navItem = NavItem.Packages, titleRes = R.string.screen_title_skill_publish) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SkillPublishScreen(
                onNavigateBack = onGoBack,
                editingIssue = editingIssue
            )
        }
    }

    data class SkillDetail(val issue: com.ai.assistance.metaagent.data.api.GitHubIssue) :
            Screen(parentScreen = SkillMarket, navItem = NavItem.Packages) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SkillDetailScreen(
                issue = issue,
                onNavigateBack = onGoBack
            )
        }
    }

    data object MCPMarket : Screen(parentScreen = Packages, navItem = NavItem.Packages, titleRes = R.string.screen_title_mcp_market) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            MCPMarketScreen(
                onNavigateBack = onGoBack,
                onNavigateToPublish = { navigateTo(MCPPublish) },
                onNavigateToManage = { navigateTo(MCPManage) },
                onNavigateToDetail = { issue ->
                    navigateTo(MCPPluginDetail(issue))
                }
            )
        }
    }

    data object MCPPublish : Screen(parentScreen = MCPMarket, navItem = NavItem.Packages, titleRes = R.string.screen_title_mcp_publish) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            MCPPublishScreen(onNavigateBack = onGoBack)
        }
    }

    data object MCPManage : Screen(parentScreen = MCPMarket, navItem = NavItem.Packages, titleRes = R.string.screen_title_mcp_manage) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            MCPManageScreen(
                onNavigateBack = onGoBack,
                onNavigateToEdit = { issue ->
                    navigateTo(MCPEditPlugin(issue))
                },
                onNavigateToPublish = { navigateTo(MCPPublish) }
            )
        }
    }

    data class MCPEditPlugin(val editingIssue: com.ai.assistance.metaagent.data.api.GitHubIssue) : Screen(parentScreen = MCPManage, navItem = NavItem.Packages, titleRes = R.string.screen_title_mcp_publish) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            MCPPublishScreen(
                onNavigateBack = onGoBack,
                editingIssue = editingIssue
            )
        }
    }

    data object Toolbox : Screen(navItem = NavItem.Toolbox) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ToolboxScreen(
                    navController = navController,
                    onFileManagerSelected = { navigateTo(FileManager) },
                    onTerminalSelected = { navigateTo(Terminal) },
                    onAppPermissionsSelected = { navigateTo(AppPermissions) },
                    onUIDebuggerSelected = { navigateTo(UIDebugger) },
                    onFFmpegToolboxSelected = { navigateTo(FFmpegToolbox) },
                    onShellExecutorSelected = { navigateTo(ShellExecutor) },
                    onLogcatSelected = { navigateTo(Logcat) },
                    onTextToSpeechSelected = { navigateTo(TextToSpeech) },
                    onSpeechToTextSelected = { navigateTo(SpeechToText) },
                    onToolTesterSelected = { navigateTo(ToolTester) },
                    onAgreementSelected = { navigateTo(Agreement) },
                    onDefaultAssistantGuideSelected = { navigateTo(DefaultAssistantGuide) },
                    onProcessLimitRemoverSelected = { navigateTo(ProcessLimitRemover) },
                    onHtmlPackagerSelected = { navigateTo(HtmlPackager) },
                    onAutoGlmOneClickSelected = { navigateTo(AutoGlmOneClick) },
                    onAutoGlmToolSelected = { navigateTo(AutoGlmTool) },
                    onSqlViewerSelected = { navigateTo(SqlViewer) },
                    onTokenConfigSelected = { navigateTo(TokenConfig) },
                    onToolPkgComposeDslSelected = { containerPackageName, uiModuleId, title ->
                        navigateTo(
                            ToolPkgComposeDsl(
                                containerPackageName = containerPackageName,
                                uiModuleId = uiModuleId,
                                title = title
                            )
                        )
                    }
            )
        }
    }


    data object ShizukuCommands : Screen(navItem = NavItem.ShizukuCommands) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ShizukuDemoScreen(navigateTo = navigateTo)
        }
    }

    data object Settings : Screen(navItem = NavItem.Settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SettingsScreen(
                    navigateToToolPermissions = { navigateTo(ToolPermission) },
                    navigateToPcAgentConnection = { navigateTo(PcAgentConnection) },
                    navigateToRemoteControl = { navigateTo(RemoteControl) },
                    onNavigateToUserPreferences = { navigateTo(UserPreferencesSettings) },
                    navigateToGitHubAccount = { navigateTo(GitHubAccount) },
                    navigateToModelConfig = { navigateTo(ModelConfig) },
                    navigateToThemeSettings = { navigateTo(ThemeSettings) },
                    navigateToGlobalDisplaySettings = { navigateTo(GlobalDisplaySettings) },
                    navigateToModelPrompts = { navigateTo(ModelPromptsSettings) },
                    navigateToFunctionalConfig = { navigateTo(FunctionalConfig) },
                    navigateToChatHistorySettings = { navigateTo(ChatHistorySettings) },
                    navigateToChatBackupSettings = { navigateTo(ChatBackupSettings) },
                    navigateToLanguageSettings = { navigateTo(LanguageSettings) },
                    navigateToSpeechServicesSettings = { navigateTo(SpeechServicesSettings) },
                    navigateToCustomHeadersSettings = { navigateTo(CustomHeadersSettings) },
                    navigateToPersonaCardGeneration = { navigateTo(PersonaCardGeneration) },
                    navigateToWaifuModeSettings = { navigateTo(WaifuModeSettings) },
                    navigateToTokenUsageStatistics = { navigateTo(TokenUsageStatistics) },
                    navigateToContextSummarySettings = { navigateTo(ContextSummarySettings) },
                    navigateToLayoutAdjustmentSettings = { navigateTo(LayoutAdjustmentSettings) }
            )
        }
    }

    data object RemoteControl :
        Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_remote_control) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            RemoteControlSettingsScreen(onBackPressed = onGoBack)
        }
    }

    data object PcAgentConnection :
        Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.settings_remote_control) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            PcAgentConnectionSettingsScreen(onBackPressed = onGoBack)
        }

        @Composable
        override fun getTitle(): String = "PC Agent 编排连接"
    }

    data object GitHubAccount : Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.github_account) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            val context = LocalContext.current
            val githubAuth = GitHubAuthPreferences.getInstance(context)

            fun initiateGitHubLogin() {
                val authUrl = githubAuth.getAuthorizationUrl()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            GitHubAccountScreen(
                onLogin = ::initiateGitHubLogin
            )
        }
    }

    data object Help : Screen(navItem = NavItem.Help) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            HelpScreen(onBackPressed = onGoBack)
        }
    }

    data object About : Screen(navItem = NavItem.About) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            AboutScreen(
                navigateToUpdateHistory = {
                    navigateTo(UpdateHistory)
                }
            )
        }
    }

    data object Agreement : Screen(navItem = NavItem.Agreement) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            com.ai.assistance.metaagent.ui.features.agreement.screens.AgreementScreen(
                    onAgreementAccepted = onGoBack
            )
        }
    }

    data object UpdateHistory : Screen(navItem = NavItem.UpdateHistory) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            UpdateScreen(onNavigateToThemeSettings = { navigateTo(ThemeSettings) })
        }
    }

    data object AssistantConfig : Screen(navItem = NavItem.AssistantConfig) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            AssistantConfigScreen()
        }
    }

    data object Workflow : Screen(navItem = NavItem.Workflow) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            WorkflowListScreen(
                onNavigateToDetail = { workflowId ->
                    navigateTo(WorkflowDetail(workflowId))
                }
            )
        }
    }

    data class WorkflowDetail(val workflowId: String) : Screen(parentScreen = Workflow, navItem = NavItem.Workflow, titleRes = R.string.nav_workflow) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            WorkflowDetailScreen(
                workflowId = workflowId,
                onNavigateBack = onGoBack
            )
        }
    }

    data object TokenConfig : Screen(parentScreen = AiChat, navItem = NavItem.TokenConfig) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TokenConfigWebViewScreen(onNavigateBack = onGoBack)
        }
    }

    // Secondary screens - Settings
    data object ToolPermission :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_tool_permissions) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ToolPermissionSettingsScreen(navigateBack = onGoBack)
        }
    }

    data class UserPreferencesGuide(var profileName: String = "", var profileId: String = "") :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_user_preferences_guide) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            UserPreferencesGuideScreen(
                    profileName = profileName,
                    profileId = profileId,
                    onComplete = onGoBack,
                    navigateToPermissions = {
                        navigateTo(ShizukuCommands)
                        updateNavItem(NavItem.ShizukuCommands)
                    }
            )
        }
    }

    data object UserPreferencesSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_user_preferences_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            UserPreferencesSettingsScreen(
                    onNavigateBack = onGoBack,
                    onNavigateToGuide = { profileName, profileId ->
                        navigateTo(UserPreferencesGuide(profileName, profileId))
                    }
            )
        }
    }

    data object ModelConfig :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_model_config) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ModelConfigScreen(
                onBackPressed = onGoBack,
                navigateToMnnModelDownload = null
            )
        }
    }
    // 娣诲姞SpeechServicesSettings灞忓箷瀹氫箟
    data object SpeechServicesSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_speech_services_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SpeechServicesSettingsScreen(
                onBackPressed = onGoBack,
                onNavigateToTextToSpeech = { navigateTo(TextToSpeech) }
            )
        }
    }
    
    // 娣诲姞鑷畾涔夎姹傚ご璁剧疆灞忓箷
    data object CustomHeadersSettings :
        Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_custom_headers_settings) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            CustomHeadersSettingsScreen(onBackPressed = onGoBack)
        }
    }
    // 鏂板锛氫汉璁惧崱鐢熸垚椤甸潰
    data object PersonaCardGeneration :
        Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_persona_card_generation) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            com.ai.assistance.metaagent.ui.features.settings.screens.PersonaCardGenerationScreen(
                onNavigateToSettings = { navigateTo(Settings) },
                onNavigateToUserPreferences = { navigateTo(UserPreferencesSettings) },
                onNavigateToModelConfig = { navigateTo(ModelConfig) },
                onNavigateToModelPrompts = { navigateTo(ModelPromptsSettings) }
            )
        }
    }

    // 鏂板锛歐aifu妯″紡璁剧疆椤甸潰
    data object WaifuModeSettings :
        Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_waifu_mode_settings) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            com.ai.assistance.metaagent.ui.features.settings.screens.WaifuModeSettingsScreen(
                onNavigateBack = onGoBack,
                onNavigateToCustomEmoji = { navigateTo(CustomEmojiManagement) }
            )
        }
    }
    
    // 鑷畾涔夎〃鎯呯鐞嗛〉闈?
    data object CustomEmojiManagement :
        Screen(parentScreen = WaifuModeSettings, navItem = NavItem.Settings, titleRes = R.string.manage_custom_emoji) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            com.ai.assistance.metaagent.ui.features.settings.screens.CustomEmojiManagementScreen(
                onNavigateBack = onGoBack
            )
        }
    }
    
    data object TagMarket :
        Screen(parentScreen = ModelPromptsSettings, navItem = NavItem.Settings, titleRes = R.string.screen_title_tag_market) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TagMarketScreen(onBackPressed = onGoBack)
        }
    }

    data object ModelPromptsSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_model_prompts_settings) {
                @Composable
                override fun Content(
                    navController: NavController,
                    navigateTo: ScreenNavigationHandler,
                    updateNavItem: NavItemChangeHandler,
                    onGoBack: () -> Unit,
                    hasBackgroundImage: Boolean,
                    onLoading: (Boolean) -> Unit,
                    onError: (String) -> Unit,
                    onGestureConsumed: (Boolean) -> Unit
                    ) {
                        ModelPromptsSettingsScreen(
                            onBackPressed = onGoBack,
                            onNavigateToMarket = { navigateTo(TagMarket) },
                            onNavigateToPersonaGeneration = { navigateTo(PersonaCardGeneration) },
                            onNavigateToChatManagement = { navigateTo(ChatHistorySettings) }
                        )
                        }
                    }
                    
                    data object FunctionalConfig :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_functional_config) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            FunctionalConfigScreen(
                    onBackPressed = onGoBack,
                    onNavigateToModelConfig = { navigateTo(ModelConfig) }
            )
        }
    }

    data object ThemeSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_theme_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ThemeSettingsScreen()
        }
    }

    data object GlobalDisplaySettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_global_display_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            GlobalDisplaySettingsScreen(onBackPressed = onGoBack)
        }
    }

    data object LayoutAdjustmentSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_layout_adjustment) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            LayoutAdjustmentSettingsScreen(onNavigateBack = onGoBack)
        }
    }

    data object ChatHistorySettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_chat_history_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ChatHistorySettingsScreen()
        }
    }

    data object ChatBackupSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_chat_backup_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ChatBackupSettingsScreen()
        }
    }

    data object LanguageSettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_language_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            LanguageSettingsScreen(onBackPressed = onGoBack)
        }
    }

    data object TokenUsageStatistics :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.settings_token_usage_stats) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TokenUsageStatisticsScreen(onBackPressed = onGoBack)
        }
    }

    data object ContextSummarySettings :
            Screen(parentScreen = Settings, navItem = NavItem.Settings, titleRes = R.string.screen_title_context_summary_settings) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ContextSummarySettingsScreen(onBackPressed = onGoBack)
        }
    }

    data class ToolPkgComposeDsl(
        val containerPackageName: String,
        val uiModuleId: String,
        val title: String
    ) : Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ToolPkgComposeDslToolScreen(
                navController = navController,
                containerPackageName = containerPackageName,
                uiModuleId = uiModuleId,
                fallbackTitle = title
            )
        }

        @Composable
        override fun getTitle(): String = title
    }

    // Toolbox secondary screens

    data object FileManager :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_file_manager) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            FileManagerToolScreen(navController = navController)
        }
    }

    data object Terminal :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_terminal) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TerminalToolScreen(navController = navController)
        }
    }

    data object TerminalSetup :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_terminal) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TerminalToolScreen(navController = navController, forceShowSetup = true)
        }
    }

    data object TerminalAutoConfig :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_terminal_auto_config) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TerminalAutoConfigToolScreen(navController = navController)
        }
    }

    data object AppPermissions :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_app_permissions) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            AppPermissionsToolScreen(navController = navController)
        }
    }

    data object UIDebugger :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_ui_debugger) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            UIDebuggerToolScreen(navController = navController)
        }
    }

    data object ShellExecutor :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_shell_executor) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ShellExecutorToolScreen(navController = navController)
        }
    }

    data object Logcat :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_logcat) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            LogcatToolScreen(navController = navController)
        }
    }

    data object SqlViewer :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_sql_viewer) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SqlViewerToolScreen(navController = navController)
        }
    }

    // FFmpeg Toolbox screen
    data object FFmpegToolbox :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_ffmpeg_toolbox) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            FFmpegToolboxScreen(navController = navController)
        }
    }

    // 娴佸紡Markdown婕旂ず灞忓箷
    data object MarkdownDemo :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_markdown_demo) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            StreamMarkdownDemoScreen(onBackClick = onGoBack)
        }
    }

    // 宸ュ叿娴嬭瘯灞忓箷
    data object ToolTester :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_tool_tester) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            ToolTesterScreen(navController = navController)
        }
    }

    // 鍦∕arkdownDemo瀵硅薄鍚庢坊鍔燭extToSpeech瀵硅薄
    data object TextToSpeech :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_text_to_speech) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            TextToSpeechToolScreen(navController = navController)
        }
    }

    // Tools screens
    data object SpeechToText :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_speech_to_text) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            SpeechToTextToolScreen(navController = navController)
        }
    }

    data object DefaultAssistantGuide :
            Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_default_assistant_guide) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            DefaultAssistantGuideToolScreen(navController = navController)
        }
    }


    data object ProcessLimitRemover : Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.tool_process_limit_remover) {
        @Composable
        override fun Content(
            navController: NavController,
            navigateTo: ScreenNavigationHandler,
            updateNavItem: NavItemChangeHandler,
            onGoBack: () -> Unit,
            hasBackgroundImage: Boolean,
            onLoading: (Boolean) -> Unit,
            onError: (String) -> Unit,
            onGestureConsumed: (Boolean) -> Unit
        ) {
            ProcessLimitRemoverToolScreen(navController = navController)
        }
    }

    data object HtmlPackager : Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_html_packager) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            HtmlPackagerScreen(onGoBack = onGoBack)
        }
    }

    data object AutoGlmOneClick : Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_autoglm_one_click) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            AutoGlmOneClickToolScreen(
                navController = navController,
                onNavigateToModelConfig = {
                    navigateTo(ModelConfig)
                    updateNavItem(NavItem.Settings)
                }
            )
        }
    }
    
    data object AutoGlmTool : Screen(parentScreen = Toolbox, navItem = NavItem.Toolbox, titleRes = R.string.screen_title_autoglm_tool) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            AutoGlmToolScreen()
        }
    }

    // MCP 鎻掍欢璇︽儏椤甸潰
    data class MCPPluginDetail(val issue: com.ai.assistance.metaagent.data.api.GitHubIssue) :
            Screen(parentScreen = Packages, navItem = NavItem.Packages) {
        @Composable
        override fun Content(
                navController: NavController,
                navigateTo: ScreenNavigationHandler,
                updateNavItem: NavItemChangeHandler,
                onGoBack: () -> Unit,
                hasBackgroundImage: Boolean,
                onLoading: (Boolean) -> Unit,
                onError: (String) -> Unit,
                onGestureConsumed: (Boolean) -> Unit
        ) {
            MCPPluginDetailScreen(
                issue = issue,
                onNavigateBack = onGoBack
            )
        }
    }

    // 鑾峰彇灞忓箷鏍囬
    @Composable
    open fun getTitle(): String = titleRes?.let { stringResource(it) } ?: ""

    // 鍒ゆ柇鏄惁涓轰簩绾у睆骞?
    val isSecondaryScreen: Boolean
        get() = parentScreen != null
}

// 璺敱绠＄悊鍣?
object MetaAgentRouter {
    // 澶勭悊杩斿洖瀵艰埅
    fun handleBackNavigation(currentScreen: Screen): Screen? {
        return currentScreen.parentScreen
    }

    // 鏍规嵁NavItem鑾峰彇瀵瑰簲鐨凷creen
    fun getScreenForNavItem(navItem: NavItem): Screen {
        return when (navItem) {
            NavItem.MetaHome -> Screen.MetaHome
            NavItem.AiChat -> Screen.AiChat
            NavItem.MemoryBase -> Screen.MemoryBase
            NavItem.Packages -> Screen.Packages
            NavItem.Toolbox -> Screen.Toolbox
            NavItem.ShizukuCommands -> Screen.ShizukuCommands
            NavItem.Settings -> Screen.Settings
            NavItem.Help -> Screen.Help
            NavItem.About -> Screen.About
            NavItem.TokenConfig -> Screen.TokenConfig
            NavItem.UserPreferencesGuide -> Screen.UserPreferencesGuide()
            NavItem.AssistantConfig -> Screen.AssistantConfig
            NavItem.Agreement -> Screen.Agreement
            NavItem.UpdateHistory -> Screen.UpdateHistory
            NavItem.Workflow -> Screen.Workflow
            else -> Screen.AiChat
        }
    }
}

// 鍏ㄥ眬鐨勬墜鍔跨姸鎬佹寔鏈夎€咃紝鐢ㄤ簬鍦ㄤ笉鍚岀粍浠堕棿鍏变韩鎵嬪娍鐘舵€?
object GestureStateHolder {
    // 鑱婂ぉ鐣岄潰鎵嬪娍鏄惁琚秷璐圭殑鐘舵€?
    var isChatScreenGestureConsumed: Boolean = false
}





