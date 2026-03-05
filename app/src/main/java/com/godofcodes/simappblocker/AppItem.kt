package com.godofcodes.simappblocker

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    var isHidden: Boolean
)