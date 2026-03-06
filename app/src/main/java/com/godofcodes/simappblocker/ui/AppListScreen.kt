package com.godofcodes.simappblocker.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.AppViewModel
import com.godofcodes.simappblocker.R
import com.godofcodes.simappblocker.ui.theme.HiddenBadge
import com.godofcodes.simappblocker.ui.theme.HiddenBadgeText
import com.godofcodes.simappblocker.ui.theme.Surface
import com.godofcodes.simappblocker.ui.theme.SurfaceVariant
import com.godofcodes.simappblocker.ui.theme.VisibleBadge
import com.godofcodes.simappblocker.ui.theme.VisibleBadgeText

@Composable
fun AppListScreen(viewModel: AppViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarEvent) {
        uiState.snackbarEvent?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppListTopBar(
                query = uiState.query,
                showSystem = uiState.showSystem,
                showHiddenOnly = uiState.showHiddenOnly,
                showBloatwareOnly = uiState.showBloatwareOnly,
                onQueryChange = viewModel::setQuery,
                onClearQuery = { viewModel.setQuery("") },
                onShowSystemToggle = { viewModel.setShowSystem(!uiState.showSystem) },
                onShowHiddenOnlyToggle = { viewModel.setShowHiddenOnly(!uiState.showHiddenOnly) },
                onShowBloatwareOnlyToggle = { viewModel.setShowBloatwareOnly(!uiState.showBloatwareOnly) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> ShimmerList()
                filteredApps.isEmpty() -> EmptyState()
                else -> AppList(
                    bloatwareApps = filteredApps.filter { it.isBloatware },
                    regularApps = filteredApps.filter { !it.isBloatware },
                    iconCache = viewModel.iconCache,
                    onItemClick = viewModel::selectItem
                )
            }
        }
    }

    val context = LocalContext.current
    uiState.selectedItem?.let { item ->
        ActionDialog(
            item = item,
            iconCache = viewModel.iconCache,
            onToggle = { viewModel.toggleVisibility(item) },
            onDelete = { viewModel.requestDelete(item) },
            onDismiss = viewModel::clearSelection,
            onLaunch = {
                context.packageManager.getLaunchIntentForPackage(item.packageName)
                    ?.let { context.startActivity(it) }
            }
        )
    }

    uiState.confirmDeleteItem?.let { item ->
        ConfirmDeleteDialog(
            item = item,
            onConfirm = { viewModel.uninstall(item) },
            onDismiss = viewModel::clearConfirmDelete
        )
    }
}

@Composable
private fun AppListTopBar(
    query: String,
    showSystem: Boolean,
    showHiddenOnly: Boolean,
    showBloatwareOnly: Boolean,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onShowSystemToggle: () -> Unit,
    onShowHiddenOnlyToggle: () -> Unit,
    onShowBloatwareOnlyToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onClearQuery() }
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = SurfaceVariant,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = showSystem,
                onClick = onShowSystemToggle,
                label = { Text(stringResource(R.string.filter_system), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = showHiddenOnly,
                onClick = onShowHiddenOnlyToggle,
                label = { Text(stringResource(R.string.filter_hidden_only), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = showBloatwareOnly,
                onClick = onShowBloatwareOnlyToggle,
                label = { Text("Bloatware", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF59E0B),
                    selectedLabelColor = Color.Black
                )
            )
        }
    }
}

@Composable
private fun AppList(
    bloatwareApps: List<AppItem>,
    regularApps: List<AppItem>,
    iconCache: MutableMap<String, Drawable>,
    onItemClick: (AppItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (bloatwareApps.isNotEmpty()) {
            item(key = "bloatware_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_dialog_alert),
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Kaldırılması Önerilen (${bloatwareApps.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
            items(bloatwareApps, key = { "bloat_${it.packageName}" }) { item ->
                AppListItem(item = item, iconCache = iconCache, onClick = { onItemClick(item) }, showBloatwareBadge = true)
            }
            item(key = "bloatware_divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = SurfaceVariant
                )
            }
        }
        items(regularApps, key = { it.packageName }) { item ->
            AppListItem(item = item, iconCache = iconCache, onClick = { onItemClick(item) })
        }
    }
}

@Composable
private fun AppListItem(
    item: AppItem,
    iconCache: MutableMap<String, Drawable>,
    onClick: () -> Unit,
    showBloatwareBadge: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = iconCache[item.packageName]
        if (icon != null) {
            Image(
                painter = rememberDrawablePainter(icon),
                contentDescription = item.label,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.packageName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(if (item.isHidden) R.string.status_hidden else R.string.status_visible),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.isHidden) HiddenBadgeText else VisibleBadgeText,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (item.isHidden) HiddenBadge else VisibleBadge)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            if (item.isUninstalled) {
                Text(
                    text = "silinmiş",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3D0000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            if (showBloatwareBadge) {
                Text(
                    text = "bloatware",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3D2E00))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionDialog(
    item: AppItem,
    iconCache: MutableMap<String, Drawable>,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                val dialogIcon = iconCache[item.packageName]
                if (dialogIcon != null) {
                    Image(
                        painter = rememberDrawablePainter(dialogIcon),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).clickable { onLaunch() }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant)
                            .clickable { onLaunch() }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(item.label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(item.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = null,
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(if (item.isHidden) R.string.action_show else R.string.action_hide))
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    item: AppItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text(stringResource(R.string.delete_title), color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Text(
                stringResource(R.string.delete_confirm_message, item.label),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.empty_list),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ShimmerList() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(SurfaceVariant, Color(0xFF3A3A3A), SurfaceVariant),
        start = Offset(shimmerOffset - 500f, 0f),
        end = Offset(shimmerOffset, 0f)
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(10) {
            ShimmerItem(shimmerBrush)
        }
    }
}

@Composable
private fun ShimmerItem(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(brush))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.55f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(11.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        }
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.width(52.dp).height(22.dp).clip(RoundedCornerShape(6.dp)).background(brush))
    }
}

@Composable
private fun rememberDrawablePainter(drawable: Drawable): BitmapPainter {
    val bitmap = remember(drawable) {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp.asImageBitmap()
    }
    return BitmapPainter(bitmap)
}

