@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.ai.assistance.metaagent.ui.features.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.metaagent.ui.components.CustomScaffold
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.PcAgentConnectionUiState
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.PcAgentConnectionViewModel
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.PcAgentConnectionViewModelFactory

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PcAgentConnectionSettingsScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PcAgentConnectionViewModel = viewModel(
        factory = PcAgentConnectionViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    CustomScaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Text(
                        text = "PC Agent 编排连接",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeroCard()
            }

            item {
                ConnectionFormCard(
                    uiState = uiState,
                    onHostChanged = viewModel::updateHost,
                    onPortChanged = viewModel::updatePort,
                    onTokenChanged = viewModel::updateToken,
                    onSave = viewModel::saveConfig,
                    onTest = viewModel::testConnection
                )
            }

            item {
                StatusCard(uiState = uiState)
            }

            item {
                StartupGuideCard(uiState = uiState)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onBackPressed) {
                    Text("返回设置")
                }
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(icon = Icons.Default.Hub, text = "独立于旧远控")
                Badge(icon = Icons.Default.Link, text = "WebSocket")
                Badge(icon = Icons.Default.Terminal, text = "面向编排树 PC 节点")
            }
            Text(
                text = "这套连接只服务于 Android 主调度 -> PC 子 Agent 编排链路，不再走旧的桌面配对或 HTTP 遥控接口。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "保存后，计划里的 PC 节点会直接连接这里配置的 ws://host:port/ws/pc-agent。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun ConnectionFormCard(
    uiState: PcAgentConnectionUiState,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onTokenChanged: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "连接信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Host / Port / Token 会单独保存给 PC Agent 编排使用，不会触发旧的请求连接或提交密码逻辑。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            OutlinedTextField(
                value = uiState.host,
                onValueChange = onHostChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("电脑地址 / Host") },
                supportingText = { Text("支持 192.168.x.x，也支持 ws:// 或 http:// 前缀") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.port,
                onValueChange = onPortChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("端口") },
                supportingText = { Text("默认 3210") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.token,
                onValueChange = onTokenChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("编排 Token") },
                supportingText = { Text("和 python main.py --token 保持一致") },
                singleLine = true
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving && !uiState.isTesting
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存编排连接")
                }
                OutlinedButton(
                    onClick = onTest,
                    enabled = !uiState.isSaving && !uiState.isTesting
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Dns, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("测试连接")
                }
            }
        }
    }
}

@Composable
private fun StatusCard(uiState: PcAgentConnectionUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "当前状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            MetaLine(label = "Endpoint", value = uiState.endpointPreview)
            uiState.statusMessage?.let {
                MetaLine(label = "状态", value = it, icon = Icons.Default.CheckCircle)
            }
            uiState.lastError?.let {
                MetaLine(label = "错误", value = it, icon = Icons.Default.Warning)
            }
            uiState.lastTestOutput?.let {
                MetaLine(
                    label = "测试输出",
                    value = it,
                    icon = Icons.Default.Terminal,
                    allowWrap = true
                )
            }
            if (uiState.statusMessage == null && uiState.lastError == null && uiState.lastTestOutput == null) {
                Text(
                    text = "保存后可直接点“测试连接”。测试会发起一个最小 shell 会话，期望返回 pc-agent-connection-ok。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StartupGuideCard(uiState: PcAgentConnectionUiState) {
    val tokenDisplay = uiState.token.ifBlank { "metaagent-step1" }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PC 端启动命令",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "在电脑上进入主仓的 pc_orchestrator 目录，然后使用下面的命令启动。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            CommandBlock(
                text = "python main.py --host 0.0.0.0 --port ${uiState.port.ifBlank { "3210" }} --token $tokenDisplay"
            )
            Text(
                text = "启动成功后，这个设置页的测试连接应返回 pc-agent-connection-ok。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun Badge(icon: ImageVector, text: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MetaLine(
    label: String,
    value: String,
    icon: ImageVector? = null,
    allowWrap: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (icon == null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (allowWrap) Int.MAX_VALUE else 2,
                overflow = if (allowWrap) TextOverflow.Clip else TextOverflow.Ellipsis
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (allowWrap) Int.MAX_VALUE else 2,
                    overflow = if (allowWrap) TextOverflow.Clip else TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CommandBlock(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
