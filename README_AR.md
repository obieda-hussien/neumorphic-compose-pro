<div dir="rtl">

# مكتبة Neumorphism UI لأندرويد

مكتبة حديثة ومرنة لواجهة Neumorphism لأندرويد تدعم كل من **Jetpack Compose** و **Views/XML** التقليدية.

## المميزات ✨

- 🎨 **دعم Jetpack Compose** - استخدم الـ `neumorphic()` modifier مع أي composable
- 📱 **دعم XML/Java Views** - NeumorphicView, NeumorphicButton, NeumorphicCardView
- 🌓 **دعم الوضع الداكن** - مخططات ألوان مدمجة للوضع الفاتح والداكن
- 🎭 **دعم Material You** - ألوان ديناميكية على أندرويد 12+
- 💫 **دعم الأنيميشن** - تأثيرات ضغط سلسة
- 🔆 **مصدر الإضاءة قابل للتخصيص** - أعلى اليسار، أعلى اليمين، أسفل اليسار، أسفل اليمين
- 🛠 **تنفيذ حديث** - انتقال من RenderScript القديم إلى StackBlur

## التثبيت

### مكتبة Jetpack Compose

```kotlin
implementation("me.nikhilchaudhari:composeNeumorphism:2.0.0")
```

### مكتبة XML/Views

```kotlin
implementation("me.nikhilchaudhari:neumorphismViews:2.0.0")
```

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
