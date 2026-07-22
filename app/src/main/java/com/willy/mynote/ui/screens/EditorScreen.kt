package com.willy.mynote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.willy.mynote.model.NoteColor
import com.willy.mynote.navigation.Screen
import com.willy.mynote.ui.theme.toCardColor
import com.willy.mynote.viewmodel.NoteViewModel

/**
 * EditorScreen melayani DUA mode dengan satu kode (DRY):
 * - noteId == NO_ID → CREATE : mulai dari teks kosong
 * - noteId != NO_ID → EDIT   : muat isi catatan lama sebagai teks awal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: NoteViewModel,
    noteId: Long,
    onNavigateBack: () -> Unit
) {
    val isEditMode = noteId != Screen.Editor.NO_ID
    val existingNote = if (isEditMode) viewModel.getNoteById(noteId) else null

    // State teks bersifat LOKAL di layar ini (belum perlu masuk ViewModel)
    // karena hanya layar ini yang peduli pada draf yang sedang diketik.
    //
    // rememberSaveable (bukan remember biasa!) → draf SELAMAT dari rotasi
    // layar.
    var textContent by rememberSaveable {
        mutableStateOf(existingNote?.content.orEmpty())
    }

    // Warna disimpan sebagai nama enum (String) karena rememberSaveable
    // hanya butuh tipe yang bisa masuk Bundle — String paling aman & sederhana.
    var selectedColorName by rememberSaveable {
        mutableStateOf((existingNote?.color ?: NoteColor.YELLOW).name)
    }
    val selectedColor = NoteColor.valueOf(selectedColorName)

    // Status pin di-refresh dari StateFlow tiap kali berubah, supaya tombol
    // pin di sini tetap sinkron walau di-toggle dari Dashboard di layar lain.
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val isPinned = notes.find { it.id == noteId }?.isPinned ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Catatan" else "Catatan Baru")
                },
                navigationIcon = {
                    // Tombol kembali TANPA menyimpan (batal)
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali tanpa menyimpan"
                        )
                    }
                },
                actions = {
                    // Pin hanya relevan untuk catatan yang sudah ada — catatan
                    // baru belum punya id sampai disimpan.
                    if (isEditMode) {
                        IconButton(onClick = { viewModel.togglePin(noteId) }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (isPinned) "Lepas pin" else "Pin catatan ini"
                            )
                        }
                    }
                    // Tombol SIMPAN (✓) di pojok kanan atas — pola umum
                    // aplikasi notes (Google Keep memakai pola serupa).
                    IconButton(
                        onClick = {
                            // Event naik ke ViewModel (Unidirectional Data Flow):
                            // UI hanya MELAPOR, ViewModel yang MEMUTUSKAN.
                            viewModel.saveNote(
                                id = if (isEditMode) noteId else null,
                                content = textContent,
                                color = selectedColor
                            )
                            // Setelah simpan → kembali ke Dashboard.
                            // Dashboard otomatis menampilkan data terbaru
                            // karena ia berlangganan StateFlow yang sama.
                            onNavigateBack()
                        },
                        enabled = textContent.isNotBlank() // cegah simpan kosong
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Simpan catatan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pemilih warna kartu — efeknya langsung terlihat di kanvas
            // TextField di bawah, seperti memilih kertas sticky note baru.
            ColorPickerRow(
                selectedColor = selectedColor,
                onColorSelected = { selectedColorName = it.name }
            )

            // Info riwayat waktu — hanya ada saat mode edit, catatan baru
            // belum punya riwayat untuk ditampilkan.
            if (existingNote != null) {
                val label = if (existingNote.updatedAt == existingNote.createdAt) {
                    "Dibuat: ${formatTanggal(existingNote.createdAt)}"
                } else {
                    "Dibuat: ${formatTanggal(existingNote.createdAt)} • Diperbarui: ${formatTanggal(existingNote.updatedAt)}"
                }
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // TextField memenuhi sisa layar → pengalaman "Notepad":
            // pengguna langsung fokus menulis tanpa distraksi.
            TextField(
                value = textContent,
                onValueChange = { newText -> textContent = newText },
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text("Tulis catatanmu di sini...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    // Kanvas mengikuti warna sticky note yang dipilih —
                    // kesan kertas berwarna sungguhan, bukan latar polos.
                    focusedContainerColor = selectedColor.toCardColor(),
                    unfocusedContainerColor = selectedColor.toCardColor(),
                    focusedIndicatorColor = Color.Transparent,   // hilangkan garis bawah
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

/** Baris bulatan warna untuk memilih warna kartu catatan. */
@Composable
private fun ColorPickerRow(
    selectedColor: NoteColor,
    onColorSelected: (NoteColor) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NoteColor.entries.forEach { noteColor ->
            val isSelected = noteColor == selectedColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(noteColor.toCardColor())
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        },
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(noteColor) }
            )
        }
    }
}
