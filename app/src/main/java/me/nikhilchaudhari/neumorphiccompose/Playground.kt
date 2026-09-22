package me.nikhilchaudhari.neumorphiccompose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.nikhilchaudhari.library.*
import me.nikhilchaudhari.library.components.*
import kotlin.math.roundToInt

@Composable
fun DemoExperience(initialScreen: String? = null) {
    var page by rememberSaveable { mutableIntStateOf(when (initialScreen) { "playground" -> 1; "stress" -> 2; else -> 0 }) }
    val pages = rememberSaveableStateHolder()
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TabRow(selectedTabIndex = page) {
            listOf("Gallery", "Playground", "Stress").forEachIndexed { index, label ->
                Tab(selected = page == index, onClick = { page = index },
                    text = { Text(label, maxLines = 1) }, modifier = Modifier.testTag("tab-$index"))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            pages.SaveableStateProvider(page) {
                when (page) { 0 -> MainContent(); 1 -> Playground(); else -> StressScreen() }
            }
        }
    }
}

/** Both Material controls and neumorphic surfaces share the same background and readable foreground. */
@Composable
private fun DemoPalette(dark: Boolean, purple: Boolean = true, tokens: NeuDesignTokens = NeuDesignTokens(), content: @Composable () -> Unit) {
    val background = if (dark) Color(0xFF292836) else if (purple) Color(0xFFE8E7F5) else Color(0xFFECEAEB)
    val accent = if (dark) Color(0xFFC7C5FF) else Color(0xFF514DA2)
    val colors = NeuTheme.customColorScheme(background, accentColor = accent)
    val material = if (dark) darkColorScheme(primary = accent, surface = background, background = background,
        onSurface = Color.White, onBackground = Color.White) else lightColorScheme(primary = accent,
        surface = background, background = background, onSurface = Color(0xFF242333), onBackground = Color(0xFF242333))
    MaterialTheme(colorScheme = material) {
        NeuDesignTheme(colors, tokens) {
            Surface(Modifier.fillMaxSize(), color = background, contentColor = material.onSurface) { content() }
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
    var text by rememberSaveable { mutableStateOf("My project") }
    var knob by rememberSaveable { mutableFloatStateOf(0.4f) }
    var rangeStart by rememberSaveable { mutableFloatStateOf(0.2f) }
    var rangeEnd by rememberSaveable { mutableFloatStateOf(0.8f) }
    var dialog by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    var codeVisible by rememberSaveable { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val list = rememberLazyListState()
    val tokens = NeuDesignTokens(elevation.dp, corners.dp, LightSource.entries[light], contrast, motion)
    val clipboard = LocalClipboardManager.current
    val code = playgroundCode(elevation.roundToInt(), corners.roundToInt(), LightSource.entries[light], dark, purple, contrast, motion, recessed)
    DemoPalette(dark, purple, tokens) {
        LazyColumn(Modifier.fillMaxSize().imePadding().testTag("playground-list"), state = list,
            contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item("title") { Column { Text("Playground", style = MaterialTheme.typography.headlineMedium); Text("Adjust the surface, then copy its Kotlin code.") } }
            item("preview") {
                // Reserve space for overflow shadows even at the maximum 20dp elevation.
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    NeuSurface(Modifier.fillMaxWidth().heightIn(min = 96.dp).testTag("preview"), recessed = recessed) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Your surface", style = MaterialTheme.typography.titleLarge)
                            Text("${elevation.roundToInt()} dp depth · ${corners.roundToInt()} dp corners")
                        }
                    }
                }
            }
            item("depth") { Column { Text("Depth ${elevation.roundToInt()} dp"); Slider(elevation, { elevation = it.roundToInt().toFloat() },
                modifier = Modifier.testTag("depth-slider"), valueRange = 0f..20f, steps = 19) } }
            item("corners") { Column { Text("Corners ${corners.roundToInt()} dp"); Slider(corners, { corners = it.roundToInt().toFloat() },
                modifier = Modifier.testTag("corner-slider"), valueRange = 0f..48f, steps = 47) } }
            item("light") { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Light source"); NeuSegmentedButton(listOf("↖ TL", "↗ TR", "↙ BL", "↘ BR"), light, { light = it }) } }
            item("options") { Column { Toggle("Dark theme", dark, "playground-dark") { dark = it }; Toggle("Purple preset", purple) { purple = it }
                Toggle("Recessed surface", recessed) { recessed = it }; Toggle("High contrast", contrast) { contrast = it }; Toggle("Reduce motion", motion) { motion = it } } }
            item("text") { NeuTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), label = "Project name", maxLength = 30,
                isError = text.isBlank(), supportingText = if (text.isBlank()) "Enter a project name" else "Up to 30 characters") }
            item("knob") { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                NeuRotaryKnob(knob, { knob = it }, label = "Volume"); Text("Volume ${(knob * 100).roundToInt()}%", Modifier.weight(1f)) } }
            item("range") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Range ${(rangeStart * 100).roundToInt()}–${(rangeEnd * 100).roundToInt()}")
                NeuRangeSlider(rangeStart..rangeEnd, { rangeStart = it.start; rangeEnd = it.endInclusive }, Modifier.fillMaxWidth()) } }
            item("overlays") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton({ dialog = true }, Modifier.weight(1f).testTag("open-dialog")) {
                        Text("Dialog")
                    }
                    OutlinedButton({ sheet = true }, Modifier.weight(1f).testTag("open-sheet")) {
                        Text("Sheet")
                    }
                }
            }
            item("export") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ clipboard.setText(AnnotatedString(code)); copied = true }, Modifier.fillMaxWidth().testTag("copy-kotlin")) { Text(if (copied) "Copied Kotlin" else "Copy Kotlin") }
                TextButton({ codeVisible = !codeVisible }) { Text(if (codeVisible) "Hide code" else "Show code") }
                if (codeVisible) Text(code, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            } }
            item("stats") { PerfPanel() }
        }
        if (dialog) NeuDialog({ dialog = false }) { Text("Neumorphic dialog"); Button({ dialog = false }, Modifier.testTag("close-dialog")) { Text("Close") } }
        if (sheet) NeuBottomSheet({ sheet = false }) { Text("Neumorphic sheet"); Button({ sheet = false }, Modifier.testTag("close-sheet")) { Text("Close") } }
    }
}

internal fun playgroundCode(depth: Int, corners: Int, light: LightSource, dark: Boolean, purple: Boolean,
    contrast: Boolean, motion: Boolean, recessed: Boolean): String {
    val background = if (dark) "FF292836" else if (purple) "FFE8E7F5" else "FFECEAEB"
    val accent = if (dark) "FFC7C5FF" else "FF514DA2"
    return """
        import androidx.compose.foundation.layout.padding
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.graphics.Color
        import androidx.compose.ui.unit.dp
        import androidx.compose.material3.Text
        import me.nikhilchaudhari.library.*
        import me.nikhilchaudhari.library.components.NeuSurface

        // Inside a @Composable function:
        NeuDesignTheme(
            colors = NeuTheme.customColorScheme(Color(0x$background), accentColor = Color(0x$accent)),
            tokens = NeuDesignTokens(elevation = $depth.dp, cornerRadius = $corners.dp,
                lightSource = LightSource.$light, highContrast = $contrast, reduceMotion = $motion)
        ) {
            NeuSurface(recessed = $recessed) {
                Text("Your surface", Modifier.padding(20.dp))
            }
        }
    """.trimIndent()
}

@Composable
private fun Toggle(label: String, checked: Boolean, tag: String = label, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag).toggleable(checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f)); Switch(checked, onCheckedChange = null)
    }
}

@Composable
private fun PerfPanel() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var stats by remember { mutableStateOf(NeuPerfStats.snapshot()) }
    var frames by remember { mutableStateOf(DemoFrameStats.snapshot()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); stats = NeuPerfStats.snapshot(); frames = DemoFrameStats.snapshot() } }
    Column(Modifier.fillMaxWidth()) {
        Text("P95 %.1f ms · P99 %.1f ms · Queue %d".format(frames.first, frames.second, stats.pendingRequests), style = MaterialTheme.typography.bodySmall)
        TextButton({ expanded = !expanded }) { Text(if (expanded) "Hide diagnostics" else "Show diagnostics") }
        if (expanded) Text("Cache ${stats.cacheHits} hits / ${stats.cacheMisses} misses\n" +
            "${stats.cacheBytes / 1024} / ${stats.cacheBudgetBytes / 1024} KiB\n" +
            "Generation avg %.2f ms · Failed %d\n".format(stats.averageGenerationMs, stats.failedRequests) +
            "Backend: ${stats.lastBackend} · GPU fallbacks ${stats.gpuFallbacks}\n" +
            "GPU estimate ${stats.estimatedGpuBytes / 1024} KiB · Thermal ${stats.thermalTier} · Saver ${stats.powerSave}\n" +
            "${frames.third} rolling frame samples", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StressScreen() {
    var dark by rememberSaveable { mutableStateOf(false) }
    var animated by rememberSaveable { mutableStateOf(false) }
    var profileName by rememberSaveable { mutableStateOf(NeuPerformanceConfig.performanceClass.name) }
    val profiles = remember { NeuPerformanceClass.entries.toList() }
    val selectedProfile = profiles.first { it.name == profileName }
    DisposableEffect(Unit) {
        val previous = NeuPerformanceConfig.performanceClass
        onDispose { NeuPerformanceConfig.performanceClass = previous }
    }
    LaunchedEffect(selectedProfile) { NeuPerformanceConfig.performanceClass = selectedProfile }
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(animated) { while (animated) { delay(600); pressed = !pressed }; pressed = false }
    val depth by animateDpAsState(if (pressed) 3.dp else 8.dp, tween(220), label = "stress-depth")
    val list = rememberLazyListState()
    DemoPalette(dark) {
        // Everything can scroll. Diagnostics cannot consume the whole viewport on a small phone.
        LazyColumn(Modifier.fillMaxSize().testTag("stress-list"), state = list, contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item("controls") { Column {
                Text("Stress test", style = MaterialTheme.typography.headlineMedium)
                Text("300 surfaces · one shadow renderer per card")
                Toggle("Dark theme", dark, "stress-dark") { dark = it }
                Toggle("Animate elevations", animated, "stress-animation") { animated = it }
                Text("Performance profile")
                NeuSegmentedButton(profiles.map { it.name.lowercase().replaceFirstChar(Char::uppercaseChar) }, profiles.indexOf(selectedProfile),
                    { profileName = profiles[it].name }, Modifier.padding(vertical = 12.dp))
                PerfPanel()
            } }
            items(300, key = { "surface-$it" }, contentType = { "surface" }) { index ->
                NeuDesignTheme(tokens = LocalNeuTokens.current.copy(elevation = if (animated) ((depth.value * 2).roundToInt() / 2f).dp else 6.dp)) {
                    NeuSurface(Modifier.fillMaxWidth().testTag("stress-card-$index"),
                        shape = RoundedCornerShape((8 + index % 5 * 6).dp), recessed = index % 3 == 0) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Surface $index", style = MaterialTheme.typography.titleMedium)
                            Text(if (index % 3 == 0) "Recessed" else "Raised")
                        }
                    }
                }
            }
        }
    }
}
