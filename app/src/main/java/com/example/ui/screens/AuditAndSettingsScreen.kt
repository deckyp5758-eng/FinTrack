package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun AuditAndSettingsScreen(
  currentMode: AppEnvironmentMode,
  isProductionVerified: Boolean,
  productionUrl: String,
  isConnecting: Boolean,
  connectionMessage: String?,
  auditLogs: List<AuditLogEntry>,
  onSwitchMode: (AppEnvironmentMode) -> Unit,
  onTestConnection: (String) -> Unit
) {
  var urlInput by remember { mutableStateOf(productionUrl) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
  ) {
    // 1. Status Database Terverifikasi Sesuai Audit
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "SPESIFIKASI GOOGLE SHEETS DATABASE (AUDIT RESMI)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("FinTrack AI — Personal Finance Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("Spreadsheet ID: 1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(
              onClick = {},
              label = {
                Text(
                  if (currentMode == AppEnvironmentMode.PRODUCTION && isProductionVerified) "PRODUKSI AKTIF" else "MODE MOCK (DEV)",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = AssistChipDefaults.assistChipColors(
                containerColor = if (currentMode == AppEnvironmentMode.PRODUCTION && isProductionVerified) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                labelColor = if (currentMode == AppEnvironmentMode.PRODUCTION && isProductionVerified) SuccessGreen else WarningAmber
              )
            )
          }

          Divider(color = Slate800)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Zona: Asia/Jakarta", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Locale: in_ID (id-ID)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Mata Uang: IDR", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Text(
            "9 Sheet Terverifikasi: Pengaturan (100), Rekening (101), Kategori (102), Transaksi (103), Anggaran (104), Target_Tabungan (105), Ringkasan_Bulanan (106), Log_Aktivitas (107), Dokumentasi_DB (108).",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 2. Mode Selector & Web App URL Config
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Pilihan Mode Lingkungan", fontWeight = FontWeight.Bold, fontSize = 14.sp)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = currentMode == AppEnvironmentMode.DEVELOPMENT,
              onClick = { onSwitchMode(AppEnvironmentMode.DEVELOPMENT) },
              label = { Text("Mode Mock (Dev)") },
              leadingIcon = { Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp)) },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = currentMode == AppEnvironmentMode.PRODUCTION,
              onClick = { onSwitchMode(AppEnvironmentMode.PRODUCTION) },
              label = { Text("Mode Produksi (Sheets)") },
              leadingIcon = { Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp)) },
              modifier = Modifier.weight(1f)
            )
          }

          Text(
            "Untuk Mode Produksi, masukkan Web App URL Google Apps Script hasil deployment dengan akses 'Anyone':",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Web App Deployment URL") },
            placeholder = { Text("https://script.google.com/macros/s/.../exec") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Button(
            onClick = { onTestConnection(urlInput) },
            enabled = !isConnecting && urlInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
            modifier = Modifier.fillMaxWidth()
          ) {
            if (isConnecting) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Menguji Koneksi...")
            } else {
              Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Uji Koneksi Google Sheets")
            }
          }

          if (connectionMessage != null) {
            val isSuccess = connectionMessage.startsWith("SUKSES")
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSuccess) SuccessGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f))
                .padding(10.dp)
            ) {
              Text(
                connectionMessage,
                fontSize = 12.sp,
                color = if (isSuccess) SuccessGreen else DangerRed,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Text(
            "Aturan Keamanan: Token OAuth dan kunci rahasia tidak pernah disimpan di source code. Skema kolom persis A1:M1 baris 1.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 3. Log Aktivitas Finansial (Sheet: Log_Aktivitas A1:J1)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Jejak Audit Aktivitas (Log_Aktivitas)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("${auditLogs.size} Entri", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    if (auditLogs.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada entri log aktivitas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(auditLogs) { log ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                log.action,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = when (log.result) {
                  "void" -> DangerRed
                  "failure" -> DangerRed
                  else -> Emerald80
                }
              )
              Text(log.eventTimestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(log.changeSummary, fontSize = 12.sp)
            Text(
              "Aktor: ${log.actor} • Entitas: ${log.entityType} [${log.entityId}] • Hasil: ${log.result}",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
