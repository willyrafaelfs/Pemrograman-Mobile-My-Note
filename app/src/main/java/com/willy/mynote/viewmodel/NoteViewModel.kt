package com.willy.mynote.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.willy.mynote.model.Note
import com.willy.mynote.model.NoteColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

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
 *
 * MENGAPA AndroidViewModel, bukan ViewModel biasa?
 * AndroidViewModel membawa Application context, dibutuhkan untuk membuka
 * SharedPreferences — tempat catatan disimpan agar tidak hilang saat
 * aplikasi ditutup (persisten antar sesi, bukan cuma antar rotasi layar).
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // _notes bersifat PRIVATE dan MUTABLE — hanya ViewModel yang boleh mengubah.
    private val _notes = MutableStateFlow<List<Note>>(emptyList())

    // notes bersifat PUBLIC dan READ-ONLY — UI hanya boleh MEMBACA.
    // Pola ini disebut "backing property", pilar dari Unidirectional Data Flow:
    // data mengalir turun (ViewModel → UI), event mengalir naik (UI → ViewModel).
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Penghitung ID sederhana. Nanti saat migrasi ke Room,
    // tugas ini diambil alih oleh autoGenerate = true.
    private var nextId = 1L

    init {
        // Muat catatan yang sudah tersimpan begitu ViewModel dibuat, supaya
        // pengguna melihat catatan lamanya lagi setelah aplikasi ditutup-buka.
        val loaded = loadNotesFromPrefs().sortedForDisplay()
        nextId = (loaded.maxOfOrNull { it.id } ?: 0L) + 1
        _notes.value = loaded
    }

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
        persistNotes()
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
        persistNotes()
    }

    /** Bonus: hapus catatan (long-press di Dashboard bisa jadi latihan mandiri). */
    fun deleteNote(id: Long) {
        _notes.update { currentList -> currentList.filterNot { it.id == id } }
        persistNotes()
    }

    /**
     * Urutan tampil: catatan yang di-pin selalu di atas, lalu di dalam
     * masing-masing kelompok (pinned/tidak) diurutkan dari yang paling
     * baru diperbarui.
     */
    private fun List<Note>.sortedForDisplay(): List<Note> =
        sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })

    /** Menulis seluruh daftar catatan saat ini sebagai JSON ke SharedPreferences. */
    private fun persistNotes() {
        val array = JSONArray()
        _notes.value.forEach { note -> array.put(note.toJson()) }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    /** Membaca daftar catatan dari SharedPreferences (kosong jika belum ada apa-apa). */
    private fun loadNotesFromPrefs(): List<Note> {
        val json = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { index -> array.getJSONObject(index).toNote() }
        } catch (e: org.json.JSONException) {
            emptyList()
        }
    }

    private fun Note.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("content", content)
        put("color", color.name)
        put("isPinned", isPinned)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toNote(): Note = Note(
        id = getLong("id"),
        content = getString("content"),
        color = NoteColor.valueOf(getString("color")),
        isPinned = getBoolean("isPinned"),
        createdAt = getLong("createdAt"),
        updatedAt = getLong("updatedAt")
    )

    private companion object {
        const val PREFS_NAME = "mynote_prefs"
        const val KEY_NOTES = "notes_json"
    }
}
