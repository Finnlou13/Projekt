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

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val BlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    onPrimary = Color.White
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

@Composable
fun AbsenceViewerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: Int = 0, // 0: Default, 1: Blue
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeMode == 1 -> BlueColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = when (themeMode) {
        1 -> BlueCustomColors
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
