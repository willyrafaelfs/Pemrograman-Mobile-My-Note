package com.willy.mynote.navigation

/**
 * Sealed class = daftar rute yang TERKUNCI (tidak bisa ditambah dari luar file ini).
 * Ini SINGLE SOURCE OF TRUTH untuk semua alamat layar.
 *
 * MENGAPA tidak pakai String mentah ("dashboard", "editor")?
 * String mentah rawan typo — salah ketik "dasboard" baru ketahuan saat
 * aplikasi CRASH di runtime. Dengan sealed class, kesalahan terdeteksi
 * SAAT COMPILE. Compiler adalah teman terbaik kita.
 */
sealed class Screen(val route: String) {

    data object Dashboard : Screen("dashboard")

    /**
     * Rute editor memakai OPTIONAL ARGUMENT (tanda "?"):
     * - "editor?noteId=-1" → mode CREATE (buat catatan baru)
     * - "editor?noteId=5"  → mode EDIT   (buka catatan id 5)
     *
     * Kita pilih optional argument (bukan path argument "editor/{id}")
     * karena satu layar melayani dua mode — argumen boleh "tidak ada".
     */
    data object Editor : Screen("editor?noteId={noteId}") {
        const val ARG_NOTE_ID = "noteId"
        const val NO_ID = -1L  // Nilai sentinel: menandakan "catatan baru"

        /** Helper membangun rute konkret — mencegah typo di call-site. */
        fun buildRoute(noteId: Long = NO_ID): String = "editor?noteId=$noteId"
    }
}
