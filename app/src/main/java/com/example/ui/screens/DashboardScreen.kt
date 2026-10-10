package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@Composable
fun DashboardScreen(
  summary: DashboardSummary,
  onAddTransaction: (TransactionType) -> Unit,
  onNavigateToAccounts: () -> Unit,
  onNavigateToBudgets: () -> Unit,
  onNavigateToTransactions: () -> Unit,
  onReconcileAccount: (Account) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
  ) {
    // 1. Total Net Worth Hero Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TOTAL KEKAYAAN BERSIH",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AssistChip(
              onClick = {},
              label = {
                Text(
                  if (summary.database.status == "CONNECTED") "Online (Sheets)" else "Mock Data (Dev)",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              leadingIcon = {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (summary.database.status == "CONNECTED") SuccessGreen else WarningAmber)
                )
              },
              colors = AssistChipDefaults.assistChipColors(
                containerColor = if (summary.database.status == "CONNECTED") SuccessGreen.copy(alpha = 0.1f) else WarningAmber.copy(alpha = 0.1f),
                labelColor = if (summary.database.status == "CONNECTED") SuccessGreen else WarningAmber
              ),
              modifier = Modifier.height(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = FinTrackViewModel.formatRupiah(summary.financialSummary.totalBalance),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(16.dp))
          Divider(color = Slate800)
          Spacer(modifier = Modifier.height(16.dp))

          // Monthly In/Out Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pemasukan", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
              }
              Text(
                text = "+${FinTrackViewModel.formatRupiah(summary.financialSummary.monthlyIncome)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Emerald80
              )
            }

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pengeluaran", fontSize = 12.sp, color = DangerRed, fontWeight = FontWeight.Medium)
              }
              Text(
                text = "-${FinTrackViewModel.formatRupiah(summary.financialSummary.monthlyExpense)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DangerRed
              )
            }

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = InfoCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Arus Kas Bersih", fontSize = 12.sp, color = InfoCyan, fontWeight = FontWeight.Medium)
              }
              val net = summary.financialSummary.netCashflow
              Text(
                text = if (net >= 0) "+${FinTrackViewModel.formatRupiah(net)}" else "-${FinTrackViewModel.formatRupiah(-net)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (net >= 0) InfoCyan else DangerRed
              )
            }
          }

          if (summary.financialSummary.monthlyTradingProfit > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(InfoCyan.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Realisasi Profit Trading (INC_TRADING):", fontSize = 11.sp, color = InfoCyan)
              Text(
                FinTrackViewModel.formatRupiah(summary.financialSummary.monthlyTradingProfit),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = InfoCyan
              )
            }
          }
        }
      }
    }

    // 2. Quick Action Buttons
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ElevatedButton(
          onClick = { onAddTransaction(TransactionType.INCOME) },
          modifier = Modifier
            .weight(1f)
            .testTag("quick_add_income_button"),
          colors = ButtonDefaults.elevatedButtonColors(containerColor = SuccessGreen.copy(alpha = 0.15f), contentColor = SuccessGreen),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Pemasukan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        ElevatedButton(
          onClick = { onAddTransaction(TransactionType.EXPENSE) },
          modifier = Modifier
            .weight(1f)
            .testTag("quick_add_expense_button"),
          colors = ButtonDefaults.elevatedButtonColors(containerColor = DangerRed.copy(alpha = 0.15f), contentColor = DangerRed),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        ElevatedButton(
          onClick = { onAddTransaction(TransactionType.TRANSFER) },
          modifier = Modifier
            .weight(1f)
            .testTag("quick_transfer_button"),
          colors = ButtonDefaults.elevatedButtonColors(containerColor = InfoCyan.copy(alpha = 0.15f), contentColor = InfoCyan),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Transfer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 3. Saldo Seluruh Rekening
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Saldo Seluruh Rekening", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onNavigateToAccounts) {
          Text("Kelola", fontSize = 12.sp)
        }
      }
    }

    items(summary.accounts) { acc ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onReconcileAccount(acc) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
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
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(acc.accountName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(
                "${acc.accountType.displayName} • ${acc.currency}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              FinTrackViewModel.formatRupiah(acc.calculatedBalance),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp
            )
            Text("Ketuk utk Rekonsiliasi", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    // 4. Peringatan Anggaran
    val warningBudgets = summary.budgets.filter { it.status == "WARNING" || it.status == "EXCEEDED" }
    if (warningBudgets.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DangerRed.copy(alpha = 0.4f))),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Peringatan Batas Anggaran!", fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            warningBudgets.forEach { b ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(b.categoryName, fontSize = 12.sp)
                Text("${b.percentage}% (${FinTrackViewModel.formatRupiah(b.spentAmount)} / ${FinTrackViewModel.formatRupiah(b.budgetAmount)})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
              }
            }
          }
        }
      }
    }

    // 5. Transaksi Terbaru
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Transaksi Terbaru", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onNavigateToTransactions) {
          Text("Lihat Semua", fontSize = 12.sp)
        }
      }
    }

    items(summary.recentTransactions.take(5)) { tx ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                  when (tx.transactionType) {
                    TransactionType.INCOME -> SuccessGreen.copy(alpha = 0.2f)
                    TransactionType.EXPENSE -> DangerRed.copy(alpha = 0.2f)
                    TransactionType.TRANSFER -> InfoCyan.copy(alpha = 0.2f)
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when (tx.transactionType) {
                  TransactionType.INCOME -> Icons.Default.ArrowDownward
                  TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                  TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                },
                contentDescription = null,
                tint = when (tx.transactionType) {
                  TransactionType.INCOME -> SuccessGreen
                  TransactionType.EXPENSE -> DangerRed
                  TransactionType.TRANSFER -> InfoCyan
                },
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(tx.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(
                "${tx.transactionDate} • ${tx.categoryId ?: tx.transactionType.wireName.uppercase()}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Column(horizontalAlignment = Alignment.End) {
            val prefix = when (tx.transactionType) {
              TransactionType.INCOME -> "+"
              TransactionType.EXPENSE -> "-"
              TransactionType.TRANSFER -> "⇄ "
            }
            Text(
              "$prefix${FinTrackViewModel.formatRupiah(tx.amount)}",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              color = when (tx.transactionType) {
                TransactionType.INCOME -> SuccessGreen
                TransactionType.EXPENSE -> DangerRed
                TransactionType.TRANSFER -> InfoCyan
              }
            )
            if (tx.categoryId == "INC_TRADING") {
              Text("Profit Trading", fontSize = 10.sp, color = InfoCyan)
            }
          }
        }
      }
    }
  }
}
