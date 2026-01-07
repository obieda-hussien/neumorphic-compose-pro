package me.nikhilchaudhari.neumorphiccompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.NeuInsets
import me.nikhilchaudhari.library.NeuTheme
import me.nikhilchaudhari.library.components.NeuButton
import me.nikhilchaudhari.library.components.NeuCard
import me.nikhilchaudhari.library.components.NeuCheckbox
import me.nikhilchaudhari.library.components.NeuChip
import me.nikhilchaudhari.library.components.NeuCircularProgress
import me.nikhilchaudhari.library.components.NeuFloatingActionButton
import me.nikhilchaudhari.library.components.NeuIconButton
import me.nikhilchaudhari.library.components.NeuProgressBar
import me.nikhilchaudhari.library.components.NeuRadioButton
import me.nikhilchaudhari.library.components.NeuSwitch
import me.nikhilchaudhari.library.components.NeuTextField
import me.nikhilchaudhari.library.expressiveNeumorphic
import me.nikhilchaudhari.library.expressiveNeumorphicClickable
import me.nikhilchaudhari.library.neumorphic
import me.nikhilchaudhari.library.shapes.Pot
import me.nikhilchaudhari.library.shapes.Pressed
import me.nikhilchaudhari.library.shapes.Punched
import me.nikhilchaudhari.library.softNeumorphic
import me.nikhilchaudhari.neumorphiccompose.ui.theme.NeumorphismComposeTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            NeumorphismComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val neuColorScheme = if (isSystemInDarkTheme()) {
        NeuTheme.DarkColorScheme
    } else {
        NeuTheme.LightColorScheme
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(neuColorScheme.backgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        item {
            HeaderSection(colorScheme = neuColorScheme)
        }
        
        // Search Bar
        item {
            SearchBarSection(colorScheme = neuColorScheme)
        }
        
        // Quick Actions
        item {
            QuickActionsSection(colorScheme = neuColorScheme)
        }
        
        // Main Cards Section
        item {
            MainCardsSection(colorScheme = neuColorScheme)
        }
        
        // Interactive Controls
        item {
            InteractiveControlsSection(colorScheme = neuColorScheme)
        }
        
        // Progress & Selection Section
        item {
            ProgressAndSelectionSection(colorScheme = neuColorScheme)
        }
        
        // Chips Section
        item {
            ChipsSection(colorScheme = neuColorScheme)
        }
        
        // Bottom Navigation Preview
        item {
            BottomNavigationSection(colorScheme = neuColorScheme)
        }
        
        // Spacing at the bottom
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection(colorScheme: NeuTheme.NeuColorScheme) {
    var notificationEnabled by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackgroundColor.copy(alpha = 0.7f)
            )
            Text(
                text = "Neumorphic UI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackgroundColor
            )
        }
        
        NeuIconButton(
            onClick = { notificationEnabled = !notificationEnabled },
            colorScheme = colorScheme,
            size = 52.dp
        ) {
            Icon(
                imageVector = if (notificationEnabled) Icons.Filled.Notifications else Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = if (notificationEnabled) colorScheme.accentColor else colorScheme.onBackgroundColor
            )
        }
    }
}

@Composable
private fun SearchBarSection(colorScheme: NeuTheme.NeuColorScheme) {
    var searchText by remember { mutableStateOf("") }
    
    NeuTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = "Search anything...",
        colorScheme = colorScheme,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
            )
        }
    )
}

@Composable
private fun QuickActionsSection(colorScheme: NeuTheme.NeuColorScheme) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackgroundColor
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionItem(
                icon = Icons.Default.Home,
                label = "Home",
                colorScheme = colorScheme
            )
            QuickActionItem(
                icon = Icons.Default.Person,
                label = "Profile",
                colorScheme = colorScheme
            )
            QuickActionItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                colorScheme = colorScheme
            )
            QuickActionItem(
                icon = Icons.Default.Favorite,
                label = "Favorites",
                colorScheme = colorScheme
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    colorScheme: NeuTheme.NeuColorScheme
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .expressiveNeumorphicClickable(
                    onClick = { isPressed = !isPressed },
                    colorScheme = colorScheme,
                    neuShape = Punched.Rounded(16.dp),
                    elevation = 8.dp
                )
                .background(colorScheme.backgroundColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) colorScheme.accentColor else colorScheme.onBackgroundColor,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onBackgroundColor
        )
    }
}

@Composable
private fun MainCardsSection(colorScheme: NeuTheme.NeuColorScheme) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackgroundColor
        )
        
        // Main Featured Card with Pot shape
        NeuCard(
            colorScheme = colorScheme,
            neuShape = Pot.Rounded(20.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Material 3 Expressive",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackgroundColor
                        )
                        Text(
                            text = "Spring physics animations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackgroundColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .neumorphic(
                                neuShape = Punched.Oval(),
                                lightShadowColor = colorScheme.lightShadowColor,
                                darkShadowColor = colorScheme.darkShadowColor,
                                elevation = 6.dp
                            )
                            .background(colorScheme.accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Check",
                            tint = colorScheme.accentColor
                        )
                    }
                }
                
                // Pressed inner section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .neumorphic(
                            neuShape = Pressed.Rounded(16.dp),
                            lightShadowColor = colorScheme.lightShadowColor,
                            darkShadowColor = colorScheme.darkShadowColor,
                            strokeWidth = 6.dp
                        )
                        .background(colorScheme.backgroundColor, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This card showcases the Pot shape with an inner Pressed section, demonstrating the depth variations possible with neumorphic design.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onBackgroundColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Two smaller cards in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NeuCard(
                colorScheme = colorScheme,
                modifier = Modifier.weight(1f),
                elevation = 8.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Punched",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackgroundColor
                    )
                    Text(
                        text = "Raised effect",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .neumorphic(
                        neuShape = Pressed.Rounded(20.dp),
                        lightShadowColor = colorScheme.lightShadowColor,
                        darkShadowColor = colorScheme.darkShadowColor,
                        strokeWidth = 5.dp
                    )
                    .background(colorScheme.backgroundColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pressed",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackgroundColor
                    )
                    Text(
                        text = "Sunken effect",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveControlsSection(colorScheme: NeuTheme.NeuColorScheme) {
    var switchState by remember { mutableStateOf(false) }
    var buttonCount by remember { mutableIntStateOf(0) }
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Interactive Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackgroundColor
        )
        
        NeuCard(
            colorScheme = colorScheme,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                // Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onBackgroundColor
                        )
                        Text(
                            text = "Spring animated toggle",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                        )
                    }
                    
                    NeuSwitch(
                        checked = switchState,
                        onCheckedChange = { switchState = it },
                        colorScheme = colorScheme
                    )
                }
                
                // Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Counter: $buttonCount",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onBackgroundColor
                        )
                        Text(
                            text = "Tap to increment",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                        )
                    }
                    
                    NeuButton(
                        onClick = { buttonCount++ },
                        colorScheme = colorScheme,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Add")
                        }
                    }
                }
                
                // Slider-like progress
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onBackgroundColor
                        )
                        Text(
                            text = "${(sliderValue * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.accentColor
                        )
                    }
                    
                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .neumorphic(
                                neuShape = Pressed.Rounded(6.dp),
                                lightShadowColor = colorScheme.lightShadowColor,
                                darkShadowColor = colorScheme.darkShadowColor,
                                strokeWidth = 3.dp
                            )
                            .background(colorScheme.backgroundColor, RoundedCornerShape(6.dp))
                    ) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = sliderValue,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "progressAnimation"
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorScheme.accentColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        )
                    }
                    
                    // Increase/Decrease buttons for slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NeuButton(
                            onClick = { sliderValue = (sliderValue - 0.1f).coerceIn(0f, 1f) },
                            colorScheme = colorScheme,
                            elevation = 6.dp,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text("-10%")
                        }
                        
                        NeuButton(
                            onClick = { sliderValue = (sliderValue + 0.1f).coerceIn(0f, 1f) },
                            colorScheme = colorScheme,
                            elevation = 6.dp,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text("+10%")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressAndSelectionSection(colorScheme: NeuTheme.NeuColorScheme) {
    var progressValue by remember { mutableFloatStateOf(0.65f) }
    var selectedRadio by remember { mutableIntStateOf(0) }
    var checkbox1 by remember { mutableStateOf(true) }
    var checkbox2 by remember { mutableStateOf(false) }
    var checkbox3 by remember { mutableStateOf(true) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Progress & Selection",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackgroundColor
        )
        
        NeuCard(
            colorScheme = colorScheme,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                // Progress Bars Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Progress Bars",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackgroundColor
                    )
                    
                    // Linear Progress
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Download Progress",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onBackgroundColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${(progressValue * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.accentColor
                            )
                        }
                        NeuProgressBar(
                            progress = progressValue,
                            colorScheme = colorScheme,
                            trackHeight = 12.dp
                        )
                    }
                    
                    // Circular Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeuCircularProgress(
                                progress = progressValue,
                                colorScheme = colorScheme,
                                size = 64.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Storage",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeuCircularProgress(
                                progress = 0.35f,
                                colorScheme = colorScheme,
                                size = 64.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Memory",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NeuCircularProgress(
                                progress = 0.82f,
                                colorScheme = colorScheme,
                                size = 64.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Battery",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onBackgroundColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // Progress control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NeuButton(
                            onClick = { progressValue = (progressValue - 0.15f).coerceIn(0f, 1f) },
                            colorScheme = colorScheme,
                            elevation = 6.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("-15%")
                        }
                        NeuButton(
                            onClick = { progressValue = (progressValue + 0.15f).coerceIn(0f, 1f) },
                            colorScheme = colorScheme,
                            elevation = 6.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("+15%")
                        }
                    }
                }
                
                // Radio Buttons Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Radio Buttons",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackgroundColor
                    )
                    
                    val options = listOf("Option 1", "Option 2", "Option 3")
                    options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NeuRadioButton(
                                selected = selectedRadio == index,
                                onClick = { selectedRadio = index },
                                colorScheme = colorScheme
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onBackgroundColor
                            )
                        }
                    }
                }
                
                // Checkboxes Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Checkboxes",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackgroundColor
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeuCheckbox(
                            checked = checkbox1,
                            onCheckedChange = { checkbox1 = it },
                            colorScheme = colorScheme
                        )
                        Text(
                            text = "Enable notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackgroundColor
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeuCheckbox(
                            checked = checkbox2,
                            onCheckedChange = { checkbox2 = it },
                            colorScheme = colorScheme
                        )
                        Text(
                            text = "Dark mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackgroundColor
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NeuCheckbox(
                            checked = checkbox3,
                            onCheckedChange = { checkbox3 = it },
                            colorScheme = colorScheme
                        )
                        Text(
                            text = "Auto-sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackgroundColor
                        )
                    }
                }
                
                // FAB Preview
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Floating Action Button",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackgroundColor
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeuFloatingActionButton(
                            onClick = { },
                            colorScheme = colorScheme,
                            size = 48.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        NeuFloatingActionButton(
                            onClick = { },
                            colorScheme = colorScheme,
                            size = 56.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        NeuFloatingActionButton(
                            onClick = { },
                            colorScheme = colorScheme,
                            size = 64.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipsSection(colorScheme: NeuTheme.NeuColorScheme) {
    var selectedChip by remember { mutableIntStateOf(0) }
    val chips = listOf("All", "Design", "Code", "Music", "Art", "Photo")
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackgroundColor
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chips.size) { index ->
                NeuChip(
                    onClick = { selectedChip = index },
                    selected = selectedChip == index,
                    colorScheme = colorScheme,
                    leadingIcon = if (selectedChip == index) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                ) {
                    Text(chips[index])
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationSection(colorScheme: NeuTheme.NeuColorScheme) {
    var selectedNav by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        Icons.Default.Home to "Home",
        Icons.Default.Search to "Search",
        Icons.Default.Add to "Add",
        Icons.Outlined.FavoriteBorder to "Likes",
        Icons.Default.Person to "Profile"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Navigation Bar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackgroundColor
        )
        
        // Clean navigation bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .neumorphic(
                    neuShape = Punched.Rounded(28.dp),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 8.dp
                )
                .background(colorScheme.backgroundColor, RoundedCornerShape(28.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, (icon, label) ->
                    val isSelected = selectedNav == index
                    
                    NeuIconButton(
                        onClick = { selectedNav = index },
                        colorScheme = colorScheme,
                        size = 48.dp
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) colorScheme.accentColor else colorScheme.onBackgroundColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainContentPreview() {
    NeumorphismComposeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            MainContent()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainContentDarkPreview() {
    NeumorphismComposeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            MainContent()
        }
    }
}