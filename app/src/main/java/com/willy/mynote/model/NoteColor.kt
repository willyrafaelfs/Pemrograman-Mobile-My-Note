package com.willy.mynote.model

/**
 * Pilihan warna kartu untuk satu catatan — ala kertas sticky note fisik
 * yang memang tersedia dalam beberapa warna.
 *
 * Enum ini sengaja TIDAK menyimpan nilai warna (hex/ColorRes) di sini.
 * Model tetap murni Kotlin tanpa dependensi Android; pemetaan ke warna
 * asli ada di layer UI (lihat ui/theme/NoteColors.kt) yang membaca dari
 * colors.xml.
 */
enum class NoteColor {
    YELLOW,
    PINK,
    BLUE,
    GREEN,
    PURPLE,
    ORANGE
}
