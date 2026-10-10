package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
  transactions: List<Transaction>,
  activeFilter: String,
  searchQuery: String,
  onFilterChange: (String) -> Unit,
  onSearchChange: (String) -> Unit,
  onAddTransactionClick: () -> Unit,
  onVoidClick: (Transaction) -> Unit
) {
  val filteredList = transactions.filter { tx ->
    val matchType = when (activeFilter) {
      "INCOME" -> tx.transactionType == TransactionType.INCOME
      "EXPENSE" -> tx.transactionType == TransactionType.EXPENSE
      "TRANSFER" -> tx.transactionType == TransactionType.TRANSFER
      else -> true
    }
    val matchSearch = searchQuery.isBlank() ||
        tx.description.contains(searchQuery, ignoreCase = true) ||
        tx.transactionDate.contains(searchQuery, ignoreCase = true) ||
        (tx.categoryId != null && tx.categoryId.contains(searchQuery, ignoreCase = true)) ||
        (tx.referenceId != null && tx.referenceId.contains(searchQuery, ignoreCase = true))
    matchType && matchSearch
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAddTransactionClick,
        containerColor = Emerald40,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_add_transaction")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Cari transaksi, deskripsi, tanggal...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(Icons.Default.Close, contentDescription = "Hapus")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      // Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          "ALL" to "Semua",
          "INCOME" to "Pemasukan",
          "EXPENSE" to "Pengeluaran",
          "TRANSFER" to "Transfer"
        ).forEach { (key, label) ->
          FilterChip(
            selected = activeFilter == key,
            onClick = { onFilterChange(key) },
            label = { Text(label, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = when (key) {
                "INCOME" -> SuccessGreen.copy(alpha = 0.2f)
                "EXPENSE" -> DangerRed.copy(alpha = 0.2f)
                "TRANSFER" -> InfoCyan.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.primaryContainer
              },
              selectedLabelColor = when (key) {
                "INCOME" -> SuccessGreen
                "EXPENSE" -> DangerRed
                "TRANSFER" -> InfoCyan
                else -> MaterialTheme.colorScheme.onPrimaryContainer
              }
            )
          )
        }
      }

      // Transaction List
      if (filteredList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = Slate700)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tidak ada transaksi ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          items(filteredList, key = { it.transactionId }) { tx ->
            TransactionCard(
              transaction = tx,
              onVoidClick = { onVoidClick(tx) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TransactionCard(
  transaction: Transaction,
  onVoidClick: () -> Unit
) {
  val isVoid = transaction.status == TransactionStatus.VOID

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isVoid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(
                if (isVoid) Slate700.copy(alpha = 0.3f) else when (transaction.transactionType) {
                  TransactionType.INCOME -> SuccessGreen.copy(alpha = 0.2f)
                  TransactionType.EXPENSE -> DangerRed.copy(alpha = 0.2f)
                  TransactionType.TRANSFER -> InfoCyan.copy(alpha = 0.2f)
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (transaction.transactionType) {
                TransactionType.INCOME -> Icons.Default.ArrowDownward
                TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
              },
              contentDescription = null,
              tint = if (isVoid) Slate700 else when (transaction.transactionType) {
                TransactionType.INCOME -> SuccessGreen
                TransactionType.EXPENSE -> DangerRed
                TransactionType.TRANSFER -> InfoCyan
              },
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = transaction.description,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (isVoid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${transaction.transactionDate} • ${transaction.categoryId ?: transaction.transactionType.wireName.uppercase()}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          val prefix = when (transaction.transactionType) {
            TransactionType.INCOME -> "+"
            TransactionType.EXPENSE -> "-"
            TransactionType.TRANSFER -> "⇄ "
          }
          Text(
            text = "$prefix${FinTrackViewModel.formatRupiah(transaction.amount)}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (isVoid) Slate700 else when (transaction.transactionType) {
              TransactionType.INCOME -> SuccessGreen
              TransactionType.EXPENSE -> DangerRed
              TransactionType.TRANSFER -> InfoCyan
            }
          )
          if (isVoid) {
            Text("VOID (DIBATALKAN)", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
          } else if (transaction.categoryId == "INC_TRADING") {
            Text("Profit Trading", fontSize = 10.sp, color = InfoCyan)
          }
        }
      }

      if (!isVoid) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!transaction.referenceId.isNullOrBlank()) {
            Text(
              "Ref: ${transaction.referenceId}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.primary
            )
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          TextButton(
            onClick = onVoidClick,
            colors = ButtonDefaults.textButtonColors(contentColor = DangerRed),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Batalkan (Void)", fontSize = 11.sp)
          }
        }
      }
    }
  }
}
