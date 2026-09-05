<div dir="rtl">

# Neumorphism UI لأندرويد

مكتبة حديثة لتصميم واجهات **Neumorphism** على أندرويد، بدعم كامل لكل من **Jetpack Compose** و**XML / Java Views** التقليدية.

الفورك ده بيركّز على 3 حاجات أساسية: **شكل النيومورفيزم، تفاعل متوقع وسليم، ورسم الظلال بكفاءة أعلى**. الإصدار `4.0.1` بيكمّل تحسينات 4.0.0 وبيشدّد على الـ renderer، والـ cache، وتحديث الـ Views، وSemantics الخاصة بـ Compose، والتزامن، واختبارات الأداء، مع الحفاظ على الـ public component API بدون migration إجباري.

[![JitPack](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro)

<p align="center">
  <img src="https://github.com/obieda-hussien/neumorphic-compose-pro/blob/main/static/complete_screen.png?raw=true" height="400" alt="Neumorphic Compose demo">
</p>

## ليه الفورك ده؟

الـ renderer الأصلي كان ممكن يعيد إنشاء موارد الظلال ويعيد تشغيل الـ blur بشكل متكرر، خصوصًا مع الأنيميشن وكثرة العناصر. النسخة دي بتضيف بنية مشتركة للرسم وكاش للظلال بحيث العناصر المتكررة تقدر تعيد استخدام نتائج متوافقة بدل إعادة حسابها بدون داعي.

الهدف مش تغيير الـ Neumorphism نفسه أو استبدال شكله. الهدف إن التأثير يبقى **أثبت، أسهل في التعامل، وأقل تكلفة في حالات الاستخدام المتكررة**.

## أهم المميزات

- **Jetpack Compose**: دعم `Modifier.neumorphic()` مع النسخ المتحركة والـ expressive variants.
- **XML / Java Views**: دعم `NeumorphicView` و`NeumorphicButton` و`NeumorphicCardView` وغيرها.
- **3 أشكال**: `Punched` و`Pressed` و`Pot`.
- **4 اتجاهات للإضاءة**: أعلى-يسار، أعلى-يمين، أسفل-يسار، أسفل-يمين.
- **Light / Dark themes** مع دعم ألوان Material You الديناميكية.
- **Press وHover interactions**.
- **Accessibility semantics** لعناصر Compose التفاعلية.
- **Shadow cache مشترك** وبنية blur قابلة لإعادة الاستخدام.
- **StackBlur fallback** لمسار RenderScript القديم.
- **إدارة للكاش مع ضغط الذاكرة**.
- **Regression tests وMacrobenchmark smoke coverage**.

## الجديد في 4.0.1

`4.0.1` إصدار hardening مبني على تغييرات renderer في 4.0.0. مفيش migration إجباري للـ public API.

### تحسينات الرسم والأداء

- حماية بنية `BlurMaker` المشتركة أثناء الاستدعاءات المتوازية.
- فصل مسارات الـ blur إلى RenderScript وStackBlur مع fallback عند فشل المسار القديم.
- إعادة استخدام موارد RenderScript بدل إعادة إنشائها مع كل عملية blur.
- تحويل Allocation cache إلى LRU حقيقي باستخدام access-order.
- مفاتيح `NeuShadowCache` بتحافظ على هوية الألوان والـ float values بدقة، وبتضم إعداد جودة الـ blur.
- أبعاد الـ downsampling بقت محسوبة بطريقة ceil عشان آخر جزء من الـ bitmap مايضيعش.
- الكاش بيتعامل مع إشعارات ضغط الذاكرة وبيعمل clear / re-budget بشكل آمن.
- cache hits في Compose بتتجنب إنشاء shadow drawable إضافي بدون داعي.
- invalidation الخاصة بالـ Views بقت مركزية، فتغييرات خصائص الظل وقت التشغيل تعمل redraw بشكل متوقع.
- `NeumorphicCardView` بيفصل بين padding المحتوى وهندسة الظل.
- `NeuAnimationType.NONE` بيطبق القيمة النهائية مباشرة بدل animation صناعي شديد.

### تحسينات Compose والتفاعل وAccessibility

- `NeuSlider` بقى تفاعلي فعلًا وبيستجيب للضغط والسحب ضمن النطاق الصحيح.
- `NeuButton` و`NeuIconButton` و`NeuFloatingActionButton` بستخدموا `hoverable` على نفس `InteractionSource` اللي بيتجمع منه hover state.
- `NeuCircularProgress(progress = null)` بقى مؤشر indeterminate متحرك بدل track ثابت.
- `Switch` و`Checkbox` و`RadioButton` بيعرضوا semantics مرتبطة بالحالة الحالية.
- `Slider` و`SeekBar` بيعرضوا progress-range semantics.
- قيم الـ progress وهندسة الـ Slider محمية من القيم الخارجة عن النطاق، بما في ذلك layouts بعرض صفر.
- حسابات الـ circular progress بتستخدم أبعاد الرسم الفعلية بدل التعارض مع parameter اسمه `size`.

### الاختبارات والـ CI

- Compose UI regression tests للتفاعل والـ semantics.
- Unit tests لهوية الـ shadow cache وسلوك الـ blur المشترك.
- تغطية لمسارات concurrent blur وضغط الكاش.
- benchmark smoke لاختبار زمن الـ blur على الجهاز.
- Module مستقل للـ Macrobenchmark لاختبارات startup / frame smoke.
- تطبيق الديمو profileable لتسهيل تشخيص الأداء.
- CI مقسوم إلى jobs مستقلة للبناء، والـ unit tests، والـ lint، والـ instrumentation، والـ Macrobenchmark.

## التثبيت

المكتبة منشورة عبر [JitPack](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro).

أضف JitPack إلى مستودعات المشروع:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Jetpack Compose

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:4.0.1")
```

### XML / Views

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library-views:4.0.1")
```

الموديولين بيستخدموا نفس رقم الإصدار.

> لو جاي من الـ artifact الأصلي `me.nikhilchaudhari:composeNeumorphism`، أنماط استخدام الـ public API الأساسية مازالت متوافقة. غيّر dependency coordinates وراجع dependency graph الخاص بتطبيقك قبل النشر.

## البداية السريعة

### Jetpack Compose

```kotlin
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.neumorphic

@Composable
fun Example() {
    Text(
        text = "Hello Neumorphism",
        modifier = Modifier
            .padding(16.dp)
            .neumorphic()
    )
}
```

### تخصيص تأثير Compose

```kotlin
Modifier.neumorphic(
    neuShape = Punched.Rounded(radius = 12.dp),
    elevation = 8.dp,
    lightShadowColor = Color.White,
    darkShadowColor = Color.Gray,
    lightSource = LightSource.TOP_LEFT
)
```

### أنيميشن الضغط

```kotlin
Modifier.animatedNeumorphic(
    neuShape = Punched.Rounded(),
    elevation = 8.dp,
    pressed = isPressed,
    animationDuration = 150
)
```

### Spring animation

```kotlin
Modifier.springNeumorphic(
    neuShape = Punched.Rounded(),
    elevation = 8.dp,
    pressed = isPressed,
    animationType = NeuAnimationType.SPRING_BOUNCY
)
```

### XML

```xml
<me.nikhilchaudhari.library.views.NeumorphicButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Neumorphic Button"
    app:neuShape="punched"
    app:neuCornerRadius="12dp"
    app:neuElevation="8dp"
    app:neuLightShadowColor="@color/white"
    app:neuDarkShadowColor="@color/gray"
    app:neuLightSource="topLeft" />
```

### Java

```java
NeumorphicButton button = new NeumorphicButton(context);
button.setNeuShapeType(NeuShapeType.PUNCHED);
button.setNeuCornerRadius(dpToPx(12));
button.setNeuElevation(dpToPx(8));
button.setNeuLightSource(LightSource.TOP_LEFT);
```

## الأشكال

| الشكل | المظهر | الاستخدام المناسب |
| --- | --- | --- |
| `Punched` | بارز / مرتفع | الكروت والأزرار والأسطح |
| `Pressed` | غائر / منخفض | الحقول والحالات المضغوطة والأسطح الغائرة |
| `Pot` | مزيج من البارز والغائر | الحاويات والأسطح المركبة |

كل شكل عنده نسختين للحواف:

```kotlin
Punched.Rounded(radius = 12.dp)
Punched.Oval()
Pressed.Rounded(radius = 8.dp)
Pressed.Oval()
Pot.Rounded(radius = 16.dp)
Pot.Oval()
```

## أهم معاملات التخصيص

| المعامل | الافتراضي | الوصف |
| --- | --- | --- |
| `neuShape` | `Punched.Rounded()` | هندسة الظل والشكل |
| `lightShadowColor` | `Color.White` | لون الظل ناحية الإضاءة |
| `darkShadowColor` | `Color.LightGray` | لون الظل الداكن |
| `elevation` | `6.dp` | عمق الظل |
| `strokeWidth` | `6.dp` | عرض الـ inner shadow |
| `neuInsets` | `NeuInsets(6.dp, 6.dp)` | هوامش الظل أفقيًا ورأسيًا |
| `lightSource` | `TOP_LEFT` | اتجاه الإضاءة |

## مصدر الإضاءة

```kotlin
Modifier.neumorphic(
    lightSource = LightSource.TOP_LEFT
)
```

القيم المتاحة:

`TOP_LEFT` · `TOP_RIGHT` · `BOTTOM_LEFT` · `BOTTOM_RIGHT`

الأفضل غالبًا إنك تستخدم نفس اتجاه الإضاءة على مستوى الشاشة أو الـ design system عشان الشكل يفضل متناسق.

## دعم الثيمات

المكتبة بتوفر helpers لألوان الثيم الفاتح والداكن، بالإضافة إلى ألوان Material You الديناميكية.

```kotlin
val colorScheme = NeuTheme.colorScheme()
```

وللألوان الديناميكية على Android 12+:

```kotlin
val dynamicScheme = NeuTheme.dynamicColorScheme()
```

وتقدر تعمل color scheme مخصص:

```kotlin
val customScheme = NeuTheme.customColorScheme(
    backgroundColor = Color(0xFFE0E5EC)
)
```

## دوال مساعدة

```kotlin
Modifier.softNeumorphic()
Modifier.deepNeumorphic()

val (lightShadow, darkShadow) = backgroundColor.toNeuColors()

val lighter = color.lighten(0.2f)
val darker = color.darken(0.2f)
```

## الأداء

معظم شغل 4.0.x مركز على **تقليل إعادة حساب الظلال بدون داعي**.

### بنية Blur مشتركة

بدل إنشاء RenderScript context مستقل لكل كومبوننت، فيه مسار blur مشترك قابل لإعادة الاستخدام. ومسار RenderScript القديم منفصل عن StackBlur، بحيث فشل الـ backend القديم مايبقاش معناه إن البنية المشتركة كلها اتسممت بحالة فشل دائمة.

### Shadow cache مشترك

الظلال المتولدة بتتخزن في LRU cache على مستوى العملية، والمفتاح مبني من العوامل المؤثرة في النتيجة، زي الحجم والـ elevation والـ stroke والألوان والشكل ومصدر الإضاءة وجودة الـ blur.

ده مفيد جدًا مع:

- عناصر متكررة داخل `LazyColumn`
- كروت متشابهة
- أزرار بترجع لنفس elevation بعد الضغط
- قيم elevation متحركة بتعيد زيارة نفس الـ cache buckets

### Blur downsampling

`NeuPerformanceConfig.blurDownsampling` بيتحكم في الدقة اللي بيشتغل عليها الـ blur.

قيم أعلى بتقلل عدد البكسلات وبالتالي تكلفة الـ blur واستهلاك الذاكرة، لكن ممكن تخلي الظلال أنعم أو أقل تفاصيل.

```kotlin
NeuPerformanceConfig.blurDownsampling = 3
```

### ميزانية الكاش

`NeuPerformanceConfig.shadowCacheBudgetKB` بتحدد الميزانية التقريبية للكاش:

```kotlin
NeuPerformanceConfig.shadowCacheBudgetKB = 12 * 1024
```

يفضل ضبط الإعدادات دي بدري أثناء تشغيل التطبيق. تقدر تغيّرها لاحقًا كمان، والظلال اللي هتتولد بعد التغيير هتستخدم الإعدادات الجديدة.

### Baseline Profiles

المكتبة فيها baseline profile صغير ومكتوب يدويًا، ومركّز على بعض الـ internal classes اللي بتستخدم بكثرة. هو متعمد يكون محافظ ومش بديل عن قياس حقيقي للتطبيق.

لأفضل نتيجة على مستوى التطبيق نفسه، استخدم Macrobenchmark / Baseline Profile tooling على جهاز فعلي واعمل profile مبني على الـ user flows الحقيقية لتطبيقك.

## أفضل الممارسات

### خلي الألوان متناسقة

اختار background وshadow palette من نفس العائلة اللونية. الدرجات القريبة من الأبيض والرمادي غالبًا بتطلع نتيجة أكثر طبيعية من الأبيض/الأسود الصريحين.

### حافظ على اتجاه إضاءة ثابت

اختار `LightSource` ثابت للشاشة أو الـ design system إلا لو عندك سبب تصميمي واضح لتغييره.

### استخدم elevation معقول

النطاق من `4.dp` إلى `12.dp` نقطة بداية مناسبة لمعظم العناصر. القيم الأعلى ممكن تشتغل، لكن بتزيد مساحة الظل وتكلفة الرسم.

### استخدم clipping بحذر

ظلال `Punched` البارزة و`Pot` المركبة مقصود إنها تمتد خارج حدود العنصر. لو استخدمت `Modifier.clip()` قبل `Modifier.neumorphic()` ممكن تقطع الامتداد الناعم للظل.

استخدم clipping بشكل مقصود مع `Pressed` لما تحتاج إن الـ inner shadow يفضل محصور داخل العنصر. الـ `NeuXxx` components الجاهزة في المكتبة بتتعامل مع هندسة الظلال الداخلية الخاصة بيها.

## الترقية

`4.0.1` لا يفرض migration على public component signatures. استخدام `neumorphic()` الحالي يفضل كما هو.

أهم تغيير للمستخدم القديم هو dependency coordinate:

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:4.0.1")
```

## متطلبات المشروع

- **Minimum SDK**: 24
- **Compile / Target SDK**: 36
- **Compose BOM**: `2026.04.01`
- **Kotlin**: `2.3.0`
- **AGP**: `8.13.2` لبناء الريبو الحالي
- **Java**: 17 source/target compatibility

الاعتماد على المكتبة المنشورة لا يفرض بالضرورة نفس إصدارات أدوات البناء المستخدمة داخل الريبو، لكن مشروعك لازم يكون متوافقًا مع قيود Android / Compose اللي بتستخدمها.

## مكونات المشروع

| الموديول | الوظيفة |
| --- | --- |
| `library` | تنفيذ Jetpack Compose |
| `library-views` | تنفيذ XML / Android Views |
| `app` | تطبيق الديمو |
| `macrobenchmark` | اختبارات startup / frame للأداء |

## المساهمة

الـ Issues والـ Pull Requests مرحب بيها. عند الإبلاغ عن مشكلة في الرسم أو الأداء، حاول تضمّن إصدار أندرويد، والجهاز/الإيميوليتر، والكومبوننت، والـ shape، والـ elevation، ومعها reproduction صغير لو أمكن.

## الرخصة

Apache License 2.0. راجع [LICENSE](LICENSE).

## Credits

- خوارزمية Stack Blur بواسطة Mario Klingemann
- فكرة تصميم Neumorphism الأصلية بواسطة Alexander Plyuto

</div>
