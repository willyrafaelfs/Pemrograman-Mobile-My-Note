package com.willy.mynote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.willy.mynote.model.Note
import com.willy.mynote.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NoteViewModel,
    onAddNote: () -> Unit,
    onNoteClick: (Long) -> Unit
) {
    // "Berlangganan" ke StateFlow. Setiap kali daftar catatan berubah
    // di ViewModel, variabel `notes` diperbarui → Compose otomatis
    // menggambar ulang bagian UI yang terpengaruh (recomposition).
    // collectAsStateWithLifecycle() lebih hemat daripada collectAsState():
    // ia berhenti mengoleksi saat aplikasi di background.
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyNote 📝", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // FAB dengan LABEL TEKS sesuai spesifikasi: "+ Catatan".
            // ExtendedFloatingActionButton = FAB versi lebar (ikon + teks),
            // lebih ramah pengguna baru dibanding FAB ikon saja.
            ExtendedFloatingActionButton(
                onClick = onAddNote,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Catatan") }
            )
        }
    ) { innerPadding ->
        if (notes.isEmpty()) {
            // EMPTY STATE: jangan biarkan layar kosong melompong —
            // beri tahu pengguna apa yang harus dilakukan.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada catatan.\nTekan \"+ Catatan\" untuk memulai ✍️",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            // LazyColumn = RecyclerView versi Compose.
            // Analogi: sushi conveyor belt — hanya piring (item) yang
            // terlihat di depan mata yang benar-benar "disajikan" (dirender).
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notes,
                    key = { note -> note.id } // key = "nomor punggung" tiap item;
                                              // membantu Compose melacak item saat
                                              // list berubah → animasi & performa lebih baik
                ) { note ->
                    NoteCard(note = note, onClick = { onNoteClick(note.id) })
                }
            }
        }
    }
}

/**
 * Komponen kartu catatan — dipisah agar REUSABLE dan mudah di-preview.
 * Analogi: satu keping LEGO yang bisa dipasang di mana saja.
 */
@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick, // Card Material 3 sudah mendukung onClick + efek ripple
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = note.content,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 3,                       // Cukup cuplikan 3 baris di dashboard
            overflow = TextOverflow.Ellipsis    // Sisa teks diganti "..."
        )
        Text(
            text = "Diperbarui: ${formatTanggal(note.updatedAt)}",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

/** Util kecil untuk memformat timestamp menjadi teks ramah pengguna. */
private fun formatTanggal(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return formatter.format(Date(millis))
}
