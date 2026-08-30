<div dir="rtl">

# مكتبة Neumorphism UI لأندرويد

مكتبة حديثة ومرنة لواجهة Neumorphism لأندرويد تدعم كل من **Jetpack Compose** و **Views/XML** التقليدية - وفي هذا الفرع (fork) تم تحسينها عشان ترسم نفس الظلال بمعالجة CPU/بطارية أقل بكتير من النسخة الأصلية، من غير أي فرق في الجودة البصرية.

[![](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro)

## المميزات ✨

- 🎨 **دعم Jetpack Compose** - استخدم الـ `neumorphic()` modifier مع أي composable
- 📱 **دعم XML/Java Views** - NeumorphicView, NeumorphicButton, NeumorphicCardView
- 🌓 **دعم الوضع الداكن** - مخططات ألوان مدمجة للوضع الفاتح والداكن
- 🎭 **دعم Material You** - ألوان ديناميكية على أندرويد 12+
- 💫 **دعم الأنيميشن** - تأثيرات ضغط سلسة
- 🔆 **مصدر الإضاءة قابل للتخصيص** - أعلى اليسار، أعلى اليمين، أسفل اليسار، أسفل اليمين
- 🔋 **رسم موفّر للطاقة (v3.1.0)** - الظلال بقت متخزنة (cached) ومشتركة بدل ما تتحسب من الصفر كل فريم - التفاصيل في قسم [الأداء](#الأداء)

## التثبيت

المكتبة منشورة عبر [JitPack](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro).

ضيف مستودع JitPack في `settings.gradle` بتاع المشروع:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### مكتبة Jetpack Compose

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:3.1.0")
```

### مكتبة XML/Views

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library-views:2.1.0")
```

> الـ API العام متغيرش نهائي - نفس الدوال ونفس الاستخدام، بس غيّر إحداثيات الاعتمادية (dependency coordinates) فوق وكل حاجة هتشتغل زي ما هي.

## البداية السريعة

### Jetpack Compose

```kotlin
// استخدام بسيط
Card(
    modifier = Modifier
        .padding(16.dp)
        .size(200.dp)
        .neumorphic()
) {
    // المحتوى
}

// مع التخصيص
Button(
    modifier = Modifier
        .neumorphic(
            neuShape = Punched.Rounded(radius = 12.dp),
            elevation = 8.dp,
            lightShadowColor = Color.White,
            darkShadowColor = Color.Gray,
            lightSource = LightSource.TOP_LEFT
        )
) {
    Text("اضغط هنا")
}
```

### XML Layout

```xml
<me.nikhilchaudhari.library.views.NeumorphicButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="زر Neumorphic"
    app:neuShape="punched"
    app:neuCornerRadius="12dp"
    app:neuElevation="8dp"
    app:neuLightSource="topLeft" />
```

### استخدام Java

```java
NeumorphicButton button = new NeumorphicButton(context);
button.setNeuShapeType(NeuShapeType.PUNCHED);
button.setNeuCornerRadius(dpToPx(12));
button.setNeuElevation(dpToPx(8));
```

## الأشكال

ثلاثة أشكال متاحة:

| الشكل | الوصف |
|-------|--------|
| `Punched` | تأثير بارز/مرتفع |
| `Pressed` | تأثير مضغوط/غائر |
| `Pot` | مزيج من البارز والغائر |

## خيارات التخصيص

| المعامل | القيمة الافتراضية | الوصف |
|---------|-------------------|--------|
| `neuShape` | `Punched.Rounded(12.dp)` | نوع الشكل |
| `lightShadowColor` | `Color.White` | لون الظل الفاتح |
| `darkShadowColor` | `Color.LightGray` | لون الظل الداكن |
| `elevation` | `6.dp` | عمق الظل |
| `lightSource` | `LightSource.TOP_LEFT` | اتجاه مصدر الإضاءة |

## دعم الثيمات

### استخدام ألوان الثيم

```kotlin
@Composable
fun ThemedCard() {
    val colorScheme = NeuTheme.colorScheme() // فاتح/داكن تلقائياً
    
    Card(
        backgroundColor = colorScheme.backgroundColor,
        modifier = Modifier.themedNeumorphic(colorScheme)
    ) {
        // المحتوى
    }
}
```

### ألوان Material You الديناميكية (أندرويد 12+)

```kotlin
@Composable
fun DynamicThemedCard() {
    val colorScheme = NeuTheme.dynamicColorScheme()
    
    Card(
        backgroundColor = colorScheme.backgroundColor,
        modifier = Modifier.themedNeumorphic(colorScheme)
    ) {
        // المحتوى
    }
}
```

## الأداء

كل ظل نيومورفيزم عبارة عن bitmap متعمّلها blur، والنسخة الأصلية كانت بتعيد توليد الـ bitmap **من الصفر في كل استدعاء رسم**، بما فيها كل فريم في أنيميشن الضغط. النسخة دي (`3.1.0` / `2.1.0`) بتحافظ على نفس الشكل البصري بالظبط بس بتغيّر عدد مرات الشغل ده وتكلفته.

### السبب الحقيقي لاستنزاف البطارية والتهنيج عند الفتح

1. **كان بيتعمل `RenderScript` context جديد كل recomposition** — يعني وقت أنيميشن الضغط كان بيتعمل context تقيل جدًا لحد 60 مرة/ثانية لكل كومبوننت.
2. **صفر caching** — كارتين متطابقين في LazyColumn كانوا بيولّدوا كل واحد الـ bitmap بتاعه لوحده رغم إن الناتج متطابق تمامًا.
3. **الـ blur كان بيشتغل على الدقة الكاملة** رغم إن الـ blur نفسه بيفقد التفاصيل الدقيقة أصلاً.
4. **كل كومبوننت كان عنده RenderScript context خاص بيه** — شاشة عادية فيها 15-20+ كومبوننت نيومورفيزم (هيدر، بحث، كروت، أزرار...)، يعني 15-20+ context تقيل بيتعملوا كلهم مع بعض في أول فريم — وده سبب التهنيج عند فتح التطبيق.

### إيه اللي اتغيّر

| الإصلاح | الأثر |
|---|---|
| `BlurMaker` (والـ RenderScript بتاعه) بقى **singleton واحد على مستوى التطبيق كله** (`NeuBlurMakerHolder`) بدل واحد لكل كومبوننت | أقصى حاجة context واحد بس يتعمل طول عمر التطبيق — ده اللي بيحل مشكلة التهنيج عند الفتح |
| إعادة استخدام نفس `ScriptIntrinsicBlur` بدل إنشاءه كل مرة | يقلل تكلفة مسار RenderScript أكتر |
| كاش LRU مشترك على مستوى التطبيق للظلال (`NeuShadowCache`) | الكومبوننتس المتطابقة (عناصر list، أزرار في وضع الراحة) بتشارك نفس الـ bitmap |
| تقريب الـ elevation لأقرب 0.5dp في مفتاح الكاش | أنيميشن الضغط (لحد 200 فريم) بيتحول لعدد بسيط من الـ bitmaps المُعاد استخدامها بدل واحد لكل فريم |
| الـ blur بقى بيشتغل على دقة مخفّضة (÷2) وبعدين يكبّر تاني | تقليل شغل CPU حوالي 4 أضعاف من غير أي فرق ملحوظ في الجودة |

الـ API العام متغيرش خالص — `neumorphic()`, `animatedNeumorphic()`, `springNeumorphic()`, `expressiveNeumorphic()`, وكل الـ XML views شغالة زي ما هي.

### اختياري: تسخين مسبق (warm-up) قبل أول فريم

لو الشاشة الأولى في تطبيقك مليانة كومبوننتس نيومورفيزم، تقدر تعمل warm-up للـ RenderScript context في الخلفية قبل ما الواجهة تحتاجه:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread { NeuBlurMakerHolder.warmUp(this) }.start()
    }
}
```

اختياري بالكامل وآمن تعمله كذا مرة.

### تطبيق الديمو: حجم أصغر وكود مظبوط

- شلت `material-icons-extended` (~5000 أيقونة، كذا ميجا) واستبدلتها بـ `material-icons-core` اللي فيها كل الأيقونات المستخدمة فعليًا (~10 أيقونات بس).
- شلت `navigation-compose` والـ 3 مكتبات `material3-adaptive` لأنهم مش مستخدمين نهائي في كود الديمو.
- فعّلت `minifyEnabled` + `shrinkResources` لنسخة الـ release (كانت متعطلة تمامًا قبل كده).
- صلّحت شرط ميت في أيقونة الإشعارات في الهيدر كان بيرجع نفس الأيقونة في الحالتين.

## أفضل الممارسات

1. **استخدم ألوان متطابقة**: يجب أن تكون ألوان الخلفية والظل متشابهة
2. **تجنب الأبيض/الأسود النقي**: استخدم ألوان رمادية للحصول على ظلال واقعية
3. **حافظ على اتساق مصدر الإضاءة**: اجعل مصدر الإضاءة ثابتاً في واجهتك
4. **استخدم elevation مناسب**: 4-12dp يعمل بشكل أفضل لمعظم الحالات

## المتطلبات

- **الحد الأدنى للـ SDK**: 21 (أندرويد 5.0)
- **الـ SDK المستهدف**: 34 (أندرويد 14)
- **Compose**: 1.5.4+
- **Kotlin**: 1.9.20+

## الرخصة

مرخص تحت Apache License, Version 2.0

## المساهمة

المساهمات مرحب بها! لا تتردد في تقديم issues و pull requests.

</div>
