package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataProvider
import com.example.model.*
import com.example.ui.FinTrackViewModel
import com.example.ui.TransactionDraft
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
  accounts: List<Account>,
  initialType: TransactionType,
  onDismiss: () -> Unit,
  onRequestConfirm: (TransactionDraft) -> Unit
) {
  var selectedType by remember { mutableStateOf(initialType) }
  var amountText by remember { mutableStateOf("") }
  var sourceAccountId by remember {
    mutableStateOf(accounts.firstOrNull { it.accountType == AccountType.BANK }?.accountId ?: accounts.firstOrNull()?.accountId ?: "")
  }
  var destinationAccountId by remember {
    mutableStateOf(accounts.firstOrNull { it.accountId != sourceAccountId }?.accountId ?: "")
  }
  var selectedCategoryId by remember {
    mutableStateOf(
      if (initialType == TransactionType.INCOME) "INC_SALARY" else "EXP_FOOD"
    )
  }
  var description by remember { mutableStateOf("") }
  var referenceId by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var isTradingRelated by remember { mutableStateOf(false) }
  var tradingProfitOnly by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = when (selectedType) {
            TransactionType.INCOME -> Icons.Default.ArrowDownward
            TransactionType.EXPENSE -> Icons.Default.ArrowUpward
            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
          },
          contentDescription = null,
          tint = when (selectedType) {
            TransactionType.INCOME -> SuccessGreen
            TransactionType.EXPENSE -> DangerRed
            TransactionType.TRANSFER -> InfoCyan
          },
          modifier = Modifier.padding(end = 8.dp)
        )
        Text("Catat Transaksi", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Type Selector Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = selectedType == TransactionType.EXPENSE,
            onClick = {
              selectedType = TransactionType.EXPENSE
              selectedCategoryId = "EXP_FOOD"
            },
            label = { Text("Pengeluaran") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = DangerRed.copy(alpha = 0.2f),
              selectedLabelColor = DangerRed
            )
          )
          FilterChip(
            selected = selectedType == TransactionType.INCOME,
            onClick = {
              selectedType = TransactionType.INCOME
              selectedCategoryId = "INC_SALARY"
            },
            label = { Text("Pemasukan") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SuccessGreen.copy(alpha = 0.2f),
              selectedLabelColor = SuccessGreen
            )
          )
          FilterChip(
            selected = selectedType == TransactionType.TRANSFER,
            onClick = { selectedType = TransactionType.TRANSFER },
            label = { Text("Transfer") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = InfoCyan.copy(alpha = 0.2f),
              selectedLabelColor = InfoCyan
            )
          )
        }

        // Amount Input
        OutlinedTextField(
          value = amountText,
          onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            amountText = digits
            validationError = null
          },
          label = { Text("Nominal (IDR)") },
          placeholder = { Text("Contoh: 150000") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Account selectors depending on type
        if (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.TRANSFER) {
          Text("Rekening Sumber (Pengurang Saldo):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          AccountDropdown(
            accounts = accounts,
            selectedId = sourceAccountId,
            onSelected = { sourceAccountId = it }
          )
        }

        if (selectedType == TransactionType.INCOME || selectedType == TransactionType.TRANSFER) {
          Text("Rekening Tujuan (Penambah Saldo):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          AccountDropdown(
            accounts = accounts,
            selectedId = destinationAccountId,
            onSelected = { destinationAccountId = it }
          )
        }

        // Category (Audited: only for income or expense, transfer must be empty)
        if (selectedType != TransactionType.TRANSFER) {
          Text("Kategori Database:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          CategoryDropdown(
            categories = MockDataProvider.categories.filter {
              if (selectedType == TransactionType.INCOME) it.transactionType == TransactionType.INCOME else it.transactionType == TransactionType.EXPENSE
            },
            selectedId = selectedCategoryId,
            onSelected = { selectedCategoryId = it }
          )
        }

        // Trading Rule Checkbox (For Income or INC_TRADING)
        if (selectedType == TransactionType.INCOME) {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                  checked = isTradingRelated || selectedCategoryId == "INC_TRADING",
                  onCheckedChange = {
                    isTradingRelated = it
                    if (it) selectedCategoryId = "INC_TRADING"
                  }
                )
                Text("Keuntungan Trading Saham/Crypto", fontSize = 12.sp, fontWeight = FontWeight.Medium)
              }
              if (isTradingRelated || selectedCategoryId == "INC_TRADING") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(
                    checked = tradingProfitOnly,
                    onCheckedChange = { tradingProfitOnly = it }
                  )
                  Text("Hanya Keuntungan Terealisasi Diterima", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
                Text(
                  "Aturan Kontrak: Modal trading ditarik BUKAN keuntungan. Modal dipindahkan via Transfer.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(start = 12.dp)
                )
              }
            }
          }
        }

        // Description Input
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Keterangan Transaksi") },
          placeholder = { Text("Contoh: Makan siang & kopi") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Reference ID (Idempotency)
        OutlinedTextField(
          value = referenceId,
          onValueChange = { referenceId = it },
          label = { Text("Reference ID (Opsional / Idempotensi)") },
          placeholder = { Text("Contoh: INV-100293") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        if (validationError != null) {
          Text(
            text = validationError ?: "",
            color = DangerRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toLongOrNull() ?: 0L
          if (amt <= 0) {
            validationError = "Nominal harus lebih dari 0."
            return@Button
          }
          if (selectedType == TransactionType.TRANSFER && sourceAccountId == destinationAccountId) {
            validationError = "Rekening sumber dan tujuan transfer tidak boleh sama."
            return@Button
          }
          if (selectedType == TransactionType.INCOME && selectedCategoryId == "INC_TRADING" && !tradingProfitOnly) {
            validationError = "Hanya keuntungan trading terealisasi yang diizinkan untuk INC_TRADING."
            return@Button
          }

          val draft = TransactionDraft(
            type = selectedType,
            amount = amt,
            sourceAccountId = if (selectedType != TransactionType.INCOME) sourceAccountId else null,
            destinationAccountId = if (selectedType != TransactionType.EXPENSE) destinationAccountId else null,
            categoryId = if (selectedType != TransactionType.TRANSFER) selectedCategoryId else null,
            description = description.ifBlank { "Transaksi FinTrack" },
            referenceId = referenceId.ifBlank { null },
            notes = notes,
            isTradingRelated = isTradingRelated || selectedCategoryId == "INC_TRADING",
            tradingProfitOnly = tradingProfitOnly
          )
          onRequestConfirm(draft)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Emerald40)
      ) {
        Text("Lanjut Konfirmasi")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun ConfirmTransactionDialog(
  draft: TransactionDraft,
  accounts: List<Account>,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  val srcAcc = accounts.find { it.accountId == draft.sourceAccountId }
  val dstAcc = accounts.find { it.accountId == draft.destinationAccountId }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
    },
    title = {
      Text("Konfirmasi Penyimpanan", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          "Periksa kembali data transaksi sebelum dikirim ke backend:",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              "Jenis: ${draft.type.wireName.uppercase()}",
              fontWeight = FontWeight.Bold,
              color = when (draft.type) {
                TransactionType.INCOME -> SuccessGreen
                TransactionType.EXPENSE -> DangerRed
                TransactionType.TRANSFER -> InfoCyan
              }
            )
            Text("Nominal: ${FinTrackViewModel.formatRupiah(draft.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            if (srcAcc != null) {
              Text("Sumber: ${srcAcc.accountName} (- ${FinTrackViewModel.formatRupiah(draft.amount)})", fontSize = 12.sp)
            }
            if (dstAcc != null) {
              Text("Tujuan: ${dstAcc.accountName} (+ ${FinTrackViewModel.formatRupiah(draft.amount)})", fontSize = 12.sp)
            }
            if (draft.categoryId != null) {
              Text("Kategori: ${draft.categoryId}", fontSize = 12.sp)
            }
            Text("Keterangan: ${draft.description}", fontSize = 12.sp)
          }
        }

        Text(
          "Backend Google Sheets akan mencatat transaksi ini sebagai status 'posted'.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = Emerald40)
      ) {
        Text("Ya, Simpan Transaksi")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Kembali Edit")
      }
    }
  )
}

@Composable
fun VoidTransactionDialog(
  tx: Transaction,
  onDismiss: () -> Unit,
  onConfirmVoid: (String) -> Unit
) {
  var reason by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
    },
    title = {
      Text("Batalkan (Void) Transaksi", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          "Transaksi '${tx.transactionId}' senilai ${FinTrackViewModel.formatRupiah(tx.amount)} akan diubah statusnya menjadi VOID dan dikeluarkan dari kalkulasi saldo.",
          fontSize = 13.sp
        )
        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Alasan Pembatalan") },
          placeholder = { Text("Contoh: Salah input nominal / duplikat") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmVoid(reason.ifBlank { "Dibatalkan oleh pengguna" }) },
        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
      ) {
        Text("Konfirmasi Void")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun ReconcileAccountDialog(
  account: Account,
  onDismiss: () -> Unit,
  onConfirmReconcile: (Long, String) -> Unit
) {
  var physicalInput by remember { mutableStateOf(account.calculatedBalance.toString()) }
  var reason by remember { mutableStateOf("") }

  val actualLong = physicalInput.toLongOrNull() ?: account.calculatedBalance
  val diff = actualLong - account.calculatedBalance

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Rekonsiliasi: ${account.accountName}", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          "Saldo Tercatat di Sistem: ${FinTrackViewModel.formatRupiah(account.calculatedBalance)}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
          value = physicalInput,
          onValueChange = { physicalInput = it.filter { c -> c.isDigit() } },
          label = { Text("Saldo Fisik Aktual Sekarang") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          prefix = { Text("Rp ") },
          modifier = Modifier.fillMaxWidth()
        )

        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (diff == 0L) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Selisih Penyesuaian:", fontSize = 12.sp)
            Text(
              text = if (diff >= 0) "+${FinTrackViewModel.formatRupiah(diff)}" else "-${FinTrackViewModel.formatRupiah(-diff)}",
              fontWeight = FontWeight.Bold,
              color = if (diff == 0L) SuccessGreen else if (diff > 0) SuccessGreen else DangerRed
            )
          }
        }

        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Keterangan Penyesuaian") },
          placeholder = { Text("Contoh: Selisih biaya admin bulanan") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmReconcile(actualLong, reason.ifBlank { "Rekonsiliasi saldo fisik berkala" }) },
        colors = ButtonDefaults.buttonColors(containerColor = Emerald40)
      ) {
        Text("Terapkan Penyesuaian")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}

@Composable
fun AddAccountDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, AccountType, Long, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(AccountType.BANK) }
  var openingBalanceText by remember { mutableStateOf("0") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Tambah Rekening Baru", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nama Rekening / Akun") },
          placeholder = { Text("Contoh: BCA Utama") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Jenis Akun (Audited Contract):", fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          AccountType.values().forEach { t ->
            FilterChip(
              selected = selectedType == t,
              onClick = { selectedType = t },
              label = { Text(t.displayName, fontSize = 11.sp) }
            )
          }
        }

        OutlinedTextField(
          value = openingBalanceText,
          onValueChange = { openingBalanceText = it.filter { c -> c.isDigit() } },
          label = { Text("Saldo Awal (opening_balance)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          prefix = { Text("Rp ") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Catatan Rekening") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val openBal = openingBalanceText.toLongOrNull() ?: 0L
            onConfirm(name, selectedType, openBal, notes)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Emerald40)
      ) {
        Text("Buat Rekening")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Batal") }
    }
  )
}

@Composable
fun ContributeGoalDialog(
  goal: SavingsGoal,
  onDismiss: () -> Unit,
  onConfirm: (Long) -> Unit
) {
  var amountText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Setor Tabungan: ${goal.targetName}", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Terkumpul: ${FinTrackViewModel.formatRupiah(goal.currentAmount)} / ${FinTrackViewModel.formatRupiah(goal.targetAmount)}", fontSize = 12.sp)
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { c -> c.isDigit() } },
          label = { Text("Jumlah Setoran (IDR)") },
          placeholder = { Text("Contoh: 500000") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          prefix = { Text("Rp ") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toLongOrNull() ?: 0L
          if (amt > 0) onConfirm(amt)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Emerald40)
      ) {
        Text("Simpan Setoran")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Batal") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
  accounts: List<Account>,
  selectedId: String,
  onSelected: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val current = accounts.find { it.accountId == selectedId } ?: accounts.firstOrNull()

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = Modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = current?.let { "${it.accountName} (${FinTrackViewModel.formatRupiah(it.calculatedBalance)})" } ?: "Pilih Rekening",
      onValueChange = {},
      readOnly = true,
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier = Modifier
        .menuAnchor()
        .fillMaxWidth()
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      accounts.forEach { acc ->
        DropdownMenuItem(
          text = {
            Column {
              Text(acc.accountName, fontWeight = FontWeight.SemiBold)
              Text(
                "${acc.accountType.displayName} • Saldo: ${FinTrackViewModel.formatRupiah(acc.calculatedBalance)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          },
          onClick = {
            onSelected(acc.accountId)
            expanded = false
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
  categories: List<Category>,
  selectedId: String,
  onSelected: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val current = categories.find { it.categoryId == selectedId } ?: categories.firstOrNull()

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = Modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = current?.let { "${it.categoryName} (${it.categoryId})" } ?: "Pilih Kategori",
      onValueChange = {},
      readOnly = true,
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier = Modifier
        .menuAnchor()
        .fillMaxWidth()
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      categories.forEach { cat ->
        DropdownMenuItem(
          text = {
            Column {
              Text(cat.categoryName, fontWeight = FontWeight.SemiBold)
              Text(cat.categoryId, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          },
          onClick = {
            onSelected(cat.categoryId)
            expanded = false
          }
        )
      }
    }
  }
}
