package com.willy.mynote.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Dialog konfirmasi hapus — dipakai bersama oleh Dashboard (long-press kartu)
 * dan Editor (tombol hapus), supaya penghapusan tidak pernah terjadi tanpa
 * sengaja lewat satu ketukan/tekan saja.
 */
@Composable
fun DeleteNoteDialog(
    noteContent: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val preview = noteContent.trim().let {
        if (it.length > 40) "${it.take(40)}…" else it
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus catatan?") },
        text = {
            Text("Catatan \"$preview\" akan dihapus dan tidak bisa dikembalikan.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Hapus", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
