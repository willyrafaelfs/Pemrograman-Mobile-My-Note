package com.willy.mynote.model

/**
 * Note adalah "cetakan" (blueprint) satu catatan.
 *
 * MENGAPA data class?
 * - equals()/hashCode() otomatis → Compose bisa mendeteksi perubahan data
 *   secara efisien saat recomposition.
 * - copy() otomatis → mendukung prinsip IMMUTABILITY: kita tidak mengubah
 *   objek lama, melainkan membuat salinan baru dengan nilai berbeda.
 *   Analogi: seperti kertas sticky note — kita tidak menghapus tulisan lama
 *   dengan tip-ex, melainkan menempel kertas baru menggantikannya.
 */
data class Note(
    val id: Long,                 // Identitas unik — kunci untuk fitur edit
    val content: String,          // Isi catatan yang ditulis pengguna
    val color: NoteColor = NoteColor.YELLOW, // Warna kartu, seperti sticky note fisik
    val isPinned: Boolean = false,           // Pin-up: tampil di urutan teratas
    val createdAt: Long = System.currentTimeMillis(), // Kapan catatan pertama kali dibuat
    val updatedAt: Long = System.currentTimeMillis()  // Kapan terakhir diubah
)
