<div dir="rtl">

# مكتبة Neumorphism UI لأندرويد

مكتبة حديثة ومرنة لواجهة Neumorphism لأندرويد تدعم كل من **Jetpack Compose** و**Views/XML** التقليدية - وفي هذا الفرع (fork) تم تحسينها عشان ترسم نفس الظلال بمعالجة CPU/بطارية أقل بكتير من النسخة الأصلية، من غير أي فرق في الجودة البصرية.

[![](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro) ![List of Awesome List Badge](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg) [![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

<p align="center">
<img src="https://github.com/CuriousNikhil/neumorphic-compose/blob/main/static/complete_screen.png?raw=true" height=400>
</p>

## المميزات ✨

- 🎨 **دعم Jetpack Compose** - استخدم الـ `neumorphic()` modifier مع أي composable
- 📱 **دعم XML/Java Views** - Views تقليدية (NeumorphicView, NeumorphicButton, NeumorphicCardView)
- 🌓 **دعم الوضع الداكن** - مخططات ألوان مدمجة للوضع الفاتح والداكن
- 🎭 **دعم Material You** - ألوان ديناميكية على أندرويد 12+
- 💫 **دعم الأنيميشن** - تأثيرات ضغط سلسة
- 🔆 **مصدر الإضاءة قابل للتخصيص** - TOP_LEFT، TOP_RIGHT، BOTTOM_LEFT، BOTTOM_RIGHT
- 🔋 **رسم موفّر للطاقة** - الظلال بقت متخزنة (cached) ومشتركة بدل ما تتحسب من الصفر كل فريم - التفاصيل في قسم [الأداء](#الأداء)
- ✨ **انتقالات crossfade سلسة** - تغيير الاختيار (الـ chips، الـ navigation bar) بيدوب بين الحالتين بدل ما يقفز فجأة - شوف [الجديد في 4.0.0](#الجديد-في-400)

## الجديد في 4.0.0

الموديولين (`library` و`library-views`) بقوا بيتاخدوا نفس رقم الإصدار مع بعض - قبل كده كانت `library` على `3.1.0` و`library-views` متأخرة عند `2.1.0`، وده كان مربك أكتر ما هو مفيد طالما الاتنين بيتنشروا مع بعض من نفس الريبو. `4.0.0` إصدار كبير؛ الـ API العام متغيرش (مفيش حاجة هنا محتاجة تعديل في كودك)، لكن حاجات كتير اتغيرت تحت السطح:

**اتصلح:**
- **تجميد التطبيق عند الفتح** و**استنزاف البطارية/المعالج أثناء الأنيميشن** - شوف [الأداء](#الأداء) للتفاصيل الكاملة (RenderScript context مشترك، كاش لبيتماب الظلال، blur بدقة مخفّضة).
- **كراش حقيقي**: الـ thumb بتاع `NeuSwitch` و`NeuSlider` كان بيستخدم `Modifier.padding()` مع قيمة متحركة بسبرنج. السبرنجات المرتدة بتتخطى هدفها شوية قبل ما تستقر، وده كان بيخلي قيمة الـ padding سالبة لحظيًا - و`Modifier.padding()` بيعمل throw لأي قيمة سالبة. اتحول لـ `Modifier.offset()`، وهو الأنسب أصلاً للحركة المتحركة وبيقبل قيم سالبة من غير كراش.
- **الأشكال البارزة (`Punched`) بتفقد ظلها لما تتقص**: كذا كومبوننت (وشاشات ديمو) كانوا بيطبّقوا `Modifier.clip()` **قبل** `Modifier.neumorphic()`، وده كان بيقص امتداد الظل الناعم بره حدود الشكل، ويسيب صندوق شكله مسطّح. اتصلح في كل المكتبة والديمو؛ شوف تحديث قسم [أفضل الممارسات](#أفضل-الممارسات). شكل `Pot` (بارز+غائر مع بعض) احتاج إصلاح أعمق، لأن clip خارجي واحد مايقدرش يتطبق على واحد بس من تمريرتين الظل بتاعته - دلوقتي بيقص التمريرة الغائرة داخليًا بس.
- **استجابة اللمس مش بتبان في اللمسات السريعة/الخفيفة**: أنيميشن الضغط في كل كومبوننت كان بيتحرك مباشرة بحالة الضغط الخام، واللمسة السريعة ممكن تخلص قبل ما الأنيميشن يلحق يبان - فكان شكله إنه بيستجيب بس للضغطة الثابتة الطويلة. اتصلح بإننا نخلي حالة "مضغوط" البصرية تفضل ظاهرة لمدة ~100ms مضمونة، مهما كانت اللمسة سريعة.
- خطر تصادم في مفتاح كاش `NeuShadowCache` (ألوان مختلفة ممكن تنتج نفس المفتاح)، وكذا مشكلة تحقق/تيست أصغر اتلقطوا في المراجعة.

**اتغيّر:**
- `NeuFloatingActionButton` و`NeuRadioButton` اتعملهم إعادة تصميم عشان يطابقوا شكل باقي المكتبة الناعم ذو الظل المزدوج - الاتنين كان فيهم دايرة صلبة اللون/حد ملون، وده أسلوب Material قياسي مش نيومورفيزم.
- `NeuChip` و`NeuIconButton` (المستخدمين في "Categories" chips وnavigation bar في الديمو) دلوقتي بيعملوا crossfade بين حالتهم البارزة/الغائرة عند تغيير الاختيار بدل ما يتقلبوا في فريم واحد، وأيقونة `NeuChip` بقت تظهر/تختفي بـ fade بدل ما تقفز.

**اتضاف:**
- `NeuPerformanceConfig` - إعدادات downsampling وميزانية كاش الظلال قابلة للتعديل وقت التشغيل؛ شوف [الأداء](#الأداء).
- Baseline profiles مكتوبة يدويًا للموديولات التلاتة؛ شوف [الأداء](#الأداء).
- Gradle version catalog (`gradle/libs.versions.toml`) كمصدر وحيد لإصدارات الاعتماديات في كل الموديولات، وإصلاح لمشكلتين `implementation` مقابل `api` كانوا ممكن يكسروا أي حد بيستخدم المكتبة من JitPack من غير ما يضيف `material3`/`compose-ui` بنفسه.
- تيستات حقيقية (unit/instrumented) لمنطق الكاش ومشاركة الـ `BlurMaker` (كان فيه تيستات placeholder بس قبل كده في المشروع كله).

## التثبيت

المكتبة منشورة عبر [JitPack](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro).

ضيف مستودع JitPack في `settings.gradle` بتاع المشروع (على مستوى المشروع، أو `build.gradle` لو مشروعك أقدم):

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
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:4.0.0")
```

### مكتبة XML/Views

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library-views:4.0.0")
```

> جاي من الـ artifact الأصلي `me.nikhilchaudhari:composeNeumorphism`؟ الـ API العام متغيرش نهائي - نفس الدوال ونفس الاستخدام، بس غيّر إحداثيات الاعتمادية (dependency coordinates) فوق وكل حاجة هتشتغل زي ما هي.

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
    // المحتوى بتاعك
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
    app:neuLightShadowColor="@color/white"
    app:neuDarkShadowColor="@color/gray"
    app:neuLightSource="topLeft" />

<me.nikhilchaudhari.library.views.NeumorphicCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:neuShape="pot"
    app:neuCornerRadius="16dp">

    <!-- المحتوى بتاعك -->

</me.nikhilchaudhari.library.views.NeumorphicCardView>
```

### استخدام Java

```java
NeumorphicButton button = new NeumorphicButton(context);
button.setNeuShapeType(NeuShapeType.PUNCHED);
button.setNeuCornerRadius(dpToPx(12));
button.setNeuElevation(dpToPx(8));
button.setNeuLightSource(LightSource.TOP_LEFT);
```

## الأشكال

فيه 3 أشكال نيومورفيزم متاحة:

| الشكل | الوصف | الشكل البصري |
|-------|-------------|--------|
| `Punched` | تأثير بارز/مرتفع | `__/‾‾‾‾‾‾‾\__` |
| `Pressed` | تأثير مضغوط/غائر | `‾‾\________/‾‾` |
| `Pot` | مزيج من البارز والغائر | `_/\‾‾‾‾‾/\_` |

كل شكل بيدعم نوعين من الحواف:
- **Rounded** - نصف قطر حافة قابل للتخصيص
- **Oval** - شكل دائري/بيضاوي

```kotlin
// Compose
Punched.Rounded(radius = 12.dp)
Punched.Oval()
Pressed.Rounded(radius = 8.dp)
Pressed.Oval()
Pot.Rounded(radius = 16.dp)
Pot.Oval()
```

## خيارات التخصيص

| المعامل | القيمة الافتراضية | الوصف |
|-----------|---------|-------------|
| `neuShape` | `Punched.Rounded(12.dp)` | نوع الشكل وإعدادات الحواف |
| `lightShadowColor` | `Color.White` | لون الظل الفاتح |
| `darkShadowColor` | `Color.LightGray` | لون الظل الداكن |
| `elevation` | `6.dp` | عمق/ارتفاع الظل |
| `strokeWidth` | `6.dp` | عرض الظل الداخلي |
| `neuInsets` | `NeuInsets(6.dp, 6.dp)` | هوامش الظل (أفقي، رأسي) |
| `lightSource` | `LightSource.TOP_LEFT` | اتجاه مصدر الإضاءة |

## مصدر الإضاءة

اضبط اتجاه مصدر الإضاءة عشان تغيّر مكان الظل:

```kotlin
// Compose
Modifier.neumorphic(
    lightSource = LightSource.TOP_LEFT    // الافتراضي
    // أو
    lightSource = LightSource.TOP_RIGHT
    lightSource = LightSource.BOTTOM_LEFT
    lightSource = LightSource.BOTTOM_RIGHT
)
```

## دعم الثيمات

### استخدام ألوان الثيم

```kotlin
@Composable
fun ThemedCard() {
    val colorScheme = NeuTheme.colorScheme() // فاتح/داكن تلقائيًا

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

### مخطط ألوان مخصص

```kotlin
val customScheme = NeuTheme.customColorScheme(
    backgroundColor = Color(0xFFE0E5EC)
)
```

## الأنيميشن

### تأثير ضغط متحرك (Compose)

```kotlin
Modifier.animatedNeumorphic(
    neuShape = Punched.Rounded(),
    elevation = 8.dp,
    pressed = isPressed, // من حالة التفاعل (interaction state)
    animationDuration = 150
)
```

### Clickable مع أنيميشن

```kotlin
Modifier.neumorphicClickable(
    onClick = { /* الحدث */ },
    elevation = 8.dp,
    neuShape = Punched.Rounded()
)
```

### أنيميشن XML Views

الأزرار عندها أنيميشن ضغط مدمج. فعّله/عطّله بـ:

```kotlin
button.enablePressAnimation = true
```

## دوال مساعدة (Utility Extensions)

```kotlin
// نيومورفيزم خفيف (elevation أقل)
Modifier.softNeumorphic()

// نيومورفيزم عميق (elevation أكتر)
Modifier.deepNeumorphic()

// توليد ألوان الظل من لون الخلفية
val (lightShadow, darkShadow) = backgroundColor.toNeuColors()

// تفتيح/تغميق الألوان
val lighter = color.lighten(0.2f)
val darker = color.darken(0.2f)
```

## أفضل الممارسات

1. **استخدم ألوان متقاربة**: لون الخلفية والظل لازم يكونوا متشابهين للحصول على أفضل تأثير
2. **تجنب الأبيض/الأسود النقي**: استخدم درجات قريبة من الأبيض/الرمادي الغامق عشان ظلال واقعية
3. **حافظ على اتساق مصدر الإضاءة**: خلّي مصدر الإضاءة ثابت في كل واجهتك
4. **استخدم elevation مناسب**: من 4 لـ 12dp بيناسب معظم الحالات
5. **استخدم Clip مع شكل Pressed بس**: استخدم `Modifier.clip()` لما تستخدم شكل `Pressed` - الظل الداخلي بتاعه لازم يفضل جوه الحدود. **متعملش clip خالص لشكل `Punched` (بارز) أو `Pot`** - ظلهم مفروض يمتد بره حدود الشكل نفسه، ولو حطيت `Modifier.clip()` قبل `Modifier.neumorphic()` هيقص الامتداد ده ويسيب شكل مسطّح بدل ظل ناعم. كل الكومبوننتس الجاهزة (`NeuXxx`) في المكتبة بتتبع القاعدة دي داخليًا بالفعل.

## الأداء

كل ظل نيومورفيزم عبارة عن bitmap متعمّلها blur، والنسخة الأصلية كانت بتعيد توليد الـ bitmap **من الصفر في كل استدعاء رسم**، بما فيها كل فريم في أنيميشن الضغط. النسخة دي (`4.0.0`) بتحافظ على نفس الشكل البصري بالظبط بس بتغيّر عدد مرات الشغل ده وتكلفته.

### السبب الحقيقي لاستنزاف البطارية والتهنيج عند الفتح

1. **كان بيتعمل `RenderScript` context جديد كل recomposition** - الـ modifier اللي بيمتلك مسار الـ blur كان بيتبني من جديد كل مرة خاصية متحركة (زي الـ elevation وقت الضغط) بتتغيّر - يعني على API أقل من 31، كان بيتعمل `RenderScript` context جديد (من أتقل الكائنات في أندرويد) وبيتقفل لحد 60 مرة/ثانية، لكل كومبوننت نيومورفيزم.
2. **صفر caching خالص.** كارتين متطابقين في `LazyColumn`، أو زرار بيرجع لنفس الـ elevation بعد ما يتضغط، كل واحد فيهم كان بيولّد `GradientDrawable` + `Bitmap` + blur كامل على CPU بمفرده، رغم إن الناتج متطابق بكسل ببكسل.
3. **الـ blur كان بيشتغل على الدقة الكاملة للـ bitmap**، رغم إن الـ blur نفسه (Gaussian/stack blur) بيفقد التفاصيل الدقيقة أصلاً - يعني الدقة الزيادة دي كانت بتترمي من غير فايدة.
4. **كل كومبوننت نيومورفيزم كان عنده RenderScript context خاص بيه.** شاشة عادية (هيدر، شريط بحث، صف من الأزرار السريعة، كذا كارت، سويتشات) بسهولة فيها 15-20+ كومبوننت نيومورفيزم. حتى مع إصلاح رقم 1، كان لسه فيه 15-20+ context منفصل بيتعملوا كلهم في نفس الوقت أثناء أول فريم - وده تقيل بما يكفي على الـ main thread إنه يبان كتهنيج/تجميد للتطبيق لحظة الفتح.

### إيه اللي اتغيّر

| الإصلاح | الأثر |
|---|---|
| `BlurMaker` (والـ `RenderScript` بتاعه) بقى **singleton واحد على مستوى التطبيق كله** (`NeuBlurMakerHolder`) مشترك بين كل استدعاء `neumorphic()` وكل XML view | أقصى حاجة، context واحد بس يتعمل طول عمر التطبيق مهما كان عدد الكومبوننتس - ده اللي بيحل مشكلة التهنيج عند الفتح |
| إعادة استخدام نفس `ScriptIntrinsicBlur` بدل إنشاءه كل مرة | يقلل تكلفة مسار RenderScript المتبقية أكتر |
| كاش LRU مشترك على مستوى التطبيق للظلال (`NeuShadowCache`)، بمفتاح مبني على الحجم/الـ elevation/الألوان/الشكل/مصدر الإضاءة | الكومبوننتس المتطابقة (عناصر list، أزرار في وضع الراحة) بتشارك نفس الـ bitmap بدل ما كل واحد يولّد بتاعه |
| تقريب الـ elevation/الـ stroke لأقرب 0.5dp في مفتاح الكاش | أنيميشن ضغط بـ ~200 فريم بيتحول لعدد بسيط من الـ bitmaps المُعاد استخدامها بدل واحد لكل فريم - الفرق مش محسوس بصريًا |
| الـ blur بقى بيشتغل على دقة مخفّضة (÷2) وبعدين يكبّر تاني | تقريبًا 4 أضعاف بكسلات أقل بيلمسها الـ CPU blur loop، من غير أي فرق ملحوظ في الجودة لأن الـ blur أصلاً بيفقد التفاصيل دي |

الـ API العام متغيرش خالص - `neumorphic()`، `animatedNeumorphic()`، `springNeumorphic()`، `expressiveNeumorphic()`، وكل الـ XML views شغالة زي ما هي بالظبط.

### اختياري: تسخين مسبق (warm-up) قبل أول فريم

الـ `RenderScript` context المشترك لسه بيتعمل lazy عند أول استخدام - افتراضيًا ده أول فريم بيرسم فيه كومبوننت نيومورفيزم. لو أول شاشة في تطبيقك مليانة كومبوننتس نيومورفيزم، تقدر تلغي التأخير ده تمامًا بعمل warm-up في الخلفية قبل ما الواجهة تحتاجه:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // في الخلفية - بيعمل الـ RenderScript context المشترك بدري
        // عشان أول فريم نيومورفيزم متستناش عليه.
        Thread { NeuBlurMakerHolder.warmUp(this) }.start()
    }
}
```

ده تحسين اختياري بالكامل - آمن تتخطاه، أو تناديه كذا مرة، أو تناديه من أي مكان؛ الـ singleton بيعمل الشغل التقيل مرة واحدة بس.

### تطبيق الديمو: حجم أصغر وكود مظبوط

موديول الـ `app` (تطبيق الديمو في الريبو، مش المكتبة نفسها) كان فيه كمان شوية مشاكل مش متعلقة بالأداء خلته أكبر وأبطأ في التثبيت من غير داعي:

- شلت `material-icons-extended` (~5000 أيقونة، كذا ميجا) - الديمو بيستخدم حوالي 10 أيقونات بس، وكلها موجودة في `material-icons-core` الأصغر بكتير.
- شلت `navigation-compose` والـ 3 مكتبات `material3-adaptive` - مش مستخدمين نهائي في كود الديمو.
- فعّلت `minifyEnabled` + `shrinkResources` لنسخة الـ release (كانت `minifyEnabled false`، يعني الـ APK بينشحن من غير أي تقليص).
- صلّحت شرط ميت (dead conditional) في أيقونة الإشعارات بالهيدر كان بيرجع نفس الأيقونة في الحالتين (مفعّل/معطّل) بغض النظر عن الحالة.

### ضبط تكلفة الكاش/البلور عن طريق `NeuPerformanceConfig`

القيم الافتراضية (downsampling ÷2، كاش 6MB) مختارة عشان تكون آمنة لمعظم الواجهات. لو تطبيقك مستهدف أجهزة ضعيفة، أو عنده عدد كبير وغير عادي من الأشكال النيومورفيزم *المختلفة* في نفس الوقت (يعني الكاش المشترك بيستفاد منه أقل):

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NeuPerformanceConfig.blurDownsampling = 3        // أجهزة ضعيفة: CPU أقل، ظل أنعم شوية
        NeuPerformanceConfig.shadowCacheBudgetKB = 12 * 1024  // واجهة كبيرة/متنوعة: تفريغ كاش أقل
    }
}
```

اضبطها مرة واحدة، بدري، قبل أي رسم لكومبوننت نيومورفيزم. آمن تغيّرها بعدين كمان - التغيير بيأثر بس على الظلال اللي هتتولّد بعد كده، مش الحاجات المرسومة بالفعل.

### Baseline Profiles

المكتبة بتشحن ملف `baseline-prof.txt` مدمج (في `library/src/main/`) فيه أهم الكلاسات اللي بتشتغل باستمرار (مسار الـ blur، كاش الظلال، شجرة الأشكال). AGP بيدمجه تلقائيًا في baseline profile أي تطبيق بيستخدم المكتبة وقت البناء - من غير أي إعداد إضافي منك غير إنك تعتمد على المكتبة عادي.

عشان أكون صريح: ده ملف مكتوب يدويًا على مستوى الكلاس بس (مش method-level)، مش متولّد من قياس فعلي على جهاز. هو بيقول لـ ART "استنى الكلاسات دي بدري، اعمل preload/verify ليها قبل ما تتحتاج" - فايدة حقيقية بس متواضعة. متعمدين مافيهوش قواعد على مستوى الدوال لـ composable functions زي `neumorphic()`/`animatedNeumorphic()`/`springNeumorphic()`، لأن الـ Compose compiler بيولّد أسماء داخلية معقدة (mangled) ودوال default-argument صناعية، صعب تتخمّن صح من غير أدوات فعلية. لو عايز بروفايل أدق (يشمل نقاط الدخول دي كمان)، تقدر تولّده من جهاز حقيقي عن طريق [Macrobenchmark + Baseline Profile Gradle plugin](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile) على تطبيق الديمو، وتدمج السطور المهمة في الملف المدمج.

### خطط مستقبلية (Roadmap)

- الترقية لـ AGP 9.1.0+ / Compose 1.12+ / `compileSdk 37` لما نتأكد من مساحة التغييرات اللي AGP 9 بيعملها (بتغيّر طريقة تطبيق Kotlin Gradle plugin) على بناء فعلي - المشروع ده متعمد يأجل القفزة دي دلوقتي (شوف [المتطلبات](#المتطلبات) فوق).
- دراسة الـ blur عن طريق `RenderEffect`/GPU compositor على API 31+ كبديل بدون أي تكلفة CPU لمسار StackBlur المخفّض الدقة الحالي (مخاطرة تنفيذ أعلى - محتاج تأكيد على جهاز حقيقي قبل النشر).
- توليد baseline profile حقيقي عن طريق Macrobenchmark (الحالي مكتوب يدويًا وعلى مستوى الكلاس بس، شوف قسم [Baseline Profiles](#baseline-profiles) فوق) لما يبقى متاح جهاز/إيميوليتر أشغّله عليه ضد تطبيق الديمو.
- توسيع `NeuPerformanceConfig` أكتر لو الاستخدام الفعلي وضّح احتياج ليه (زي إعدادات مخصصة لكل شكل، أو تعطيل الـ downsampling للأسطح المرتفعة جدًا اللي ممكن يبان فيها الفرق).

## الترقية من v1.x

```kotlin
// v1.x
Modifier.neumorphic(
    neuShape = Punched.Rounded(),
    elevation = 6.dp
)

// v2.0 - نفس الـ API، مميزات جديدة
Modifier.neumorphic(
    neuShape = Punched.Rounded(),
    elevation = 6.dp,
    lightSource = LightSource.TOP_LEFT // جديد
)
```

## المتطلبات

- **الحد الأدنى للـ SDK**: 24 (أندرويد 7.0) لكل من `library` (Compose) و`library-views` (XML/Views)
- **Compile/Target SDK**: 36 (أقصى حد موصى بيه من AGP 8.13.0 - التفاصيل تحت)
- **Compose BOM**: 2026.04.01 (Compose 1.11)
- **Kotlin**: 2.3.0
- **AGP**: 8.13.0 (متطلب وقت البناء للمساهمين في المكتبة فقط؛ مش قيد على مستخدمي المكتبة)
- **Java**: توافقية 17 (source/target)

> **ليه مش أحدث BOM؟** Compose 1.12.0 (BOM 2026.08.00 وبعده) محتاج `compileSdk 37`، واللي بدوره محتاج AGP 9.1.0+. المشروع ده متعمد يفضل على AGP 8.13.x دلوقتي (شوف [خطط مستقبلية](#خطط-مستقبلية-roadmap))، فمثبّت على Compose 1.11 (BOM `2026.04.01`) - آخر نسخة قبل القفزة دي، وأتأكد من توافقها مع `compileSdk 36` + AGP 8.13 فعليًا عن طريق CI. كمان `material3` بقى مش مثبّت على alpha مستقل زي الأول لنفس السبب: تثبيت alpha لوحده محتاج `compileSdk 37` بمفرده (بغض النظر عن باقي الـ BOM) بالظبط ده اللي كسّر البناء قبل كده - سيبان الـ BOM يدير إصدار `material3` بيخلي كل حاجة على خط واحد متسق ومُختبر.

## الرخصة

مرخّص تحت Apache License, Version 2.0 [هنا](https://github.com/CuriousNikhil/neumorphic-compose/blob/main/LICENSE)

## المساهمة

المساهمات مرحب بها! لا تتردد في تقديم issues و pull requests.

## شكر وتقدير

- خوارزمية Stack Blur بواسطة Mario Klingemann
- فكرة تصميم Neumorphism الأصلية من Alexander Plyuto

</div>
