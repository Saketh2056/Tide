# Room entities and DAOs are kept via generated code; keep enum values used in converters.
-keepclassmembers enum com.tide.app.data.db.** { *; }

# Keep the accessibility service (referenced from the manifest).
-keep class com.tide.app.service.GuardianService { *; }

# Compose / Kotlin metadata handled by default rules. Strip logs in release.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
