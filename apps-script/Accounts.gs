/**
 * FinTrack AI — Personal Finance Manager
 * Accounts.gs — Logika Rekening & Perhitungan Saldo pada sheet Rekening (A1:J1)
 *
 * Kolom: account_id | account_name | account_type | opening_balance | opening_balance_date | currency | is_active | notes | created_at | updated_at
 * Relasi: account_id dipakai oleh Transaksi.source_account_id, Transaksi.destination_account_id, dan Target_Tabungan.linked_account_id.
 */

/**
 * Mengambil seluruh rekening dengan saldo terhitung secara deterministik
 * @return {Array<Object>}
 */
function getAllAccountsWithBalances() {
  var accounts = readSheetData(SHEET_NAMES.REKENING);
  var transactions = readSheetData(SHEET_NAMES.TRANSAKSI);

  // Inisialisasi peta saldo dengan opening_balance yang terkonfirmasi
  var balanceMap = {};
  for (var a = 0; a < accounts.length; a++) {
    var acc = accounts[a];
    var openBal = Number(acc.opening_balance) || 0;
    balanceMap[acc.account_id] = openBal;
  }

  // Iterasi transaksi yang berstatus 'posted'
  for (var i = 0; i < transactions.length; i++) {
    var tx = transactions[i];
    var status = String(tx.status || "").toLowerCase().trim();
    if (status !== "posted") {
      continue; // Lewati void atau status non-posted
    }

    var amount = Number(tx.amount) || 0;
    var txType = String(tx.transaction_type || "").toLowerCase().trim();
    var src = String(tx.source_account_id || "").trim();
    var dest = String(tx.destination_account_id || "").trim();

    if (txType === "income") {
      // Pemasukan menambah saldo rekening tujuan
      if (dest && balanceMap[dest] !== undefined) {
        balanceMap[dest] += amount;
      }
    } else if (txType === "expense") {
      // Pengeluaran mengurangi saldo rekening sumber
      if (src && balanceMap[src] !== undefined) {
        balanceMap[src] -= amount;
      }
    } else if (txType === "transfer") {
      // Transfer mengurangi sumber dan menambah tujuan
      if (src && balanceMap[src] !== undefined) {
        balanceMap[src] -= amount;
      }
      if (dest && balanceMap[dest] !== undefined) {
        balanceMap[dest] += amount;
      }
    }
  }

  return accounts.map(function(acc) {
    var isActive = String(acc.is_active).toUpperCase() === "TRUE" || acc.is_active === true;
    return {
      account_id: String(acc.account_id),
      account_name: String(acc.account_name),
      account_type: String(acc.account_type || "bank").toLowerCase(),
      opening_balance: Number(acc.opening_balance) || 0,
      opening_balance_date: String(acc.opening_balance_date || ""),
      currency: "IDR",
      is_active: isActive,
      notes: String(acc.notes || ""),
      created_at: String(acc.created_at || ""),
      updated_at: String(acc.updated_at || ""),
      calculatedBalance: balanceMap[acc.account_id] || 0
    };
  });
}

/**
 * Membuat rekening baru
 * @param {Object} accountData
 */
function createAccount(accountData) {
  if (!accountData.account_name || !accountData.account_name.trim()) {
    throw new Error("Nama rekening wajib diisi.");
  }
  var type = String(accountData.account_type || "bank").toLowerCase();
  if (["bank", "cash", "trading", "other"].indexOf(type) === -1) {
    throw new Error("account_type tidak valid: " + type + ". Harus bank, cash, trading, atau other.");
  }

  var accId = accountData.account_id || ("ACC-" + Utilities.getUuid().substring(0, 8).toUpperCase());
  var now = nowJakartaString();
  var openBal = Number(accountData.opening_balance) || 0;
  var openDate = accountData.opening_balance_date || (openBal > 0 ? todayJakartaString() : "");

  var newAccount = {
    account_id: accId,
    account_name: accountData.account_name.trim(),
    account_type: type,
    opening_balance: openBal,
    opening_balance_date: openDate,
    currency: "IDR",
    is_active: true,
    notes: accountData.notes || "",
    created_at: now,
    updated_at: now
  };

  return withScriptLock(function() {
    appendRowData(SHEET_NAMES.REKENING, newAccount);
    logActivity("create_account", "account", accId, "Membuat rekening " + newAccount.account_name + " (Saldo awal: " + openBal + ")", "success", newAccount.notes);
    return newAccount;
  });
}

/**
 * Rekonsiliasi saldo fisik: mencocokkan saldo tercatat dan mencatat transaksi penyesuaian jika ada selisih
 */
function reconcileAccountBalance(accountId, actualPhysicalBalance, reason) {
  return withScriptLock(function() {
    var accounts = getAllAccountsWithBalances();
    var target = null;
    for (var i = 0; i < accounts.length; i++) {
      if (accounts[i].account_id === accountId) {
        target = accounts[i];
        break;
      }
    }
    if (!target) {
      throw new Error("Rekening tidak ditemukan: " + accountId);
    }

    var diff = actualPhysicalBalance - target.calculatedBalance;
    if (diff === 0) {
      logActivity("reconcile_account", "account", accountId, "Rekonsiliasi cocok (Tanpa selisih)", "success", reason);
      return {
        status: "BALANCED",
        message: "Saldo tercatat sudah cocok dengan saldo fisik aktual."
      };
    }

    // Buat transaksi penyesuaian yang valid (posted)
    var txId = "TX-ADJ-" + Utilities.getUuid().substring(0, 8).toUpperCase();
    var isPositive = diff > 0;
    var now = nowJakartaString();

    var adjTx = {
      transaction_id: txId,
      transaction_date: todayJakartaString(),
      transaction_type: isPositive ? "income" : "expense",
      category_id: isPositive ? "INC_OTHER" : "EXP_OTHER",
      source_account_id: isPositive ? "" : accountId,
      destination_account_id: isPositive ? accountId : "",
      amount: Math.abs(diff),
      description: "Penyesuaian Rekonsiliasi Saldo: " + (reason || "Penyesuaian fisik") + " (Selisih: " + diff + ")",
      status: "posted",
      created_at: now,
      updated_at: now,
      reference_id: "RECON-" + accountId,
      notes: "Rekonsiliasi dari " + target.calculatedBalance + " ke " + actualPhysicalBalance
    };

    appendRowData(SHEET_NAMES.TRANSAKSI, adjTx);
    logActivity("reconcile_account", "account", accountId, {
      calculatedBefore: target.calculatedBalance,
      actualPhysical: actualPhysicalBalance,
      difference: diff,
      adjustmentTxId: txId
    }, "success", reason);

    return {
      status: "RECONCILED",
      adjustmentTransactionId: txId,
      previousBalance: target.calculatedBalance,
      newBalance: actualPhysicalBalance,
      difference: diff
    };
  });
}
