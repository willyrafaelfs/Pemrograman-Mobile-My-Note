package com.willy.mynote.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.willy.mynote.navigation.Screen
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

    // State teks bersifat LOKAL di layar ini (belum perlu masuk ViewModel)
    // karena hanya layar ini yang peduli pada draf yang sedang diketik.
    //
    // rememberSaveable (bukan remember biasa!) → draf SELAMAT dari rotasi
    // layar.
    var textContent by rememberSaveable {
        mutableStateOf(
            if (isEditMode) viewModel.getNoteById(noteId)?.content.orEmpty()
            else ""
        )
    }

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
                    // Tombol SIMPAN (✓) di pojok kanan atas — pola umum
                    // aplikasi notes (Google Keep memakai pola serupa).
                    IconButton(
                        onClick = {
                            // Event naik ke ViewModel (Unidirectional Data Flow):
                            // UI hanya MELAPOR, ViewModel yang MEMUTUSKAN.
                            viewModel.saveNote(
                                id = if (isEditMode) noteId else null,
                                content = textContent
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
        // TextField memenuhi seluruh layar → pengalaman "Notepad":
        // pengguna langsung fokus menulis tanpa distraksi.
        TextField(
            value = textContent,
            onValueChange = { newText -> textContent = newText },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            placeholder = { Text("Tulis catatanmu di sini...") },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                // Transparan agar menyatu dengan latar — kesan kertas kosong
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = Color.Transparent,   // hilangkan garis bawah
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}
