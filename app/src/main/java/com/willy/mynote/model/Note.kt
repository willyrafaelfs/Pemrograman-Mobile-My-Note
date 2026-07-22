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
    val updatedAt: Long = System.currentTimeMillis() // Kapan terakhir diubah
)
