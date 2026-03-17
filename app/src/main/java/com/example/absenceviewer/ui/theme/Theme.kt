package com.example.absenceviewer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class CustomColors(
    val banner: Color,
    val background: Color,
    val border: Color,
    val box: Color,
    val card: Color,
    val innerBox: Color,
    val onBackground: Color,
    val onBox: Color,
    val onCard: Color,
    val onInnerBox: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        banner = Color.Unspecified,
        background = Color.Unspecified,
        border = Color.Unspecified,
        box = Color.Unspecified,
        card = Color.Unspecified,
        innerBox = Color.Unspecified,
        onBackground = Color.Unspecified,
        onBox = Color.Unspecified,
        onCard = Color.Unspecified,
        onInnerBox = Color.Unspecified
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DarkColorScheme2 = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val LightColorScheme2 = lightColorScheme(
    primary = Color(0xFF6650a4),
    secondary = Color(0xFF625b71),
    tertiary = Color(0xFF7D5260)
)

val DefaultCustomColors = CustomColors(
    banner = Color(62, 103, 121),
    background = Color(90, 58, 49),
    border = Color(56, 59, 83),
    box = Color(49, 134, 29),
    card = Color(196, 203, 202),
    innerBox = Color(242, 245, 234),
    onBackground = Color.White,
    onBox = Color.White,
    onCard = Color.Black,
    onInnerBox = Color.Black
)

val ForestTheme = CustomColors(
    banner = Color(0xFF2E7D32),
    background = Color(0xFFF1F8E9),
    border = Color(0xFF1B5E20),
    box = Color(0xFF4CAF50),
    card = Color(0xFFC8E6C9),
    innerBox = Color(0xFFE8F5E9),
    onBackground = Color(0xFF1B5E20),
    onBox = Color.White,
    onCard = Color(0xFF1B5E20),
    onInnerBox = Color(0xFF1B5E20)
)

val BlueCustomColors = CustomColors(
    banner = Color(13, 71, 161),
    background = Color(225, 245, 254),
    border = Color(25, 118, 210),
    box = Color(30, 136, 229),
    card = Color(227, 242, 253),
    innerBox = Color.White,
    onBackground = Color(1, 87, 155),
    onBox = Color.White,
    onCard = Color(1, 87, 155),
    onInnerBox = Color.Black
)

val OceanTheme = CustomColors(
    banner = Color(0xFF01579B),
    background = Color(0xFFE1F5FE),
    border = Color(0xFF0277BD),
    box = Color(0xFF03A9F4),
    card = Color(0xFFB3E5FC),
    innerBox = Color.White,
    onBackground = Color(0xFF01579B),
    onBox = Color.White,
    onCard = Color(0xFF01579B),
    onInnerBox = Color(0xFF01579B)
)

val RedCustomColors = CustomColors(
    banner = Color(183, 28, 28),
    background = Color(255, 235, 238),
    border = Color(211, 47, 47),
    box = Color(244, 67, 54),
    card = Color(255, 205, 210),
    innerBox = Color.White,
    onBackground = Color(183, 28, 28),
    onBox = Color.White,
    onCard = Color(183, 28, 28),
    onInnerBox = Color.Black
)

val SunsetTheme = CustomColors(
    banner = Color(0xFFBF360C),
    background = Color(0xFFFFF3E0),
    border = Color(0xFFE64A19),
    box = Color(0xFFFF5722),
    card = Color(0xFFFFCCBC),
    innerBox = Color.White,
    onBackground = Color(0xFFBF360C),
    onBox = Color.White,
    onCard = Color(0xFFBF360C),
    onInnerBox = Color(0xFFBF360C)
)

val GreenCustomColors = CustomColors(
    banner = Color(27, 94, 32),
    background = Color(232, 245, 233),
    border = Color(56, 142, 60),
    box = Color(76, 175, 80),
    card = Color(200, 230, 201),
    innerBox = Color.White,
    onBackground = Color(27, 94, 32),
    onBox = Color.White,
    onCard = Color(27, 94, 32),
    onInnerBox = Color.Black
)

val MidnightTheme = CustomColors(
    banner = Color(0xFF121212),
    background = Color(0xFF1A1A1A),
    border = Color(0xFF333333),
    box = Color(0xFF242424),
    card = Color(0xFF2C2C2C),
    innerBox = Color(0xFF383838),
    onBackground = Color(0xFFE0E0E0),
    onBox = Color.White,
    onCard = Color(0xFFB0B0B0),
    onInnerBox = Color.White
)

val DarkOledCustomColors = CustomColors(
    banner = Color(33, 33, 33),
    background = Color.Black,
    border = Color(66, 66, 66),
    box = Color(33, 33, 33),
    card = Color(66, 66, 66),
    innerBox = Color(33, 33, 33),
    onBackground = Color.White,
    onBox = Color.White,
    onCard = Color.White,
    onInnerBox = Color.White
)

val LavenderTheme = CustomColors(
    banner = Color(0xFF4A148C),
    background = Color(0xFFF3E5F5),
    border = Color(0xFF7B1FA2),
    box = Color(0xFF9C27B0),
    card = Color(0xFFE1BEE7),
    innerBox = Color.White,
    onBackground = Color(0xFF4A148C),
    onBox = Color.White,
    onCard = Color(0xFF4A148C),
    onInnerBox = Color(0xFF4A148C)
)

@Composable
fun AbsenceViewerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: Int = 0, // 0: Tannenzapfen, 1: Wald, 2: Blau, 3: Rot, 4: Grün, 5: Ocean, 6: Sunset, 7: Lavender, 8: Midnight
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeMode == 8 -> DarkColorScheme // Use dark scheme for OLED
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = when (themeMode) {
        1 -> ForestTheme
        2 -> BlueCustomColors
        3 -> RedCustomColors
        4 -> GreenCustomColors
        5 -> OceanTheme
        6 -> SunsetTheme
        7 -> LavenderTheme
        8 -> MidnightTheme
        else -> DefaultCustomColors
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
