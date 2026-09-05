package com.gameora.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * خدمة استقبال إشعارات Firebase Cloud Messaging.
 *
 * ملاحظة:
 * التعامل الفعلي مع الإشعارات (عرضها، الضغط عليها، التنقل
 * للشاشة المناسبة) سيتم استكماله لاحقًا حسب حاجة المنتج.
 * الكلاس هنا موجود بالحد الأدنى حتى يطابق التعريف في
 * AndroidManifest.xml ولا يفشل البناء أو تشغيل FCM.
 */
class GameoraMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // TODO: إرسال الـ token الجديد إلى Backend
        // وربطه بحساب المستخدم الحالي (users/{uid}/fcmTokens)
        // حتى يستطيع الـ Backend إرسال إشعارات مستهدفة له.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // TODO: عرض الإشعار للمستخدم (NotificationCompat)
        // بناءً على نوع الحدث المُرسَل من Backend، مثل:
        // - رسالة جديدة في محادثة
        // - موافقة/رفض البائع على طلب
        // - انتهاء مهلة موافقة البائع
    }
}
