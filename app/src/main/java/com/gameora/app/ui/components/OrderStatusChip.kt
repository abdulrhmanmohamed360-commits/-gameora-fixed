package com.gameora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gameora.app.data.model.OrderStatus
import com.gameora.app.ui.theme.ErrorRed
import com.gameora.app.ui.theme.PendingBlue
import com.gameora.app.ui.theme.SuccessGreen
import com.gameora.app.ui.theme.WarningYellow

@Composable
fun OrderStatusChip(
    status: OrderStatus
) {

    val (label, color) = when (status) {

        // =========================
        // انتظار موافقة البائع
        // =========================

        OrderStatus.PENDING_SELLER_APPROVAL ->
            "بانتظار موافقة البائع" to PendingBlue

        // =========================
        // البائع وافق
        // =========================

        OrderStatus.SELLER_ACCEPTED ->
            "البائع وافق" to SuccessGreen

        // =========================
        // المحادثة مفتوحة
        // =========================

        OrderStatus.CHAT_ACTIVE ->
            "المحادثة مفتوحة" to PendingBlue

        // =========================
        // تم تسليم الحساب
        // =========================

        OrderStatus.ACCOUNT_DELIVERED ->
            "تم تسليم الحساب" to WarningYellow

        // =========================
        // المشتري يختبر الحساب
        // =========================

        OrderStatus.BUYER_TESTING ->
            "المشتري يختبر الحساب" to WarningYellow

        // =========================
        // المشتري أكد الاستلام
        // =========================

        OrderStatus.BUYER_CONFIRMED ->
            "تم تأكيد الاستلام" to SuccessGreen

        // =========================
        // اكتملت العملية
        // =========================

        OrderStatus.COMPLETED ->
            "مكتمل" to SuccessGreen

        // =========================
        // يوجد نزاع
        // =========================

        OrderStatus.DISPUTED ->
            "يوجد بلاغ" to ErrorRed

        // =========================
        // تم الإلغاء
        // =========================

        OrderStatus.CANCELLED ->
            "ملغي" to ErrorRed

        // =========================
        // تم رد الأموال
        // =========================

        OrderStatus.REFUNDED ->
            "تم رد المبلغ" to SuccessGreen
    }

    Text(
        text = label,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(
                color.copy(alpha = 0.15f)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
    )
}

