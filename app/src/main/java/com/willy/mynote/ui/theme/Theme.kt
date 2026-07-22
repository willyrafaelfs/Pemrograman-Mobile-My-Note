package com.willy.mynote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.willy.mynote.R

/**
 * MyNoteTheme membungkus seluruh UI aplikasi.
 *
 * MENGAPA warna dibaca via colorResource()?
 * Karena sumber kebenaran (single source of truth) warna ada di colors.xml.
 * Jika desainer ingin mengganti palet, cukup ubah XML — tidak perlu
 * menyentuh kode Kotlin sama sekali.
 */
@Composable
fun MyNoteTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = colorResource(id = R.color.note_primary),
        onPrimary = colorResource(id = R.color.note_on_primary),
        surface = colorResource(id = R.color.note_surface),
        onSurface = colorResource(id = R.color.note_on_surface),
        background = colorResource(id = R.color.note_background),
        secondaryContainer = colorResource(id = R.color.note_secondary_container),
        onSecondaryContainer = colorResource(id = R.color.note_on_secondary_container)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
