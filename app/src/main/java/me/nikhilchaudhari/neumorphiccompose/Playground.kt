package me.nikhilchaudhari.neumorphiccompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.nikhilchaudhari.library.*
import me.nikhilchaudhari.library.components.*

@Composable
fun DemoExperience(initialScreen: String? = null) {
    var page by rememberSaveable { mutableIntStateOf(if (initialScreen == "stress") 2 else 0) }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        NeuTabs(listOf("Gallery", "Playground", "Stress"), page, { page = it }, Modifier.padding(12.dp))
        Box(Modifier.weight(1f)) {
            when (page) {
                0 -> MainContent()
                1 -> Playground()
                else -> StressScreen()
            }
        }
    }
}

@Composable
private fun Playground() {
    var elevation by rememberSaveable { mutableFloatStateOf(6f) }
    var corners by rememberSaveable { mutableFloatStateOf(16f) }
    var light by rememberSaveable { mutableIntStateOf(0) }
    var dark by rememberSaveable { mutableStateOf(false) }
    var contrast by rememberSaveable { mutableStateOf(false) }
    var motion by rememberSaveable { mutableStateOf(false) }
    var purple by rememberSaveable { mutableStateOf(true) }
    var recessed by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    var knob by rememberSaveable { mutableFloatStateOf(0.4f) }
    var range by remember { mutableStateOf(0.2f..0.8f) }
    var dialog by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    val background = if (dark) Color(0xFF292836) else if (purple) Color(0xFFE8E7F5) else Color(0xFFECEAEB)
    val colors = NeuTheme.customColorScheme(background, accentColor = Color(0xFF8E8DE5))
    val tokens = NeuDesignTokens(elevation.dp, corners.dp, LightSource.entries[light], contrast, motion)
    val clipboard = LocalClipboardManager.current
    val hex = if (dark) "FF292836" else if (purple) "FFE8E7F5" else "FFECEAEB"
    val code = """
        import androidx.compose.foundation.layout.padding
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.graphics.Color
        import androidx.compose.ui.unit.dp
        import androidx.compose.material3.Text
        import me.nikhilchaudhari.library.*
        import me.nikhilchaudhari.library.components.NeuSurface

        // Inside a @Composable function:
        NeuDesignTheme(
            colors = NeuTheme.customColorScheme(Color(0x$hex), accentColor = Color(0xFF8E8DE5)),
            tokens = NeuDesignTokens(elevation = ${elevation.toInt()}.dp,
                cornerRadius = ${corners.toInt()}.dp, lightSource = LightSource.${LightSource.entries[light]},
                highContrast = $contrast, reduceMotion = $motion)
        ) {
            NeuSurface(recessed = $recessed) {
                Text("Hello Neumorphism", Modifier.padding(24.dp))
            }
        }
    """.trimIndent()
    NeuDesignTheme(colors, tokens) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item { Text("Make it yours", style = MaterialTheme.typography.headlineMedium) }
            item { NeuSurface(Modifier.fillMaxWidth(), recessed = recessed) { Text("Purple · #8E8DE5", Modifier.padding(28.dp)) } }
            item { Text("Depth ${elevation.toInt()} dp"); Slider(elevation, { elevation = it }, valueRange = 0f..20f, steps = 19) }
            item { Text("Corners ${corners.toInt()} dp"); Slider(corners, { corners = it }, valueRange = 0f..48f, steps = 47) }
            item { NeuSegmentedButton(listOf("↖", "↗", "↙", "↘"), light, { light = it }) }
            item {
                Toggle("Dark", dark) { dark = it }
                Toggle("Purple preset", purple) { purple = it }
                Toggle("Recessed", recessed) { recessed = it }
                Toggle("High contrast", contrast) { contrast = it }
                Toggle("Reduce motion", motion) { motion = it }
            }
            item { NeuTextField(text, { text = it }, label = "Project name", maxLength = 30,
                isError = text.isBlank(), supportingText = if (text.isBlank()) "Enter a project name" else "Ready") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) { NeuRotaryKnob(knob, { knob = it }, label = "Volume"); Text("Volume ${(knob * 100).toInt()}%") } }
            item { NeuRangeSlider(range, { range = it }); Text("Range ${(range.start * 100).toInt()}–${(range.endInclusive * 100).toInt()}") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button({ dialog = true }) { Text("Dialog") }; Button({ sheet = true }) { Text("Sheet") } } }
            item { Button({ clipboard.setText(AnnotatedString(code)) }) { Text("Copy Kotlin") }; Text(code, style = MaterialTheme.typography.bodySmall) }
            item { PerfPanel() }
        }
        if (dialog) NeuDialog({ dialog = false }) { Text("Neumorphic dialog"); Button({ dialog = false }) { Text("Close") } }
        if (sheet) NeuBottomSheet({ sheet = false }) { Text("Neumorphic sheet"); Button({ sheet = false }) { Text("Close") } }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Switch(checked, onChange) }
}

@Composable
private fun PerfPanel() {
    var stats by remember { mutableStateOf(NeuPerfStats.snapshot()) }
    var frames by remember { mutableStateOf(DemoFrameStats.snapshot()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); stats = NeuPerfStats.snapshot(); frames = DemoFrameStats.snapshot() } }
    Text("Cache ${stats.cacheHits} hits / ${stats.cacheMisses} misses\n" +
        "${stats.cacheBytes / 1024} / ${stats.cacheBudgetBytes / 1024} KiB · Pending ${stats.pendingRequests}\n" +
        "Generation avg %.2f ms · Failed %d\n".format(stats.averageGenerationMs, stats.failedRequests) +
        "Preferred: ${stats.preferredBackend}\nThermal ${stats.thermalTier} · Saver ${stats.powerSave}\n" +
        "Frame total P95 %.2f ms · P99 %.2f ms (%d samples)".format(frames.first, frames.second, frames.third))
}

@Composable
private fun StressScreen() {
    var dark by rememberSaveable { mutableStateOf(false) }
    var animated by rememberSaveable { mutableStateOf(false) }
    var profile by rememberSaveable { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val previous = NeuPerformanceConfig.performanceClass
        onDispose { NeuPerformanceConfig.performanceClass = previous }
    }
    NeuDesignTheme(if (dark) NeuTheme.DarkColorScheme else NeuTheme.LightColorScheme) {
        Column {
            Toggle("Dark theme", dark) { dark = it }
            Toggle("Animate elevations", animated) { animated = it }
            NeuSegmentedButton(listOf("Auto", "Quality", "Battery"), profile, {
                profile = it
                NeuPerformanceConfig.performanceClass = listOf(NeuPerformanceClass.AUTO, NeuPerformanceClass.QUALITY, NeuPerformanceClass.BATTERY)[it]
            }, Modifier.padding(12.dp))
            PerfPanel()
            LazyColumn(Modifier.fillMaxSize().testTag("stress-list"), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                items((0 until 300).toList(), key = { it }) { index ->
                    StressCard(index, animated)
                }
            }
        }
    }
}

@Composable
private fun StressCard(index: Int, animated: Boolean) {
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(animated) {
        while (animated) { delay(350); pressed = !pressed }
        pressed = false
    }
    NeuSurface(Modifier.fillMaxWidth().then(if (animated) Modifier.animatedNeumorphic(pressed = pressed) else Modifier),
        shape = RoundedCornerShape((8 + index % 5 * 6).dp), recessed = index % 3 == 0) {
        Text("Surface $index · ${if (index % 3 == 0) "recessed" else "raised"}", Modifier.padding(24.dp))
    }
}
