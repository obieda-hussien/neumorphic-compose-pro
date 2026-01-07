-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose

# Keep neumorphic view classes
-keep class me.nikhilchaudhari.library.views.** { *; }

# Keep custom attributes
-keepclassmembers class **.R$styleable {
    public static <fields>;
}
