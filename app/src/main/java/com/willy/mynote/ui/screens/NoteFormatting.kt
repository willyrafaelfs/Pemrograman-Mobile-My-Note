package com.willy.mynote.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Util berbagi untuk memformat timestamp menjadi teks ramah pengguna. */
internal fun formatTanggal(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return formatter.format(Date(millis))
}
