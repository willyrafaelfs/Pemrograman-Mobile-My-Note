package com.willy.mynote.viewmodel

import androidx.lifecycle.ViewModel
import com.willy.mynote.model.Note
import com.willy.mynote.model.NoteColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * NoteViewModel = "otak" aplikasi yang menyimpan dan mengelola semua catatan.
 *
 * MENGAPA ViewModel?
 * ViewModel bertahan hidup saat configuration change (mis. rotasi layar).
 * Jika daftar catatan disimpan langsung di Composable dengan remember{},
 * data akan HILANG saat layar dirotasi.
 *
 * MENGAPA StateFlow, bukan variabel biasa?
 * StateFlow adalah "aliran data yang bisa diamati" (observable).
 * Analogi: langganan koran — Dashboard "berlangganan" ke _notes;
 * setiap kali ada edisi baru (data berubah), koran otomatis diantar
 * (UI otomatis recompose). Tanpa Flow, UI tidak tahu data sudah berubah.
 */
class NoteViewModel : ViewModel() {

    // _notes bersifat PRIVATE dan MUTABLE — hanya ViewModel yang boleh mengubah.
    private val _notes = MutableStateFlow<List<Note>>(emptyList())

    // notes bersifat PUBLIC dan READ-ONLY — UI hanya boleh MEMBACA.
    // Pola ini disebut "backing property", pilar dari Unidirectional Data Flow:
    // data mengalir turun (ViewModel → UI), event mengalir naik (UI → ViewModel).
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Penghitung ID sederhana. Nanti saat migrasi ke Room,
    // tugas ini diambil alih oleh autoGenerate = true.
    private var nextId = 1L

    /** Mencari catatan berdasarkan id — dipakai EditorScreen saat mode edit. */
    fun getNoteById(id: Long): Note? =
        _notes.value.find { it.id == id }

    /**
     * Satu fungsi untuk DUA skenario (prinsip DRY):
     * - id == null  → CREATE : buat catatan baru
     * - id != null  → UPDATE : perbarui catatan yang sudah ada
     */
    fun saveNote(id: Long?, content: String, color: NoteColor) {
        // Validasi: catatan kosong tidak layak disimpan
        if (content.isBlank()) return

        _notes.update { currentList ->
            if (id == null) {
                // CREATE: catatan baru ditaruh PALING ATAS (sebelum diurutkan)
                // agar langsung terlihat pengguna begitu kembali ke Dashboard.
                (listOf(Note(id = nextId++, content = content.trim(), color = color)) + currentList)
                    .sortedForDisplay()
            } else {
                // UPDATE: gunakan copy() — buat salinan baru, jangan mutasi.
                // map() menghasilkan LIST BARU sehingga StateFlow mendeteksi
                // perubahan dan memicu recomposition.
                currentList.map { note ->
                    if (note.id == id) {
                        note.copy(
                            content = content.trim(),
                            color = color,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        note
                    }
                }.sortedForDisplay()
            }
        }
    }

    /**
     * Pin-up: catatan yang di-pin selalu tampil di urutan teratas daftar.
     * Toggle — dipanggil lagi untuk melepas pin.
     */
    fun togglePin(id: Long) {
        _notes.update { currentList ->
            currentList.map { note ->
                if (note.id == id) note.copy(isPinned = !note.isPinned) else note
            }.sortedForDisplay()
        }
    }

    /** Bonus: hapus catatan (long-press di Dashboard bisa jadi latihan mandiri). */
    fun deleteNote(id: Long) {
        _notes.update { currentList -> currentList.filterNot { it.id == id } }
    }

    /**
     * Urutan tampil: catatan yang di-pin selalu di atas, lalu di dalam
     * masing-masing kelompok (pinned/tidak) diurutkan dari yang paling
     * baru diperbarui.
     */
    private fun List<Note>.sortedForDisplay(): List<Note> =
        sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
}
