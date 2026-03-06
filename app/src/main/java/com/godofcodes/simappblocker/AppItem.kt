package com.godofcodes.simappblocker

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isHidden: Boolean,
    val isSystem: Boolean,
    val isBloatware: Boolean = false,
    val isUninstalled: Boolean = false
)
