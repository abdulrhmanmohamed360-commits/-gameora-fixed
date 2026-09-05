package com.gameora.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gameora.app.data.model.DisplayPrice
import java.util.Locale

/**
 * يعرض السعر المحوَّل لعملة المستخدم، مع إظهار السعر الأصلي كملاحظة صغيرة
 * حتى تكون الشفافية كاملة للمشتري (لا يوجد تغيير في السعر الحقيقي، فقط عرض).
 */
@Composable
fun PriceTag(
    displayPrice: DisplayPrice,
    originalPrice: Double,
    originalCurrency: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "%,.2f %s".format(Locale.US, displayPrice.amount, displayPrice.currencyCode),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (displayPrice.currencyCode != originalCurrency) {
            Text(
                text = "السعر الأصلي: %,.2f %s".format(Locale.US, originalPrice, originalCurrency),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
