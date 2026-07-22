package com.willy.mynote.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.willy.mynote.R
import com.willy.mynote.model.NoteColor

/**
 * Menerjemahkan pilihan warna (model murni) menjadi Color asli dari colors.xml.
 * Pemetaan ini sengaja dipisah dari model agar Note.kt tetap bebas dependensi Android.
 */
@Composable
fun NoteColor.toCardColor(): Color = colorResource(
    id = when (this) {
        NoteColor.YELLOW -> R.color.note_card_yellow
        NoteColor.PINK -> R.color.note_card_pink
        NoteColor.BLUE -> R.color.note_card_blue
        NoteColor.GREEN -> R.color.note_card_green
        NoteColor.PURPLE -> R.color.note_card_purple
        NoteColor.ORANGE -> R.color.note_card_orange
    }
)
