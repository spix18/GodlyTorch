# Android default ProGuard rules. Add project-specific rules here.
# https://developer.android.com/build/shrink-code

# Keep BuildConfig
-keep class com.teamdarkness.godlytorch.BuildConfig { *; }

# libsu (root shell) — keep for reflection
-keep class com.topjohnwu.superuser.** { *; }
-dontwarn com.topjohnwu.superuser.**

# Croller knob library — uses reflection on attributes
-keep class com.sdsmdg.harjot.crollerTest.** { *; }

# Iconics (typeface registration via reflection)
-keep class com.mikepenz.iconics.** { *; }
-keep class com.mikepenz.google_material_typeface_library.** { *; }
-keep class com.mikepenz.fontawesome_typeface_library.** { *; }

# Keep tile services (referenced by manifest only)
-keep class com.teamdarkness.godlytorch.Service.** { *; }

# Keep theme launcher / activities referenced by manifest
-keep class com.teamdarkness.godlytorch.Activity.** { *; }
-keep class com.teamdarkness.godlytorch.Settings.SettingsActivity { *; }
-keep class com.teamdarkness.godlytorch.Settings.SelectDevicePreference { *; }

# Kotlin metadata
-keep class kotlin.Metadata { *; }
