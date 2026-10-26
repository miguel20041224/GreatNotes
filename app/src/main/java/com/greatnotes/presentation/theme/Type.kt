package com.greatnotes.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val AppTypography = Typography().let {
    it.copy(
        titleLarge = it.titleLarge.merge(TextStyle(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)),
        titleSmall = it.titleSmall.merge(TextStyle(fontWeight = FontWeight.SemiBold)),
    )
}
