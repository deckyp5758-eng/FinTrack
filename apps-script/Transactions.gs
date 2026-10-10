/**
 * FinTrack AI — Personal Finance Manager
 * Transactions.gs — Logika Transaksi pada sheet Transaksi (A1:M1)
 *
 * Kolom: transaction_id | transaction_date | transaction_type | category_id | source_account_id | destination_account_id | amount | description | status | created_at | updated_at | reference_id | notes
 *
 * Aturan Posisi & Validasi:
 * - expense: butuh category_id, source_account_id, amount, status ('posted')
 * - income: butuh category_id, destination_account_id, amount, status ('posted')
 * - transfer: butuh source_account_id, destination_account_id, amount, status ('posted'), dan category_id HARUS KOSONG.
 * - transfer: source_account_id != destination_account_id
 * - modal trading yang ditarik BUKAN keuntungan. Hanya keuntungan terealisasi yang memakai INC_TRADING.
 * - status: 'posted' atau 'void'.
 */

/**
 * Mengambil daftar transaksi dengan opsi filter
 */
function getTransactions(filterOptions) {
  var opts = filterOptions || {};
  var rows = readSheetData(SHEET_NAMES.TRANSAKSI);

  var result = rows.filter(function(tx) {
    var status = String(tx.status || "").toLowerCase().trim();
    if (!opts.includeVoid && status === "void") {
      return false;
    }

    if (opts.startDate && tx.transaction_date < opts.startDate) {
      return false;
    }
    if (opts.endDate && tx.transaction_date > opts.endDate) {
      return false;
    }

    if (opts.type && String(tx.transaction_type).toLowerCase() !== String(opts.type).toLowerCase()) {
      return false;
    }

    if (opts.accountId) {
      var matchSrc = String(tx.source_account_id) === String(opts.accountId);
      var matchDest = String(tx.destination_account_id) === String(opts.accountId);
      if (!matchSrc && !matchDest) return false;
    }

    if (opts.categoryId && String(tx.category_id) !== String(opts.categoryId)) {
      return false;
    }

    return true;
  });

  result.sort(function(a, b) {
    if (a.transaction_date !== b.transaction_date) {
      return a.transaction_date < b.transaction_date ? 1 : -1;
    }
    return String(b.created_at) < String(a.created_at) ? -1 : 1;
  });

  return result;
}

/**
 * Mencatat transaksi baru (Wajib konfirmasi pengguna & atomic write dengan script lock)
 */
function recordTransaction(txData) {
  if (!txData.isUserConfirmed) {
    throw new Error("Transaksi ditolak: Transaksi harus dikonfirmasi oleh pengguna sebelum disimpan.");
  }

  var amount = Number(txData.amount);
  if (isNaN(amount) || amount <= 0) {
    throw new Error("Nominal amount harus berupa angka positif lebih dari 0.");
  }

  var type = String(txData.transaction_type || txData.type || "").toLowerCase().trim();
  if (["income", "expense", "transfer"].indexOf(type) === -1) {
    throw new Error("transaction_type tidak valid: " + type + ". Harus income, expense, atau transfer.");
  }

  var src = String(txData.source_account_id || txData.sourceAccountId || "").trim();
  var dest = String(txData.destination_account_id || txData.destAccountId || "").trim();
  var catId = String(txData.category_id || txData.categoryId || "").trim();

  // Validasi posisi kolom kondisional
  if (type === "expense") {
    if (!src) throw new Error("Pengeluaran (expense) wajib menentukan source_account_id.");
    if (!catId) throw new Error("Pengeluaran (expense) wajib menentukan category_id.");
  } else if (type === "income") {
    if (!dest) throw new Error("Pemasukan (income) wajib menentukan destination_account_id.");
    if (!catId) throw new Error("Pemasukan (income) wajib menentukan category_id.");

    // Validasi aturan trading: penarikan modal bukan keuntungan!
    if (catId === "INC_TRADING" || txData.isTradingRelated) {
      if (txData.tradingType === "CAPITAL_WITHDRAWAL") {
        throw new Error("Penarikan modal trading BUKAN keuntungan trading! Gunakan transaksi transfer dari rekening trading ke rekening bank/tujuan.");
      }
      if (!txData.tradingProfitOnly && catId === "INC_TRADING") {
        throw new Error("Hanya keuntungan trading terealisasi yang benar-benar diterima yang memakai kategori INC_TRADING.");
      }
    }
  } else if (type === "transfer") {
    if (!src || !dest) {
      throw new Error("Transfer dana membutuhkan source_account_id dan destination_account_id.");
    }
    if (src === dest) {
      throw new Error("source_account_id dan destination_account_id transfer tidak boleh sama.");
    }
    catId = ""; // Kontrak audit: Kategori transfer harus kosong!
  }

  return withScriptLock(function() {
    var date = txData.transaction_date || txData.date || todayJakartaString();
    var desc = String(txData.description || "").trim();
    var refId = String(txData.reference_id || txData.idempotencyKey || "").trim();

    // Pencegahan Transaksi Ganda (Idempotency & Duplicate Check)
    var recent = readSheetData(SHEET_NAMES.TRANSAKSI);
    for (var i = recent.length - 1; i >= Math.max(0, recent.length - 30); i--) {
      var ex = recent[i];
      if (refId && ex.reference_id === refId) {
        throw new Error("Duplikasi Terdeteksi: Transaksi dengan reference_id '" + refId + "' sudah tercatat.");
      }
      if (ex.status === "posted" &&
          ex.transaction_date === date &&
          Number(ex.amount) === amount &&
          ex.transaction_type === type &&
          ex.source_account_id === src &&
          ex.destination_account_id === dest &&
          ex.description === desc) {
        throw new Error("Peringatan Duplikasi: Transaksi serupa sudah tersimpan baru-baru ini.");
      }
    }

    var txId = txData.transaction_id || ("TX-" + Utilities.getUuid().substring(0, 8).toUpperCase());
    var now = nowJakartaString();

    var record = {
      transaction_id: txId,
      transaction_date: date,
      transaction_type: type,
      category_id: catId,
      source_account_id: src,
      destination_account_id: dest,
      amount: amount,
      description: desc || "Transaksi FinTrack",
      status: "posted",
      created_at: now,
      updated_at: now,
      reference_id: refId,
      notes: String(txData.notes || "")
    };

    appendRowData(SHEET_NAMES.TRANSAKSI, record);

    logActivity("create_transaction", "transaction", txId, {
      type: type,
      amount: amount,
      src: src,
      dest: dest,
      category: catId
    }, "success", record.notes, refId);

    return {
      success: true,
      transaction: record
    };
  });
}

/**
 * Membatalkan transaksi (Ubah status ke 'void' dan catat ke Log_Aktivitas)
 */
function voidTransaction(transactionId, reason) {
  if (!transactionId) {
    throw new Error("transaction_id wajib disertakan untuk pembatalan.");
  }

  return withScriptLock(function() {
    var now = nowJakartaString();
    var success = updateRowById(SHEET_NAMES.TRANSAKSI, "transaction_id", transactionId, {
      status: "void",
      updated_at: now,
      notes: "VOID: " + (reason || "Dibatalkan oleh pengguna")
    });

    if (!success) {
      throw new Error("Transaksi dengan ID '" + transactionId + "' tidak ditemukan.");
    }

    logActivity("void_transaction", "transaction", transactionId, {
      reason: reason || "Dibatalkan oleh pengguna",
      voided_at: now
    }, "void", reason);

    return {
      success: true,
      message: "Transaksi " + transactionId + " berhasil dibatalkan (void) dan dikeluarkan dari saldo."
    };
  });
}
