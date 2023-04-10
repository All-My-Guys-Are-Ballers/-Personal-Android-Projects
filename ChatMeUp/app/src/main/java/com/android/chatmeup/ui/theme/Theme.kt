package com.android.chatmeup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = cmuWhite,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = cmuBlack,
    onBackground = cmuDarkGrey
)

private val LightColorPalette = lightColors(
    primary = cmuBlack,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = cmuWhite,
    onBackground = cmuLightGrey

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun ChatMeUpTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = if(!darkTheme) LightTypography else DarkTypography,
        shapes = Shapes,
        content = content
    )
}