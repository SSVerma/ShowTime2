package com.ssverma.core.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.R

private val Nunito = FontFamily(
    Font(R.font.nunito_sans_regular),
    Font(R.font.nunito_sans_semibold, FontWeight.W500),
    Font(R.font.nunito_sans_bold, FontWeight.Bold)
)

val Typography = typographyFromDefaults(
    defaultFontFamily = Nunito,
    h4 = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.Bold
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.W500,
        lineHeight = 28.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.W500,
        lineHeight = 22.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.W500
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold
    ),
    caption = TextStyle(
        fontFamily = Nunito
    )
)

private fun typographyFromDefaults(
    defaultFontFamily: FontFamily,
    h1: TextStyle? = null,
    h2: TextStyle? = null,
    h3: TextStyle? = null,
    h4: TextStyle? = null,
    h5: TextStyle? = null,
    h6: TextStyle? = null,
    subtitle1: TextStyle? = null,
    subtitle2: TextStyle? = null,
    body1: TextStyle? = null,
    body2: TextStyle? = null,
    button: TextStyle? = null,
    caption: TextStyle? = null,
    overline: TextStyle? = null
): Typography {
    val defaults = Typography(defaultFontFamily = defaultFontFamily)
    return Typography(
        h1 = defaults.h1.merge(h1),
        h2 = defaults.h2.merge(h2),
        h3 = defaults.h3.merge(h3),
        h4 = defaults.h4.merge(h4),
        h5 = defaults.h5.merge(h5),
        h6 = defaults.h6.merge(h6),
        subtitle1 = defaults.subtitle1.merge(subtitle1),
        subtitle2 = defaults.subtitle2.merge(subtitle2),
        body1 = defaults.body1.merge(body1),
        body2 = defaults.body2.merge(body2),
        button = defaults.button.merge(button),
        caption = defaults.caption.merge(caption),
        overline = defaults.overline.merge(overline)
    )
}