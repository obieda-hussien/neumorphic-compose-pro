<div dir="rtl">

# مكتبة Neumorphism UI Pro لأندرويد 🚀

[![JitPack](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro)
![الرخصة](https://img.shields.io/badge/License-Apache_2.0-blue.svg)
![أندرويد](https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg)

مكتبة واجهات neumorphic فائقة الأداء ومحسّنة بالكامل لأندرويد تدعم **Jetpack Compose** و **XML / Views** التقليدية.

تمت إعادة هيكلتها وتطويرها للقضاء على التقطيع (Lag)، وتوفير البطارية، وتخزين الظلال مؤقتاً في الذاكرة (LRU Cache)، وتكامل كامل مع **Material 3 Expressive**.

---

## أبرز التحسينات والأداء ⚡

- ⚡ **سرعة فائقة وسلاسة في التشغيل**: استخدام `NeuCache` (LruCache) لتخزين الظلال المنفذة سابقاً وإعادة استخدامها دون الحاجة لإعادة التنعيم والبلور (Blur) عند كل رندر أو سكرول.
- 🔋 **توفير البطارية والموارد**: تقليل ضغط الـ Garbage Collector والذاكرة لضمان عدم استنزاف عتاد الجهاز.
- 🎨 **دعم كامل لـ Jetpack Compose**: استخدام modifiers مثل `neumorphic()`, `animatedNeumorphic()`, `springNeumorphic()`, و `expressiveNeumorphic()`.
- 📱 **دعم XML / Android Views**: عناصر `NeumorphicView`, `NeumorphicButton`, و `NeumorphicCardView` عالية الأداء.
- 🎭 **Material Design 3 Expressive**: فيزياء الزنبرك (Spring Physics) والألوان الديناميكية على Android 12+.

---

## التثبيت عبر JitPack 📦

### 1. إضافة مستودع JitPack

أضف مستودع JitPack في ملف `settings.gradle` أو `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. إضافة التبعيات (Dependencies)

في ملف `build.gradle` الخاص بالموديول/التطبيق:

#### مكتبة Jetpack Compose
```kotlin
implementation("com.github.obieda-hussien:neumorphic-compose-pro:3.0.0")
```

#### مكتبة XML / Android Views
```kotlin
implementation("com.github.obieda-hussien:neumorphic-views-pro:3.0.0")
```

---

## البداية السريعة 🏁

### Jetpack Compose

```kotlin
// عنصر نيومورفك أساسي
Box(
    modifier = Modifier
        .size(150.dp)
        .neumorphic(
            neuShape = Punched.Rounded(16.dp),
            elevation = 6.dp,
            lightSource = LightSource.TOP_LEFT
        )
) {
    Text("Pro Neumorphic Card", modifier = Modifier.align(Alignment.Center))
}
```

---

## المطور والمسؤول 👨‍💻

**عبيدة حسين (Obieda Hussien)**
مستودع المشروع: [neumorphic-compose-pro](https://github.com/obieda-hussien/neumorphic-compose-pro)

---

## الرخصة 📜

مرخص تحت رخصة Apache License, Version 2.0.

</div>
