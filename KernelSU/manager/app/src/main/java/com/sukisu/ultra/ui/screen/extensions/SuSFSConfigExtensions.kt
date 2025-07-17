package com.sukisu.ultra.ui.screen.extensions

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.AppIcon
import com.sukisu.ultra.ui.util.SuSFSManager
import com.sukisu.ultra.ui.viewmodel.SuperUserViewModel
import kotlinx.coroutines.launch

// 应用信息缓存
object AppInfoCache {
    private val appInfoMap = mutableMapOf<String, CachedAppInfo>()

    data class CachedAppInfo(
        val appName: String,
        val packageInfo: PackageInfo?,
        val drawable: Drawable?,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getAppInfo(packageName: String): CachedAppInfo? {
        return appInfoMap[packageName]
    }

    fun putAppInfo(packageName: String, appInfo: CachedAppInfo) {
        appInfoMap[packageName] = appInfo
    }

    fun clearCache() {
        appInfoMap.clear()
    }

    fun hasCache(packageName: String): Boolean {
        return appInfoMap.containsKey(packageName)
    }

    fun getAppInfoFromSuperUser(packageName: String): CachedAppInfo? {
        val superUserApp = SuperUserViewModel.apps.find { it.packageName == packageName }
        return superUserApp?.let { app ->
            CachedAppInfo(
                appName = app.label,
                packageInfo = app.packageInfo,
                drawable = null
            )
        }
    }
}

/**
 * 空状态显示组件
 */
@Composable
fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 路径项目卡片组件
 */
@Composable
fun PathItemCard(
    path: String,
    icon: ImageVector,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    isLoading: Boolean = false,
    additionalInfo: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = path,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (additionalInfo != null) {
                        Text(
                            text = additionalInfo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onEdit != null) {
                    IconButton(
                        onClick = onEdit,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Kstat配置项目卡片组件
 */
@Composable
fun KstatConfigItemCard(
    config: String,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val parts = config.split("|")
                    if (parts.isNotEmpty()) {
                        Text(
                            text = parts[0], // 路径
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (parts.size > 1) {
                            Text(
                                text = parts.drop(1).joinToString(" "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = config,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onEdit != null) {
                    IconButton(
                        onClick = onEdit,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Add Kstat路径项目卡片组件
 */
@Composable
fun AddKstatPathItemCard(
    path: String,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onUpdate: () -> Unit,
    onUpdateFullClone: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onEdit != null) {
                    IconButton(
                        onClick = onEdit,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onUpdate,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = stringResource(R.string.update),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onUpdateFullClone,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.susfs_update_full_clone),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 启用功能状态卡片组件
 */
@Composable
fun FeatureStatusCard(
    feature: SuSFSManager.EnabledFeature,
    onRefresh: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 日志配置对话框状态
    var showLogConfigDialog by remember { mutableStateOf(false) }
    var logEnabled by remember { mutableStateOf(SuSFSManager.getEnableLogState(context)) }

    // 日志配置对话框
    if (showLogConfigDialog) {
        AlertDialog(
            onDismissRequest = { showLogConfigDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.susfs_log_config_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.susfs_log_config_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.susfs_enable_log_label),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = logEnabled,
                            onCheckedChange = { logEnabled = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (SuSFSManager.setEnableLog(context, logEnabled)) {
                                onRefresh?.invoke()
                            }
                            showLogConfigDialog = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.susfs_apply))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 恢复原始状态
                        logEnabled = SuSFSManager.getEnableLogState(context)
                        showLogConfigDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .then(
                if (feature.canConfigure) {
                    Modifier.clickable {
                        // 更新当前状态
                        logEnabled = SuSFSManager.getEnableLogState(context)
                        showLogConfigDialog = true
                    }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (feature.canConfigure) {
                    Text(
                        text = stringResource(R.string.susfs_feature_configurable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态标签
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        feature.isEnabled -> MaterialTheme.colorScheme.primary
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = feature.statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            feature.isEnabled -> MaterialTheme.colorScheme.onPrimary
                            else -> Color.White
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * SUS挂载隐藏控制卡片组件
 */
@Composable
fun SusMountHidingControlCard(
    hideSusMountsForAllProcs: Boolean,
    isLoading: Boolean,
    onToggleHiding: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hideSusMountsForAllProcs) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.susfs_hide_mounts_control_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 描述文本
            Text(
                text = stringResource(R.string.susfs_hide_mounts_control_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // 控制开关行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.susfs_hide_mounts_for_all_procs_label),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hideSusMountsForAllProcs) {
                            stringResource(R.string.susfs_hide_mounts_for_all_procs_enabled_description)
                        } else {
                            stringResource(R.string.susfs_hide_mounts_for_all_procs_disabled_description)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
                Switch(
                    checked = hideSusMountsForAllProcs,
                    onCheckedChange = onToggleHiding,
                    enabled = !isLoading
                )
            }

            // 当前设置显示
            Text(
                text = stringResource(
                    R.string.susfs_hide_mounts_current_setting,
                    if (hideSusMountsForAllProcs) {
                        stringResource(R.string.susfs_hide_mounts_setting_all)
                    } else {
                        stringResource(R.string.susfs_hide_mounts_setting_non_ksu)
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            // 建议文本
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.susfs_hide_mounts_recommendation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

/**
 * 应用路径分组卡片
 */
@Composable
fun AppPathGroupCard(
    packageName: String,
    paths: List<String>,
    onDeleteGroup: () -> Unit,
    onEditGroup: (() -> Unit)? = null,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val superUserApps = SuperUserViewModel.apps
    var cachedAppInfo by remember(packageName, superUserApps.size) {
        mutableStateOf(AppInfoCache.getAppInfo(packageName))
    }
    var isLoadingAppInfo by remember(packageName, superUserApps.size) { mutableStateOf(false) }

    LaunchedEffect(packageName, superUserApps.size) {
        if (cachedAppInfo == null || superUserApps.isNotEmpty()) {
            isLoadingAppInfo = true
            coroutineScope.launch {
                try {
                    val superUserAppInfo = AppInfoCache.getAppInfoFromSuperUser(packageName)

                    if (superUserAppInfo != null) {
                        val packageManager = context.packageManager
                        val drawable = try {
                            superUserAppInfo.packageInfo?.applicationInfo?.let {
                                packageManager.getApplicationIcon(it)
                            }
                        } catch (_: Exception) {
                            null
                        }

                        val newCachedInfo = AppInfoCache.CachedAppInfo(
                            appName = superUserAppInfo.appName,
                            packageInfo = superUserAppInfo.packageInfo,
                            drawable = drawable
                        )

                        AppInfoCache.putAppInfo(packageName, newCachedInfo)
                        cachedAppInfo = newCachedInfo
                    } else {
                        val packageManager = context.packageManager
                        val appInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)

                        val appName = try {
                            appInfo.applicationInfo?.let {
                                packageManager.getApplicationLabel(it).toString()
                            } ?: packageName
                        } catch (_: Exception) {
                            packageName
                        }

                        val drawable = try {
                            appInfo.applicationInfo?.let {
                                packageManager.getApplicationIcon(it)
                            }
                        } catch (_: Exception) {
                            null
                        }

                        val newCachedInfo = AppInfoCache.CachedAppInfo(
                            appName = appName,
                            packageInfo = appInfo,
                            drawable = drawable
                        )

                        AppInfoCache.putAppInfo(packageName, newCachedInfo)
                        cachedAppInfo = newCachedInfo
                    }
                } catch (_: Exception) {
                    val newCachedInfo = AppInfoCache.CachedAppInfo(
                        appName = packageName,
                        packageInfo = null,
                        drawable = null
                    )
                    AppInfoCache.putAppInfo(packageName, newCachedInfo)
                    cachedAppInfo = newCachedInfo
                } finally {
                    isLoadingAppInfo = false
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 应用图标
                AppIcon(
                    packageName = packageName,
                    packageInfo = cachedAppInfo?.packageInfo,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val displayName = cachedAppInfo?.appName?.ifEmpty { packageName } ?: packageName
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!isLoadingAppInfo && cachedAppInfo?.appName?.isNotEmpty() == true &&
                        cachedAppInfo?.appName != packageName) {
                        Text(
                            text = packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onEditGroup != null) {
                        IconButton(
                            onClick = onEditGroup,
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDeleteGroup,
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 显示所有路径
            Spacer(modifier = Modifier.height(8.dp))

            paths.forEach { path ->
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp)
                )

                if (path != paths.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * 分组标题组件
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    count: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}