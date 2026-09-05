# قواعد Proguard/R8 الخاصة بتطبيق Gameora.
# راجع: https://developer.android.com/studio/build/shrink-code

# الاحتفاظ بموديلات البيانات كاملة لأنها تُستخدم مع Firestore
# عبر toObject()/toObjects() اللي بتعتمد على reflection.
-keep class com.gameora.app.data.model.** { *; }

# Firebase Firestore / Functions / Auth / Messaging
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coroutines
-dontwarn kotlinx.coroutines.**
