package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.FinTrackViewModel
import com.example.ui.theme.*

@Composable
fun AccountsScreen(
  accounts: List<Account>,
  onAddAccountClick: () -> Unit,
  onReconcileClick: (Account) -> Unit
) {
  val total = accounts.filter { it.isActive }.sumOf { it.calculatedBalance }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAddAccountClick,
        containerColor = Emerald40,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_add_account")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Tambah Rekening")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
      // Header Info Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              "TOTAL SALDO SEMUA REKENING",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              FinTrackViewModel.formatRupiah(total),
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              "Google Sheets (Sheet: Rekening A1:J1) adalah Single Source of Truth untuk saldo. Saldo dihitung deterministik dari opening_balance + transaksi posted.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Accounts List
      items(accounts) { acc ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      when (acc.accountType) {
                        AccountType.BANK -> Emerald40.copy(alpha = 0.2f)
                        AccountType.CASH -> PurpleAccent.copy(alpha = 0.2f)
                        AccountType.TRADING -> Gold40.copy(alpha = 0.2f)
                        AccountType.OTHER -> InfoCyan.copy(alpha = 0.2f)
                      }
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = when (acc.accountType) {
                      AccountType.BANK -> Icons.Default.AccountBalance
                      AccountType.CASH -> Icons.Default.Payments
                      AccountType.TRADING -> Icons.Default.TrendingUp
                      AccountType.OTHER -> Icons.Default.QrCode
                    },
                    contentDescription = null,
                    tint = when (acc.accountType) {
                      AccountType.BANK -> Emerald80
                      AccountType.CASH -> PurpleAccent
                      AccountType.TRADING -> Gold80
                      AccountType.OTHER -> InfoCyan
                    },
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(acc.accountName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  Text(
                    "${acc.accountType.displayName} • ID: ${acc.accountId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  FinTrackViewModel.formatRupiah(acc.calculatedBalance),
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 15.sp
                )
                Text("Terverifikasi", fontSize = 10.sp, color = SuccessGreen)
              }
            }

            if (acc.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                acc.notes,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = { onReconcileClick(acc) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Rekonsiliasi Saldo Fisik", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}
