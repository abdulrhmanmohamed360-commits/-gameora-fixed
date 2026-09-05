package com.gameora.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

val GameoraTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        headlineLarge = headlineLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        headlineMedium = headlineMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        titleLarge = titleLarge.copy(
            fontWeight = FontWeight.SemiBold
        ),
        titleMedium = titleMedium.copy(
            fontWeight = FontWeight.Medium
        ),
        bodyLarge = bodyLarge.copy(
            fontWeight = FontWeight.Normal
        ),
        bodyMedium = bodyMedium.copy(
            fontWeight = FontWeight.Normal
        ),
        labelLarge = labelLarge.copy(
            fontWeight = FontWeight.SemiBold
        )
    )
}
