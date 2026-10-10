/**
 * FinTrack AI — Personal Finance Manager
 * Database.gs — Google Sheets Database Adapter (Sesuai Audit Kontrak Persis)
 *
 * Spreadsheet ID: 1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk
 * Nama Database: FinTrack AI — Personal Finance Database
 * Timezone: Asia/Jakarta | Locale: in_ID (id-ID) | Currency: IDR
 *
 * Pemetaan SheetId & Kontrak Header:
 * 100: Pengaturan (A1:D1)       -> setting_key, setting_value, description, updated_at
 * 101: Rekening (A1:J1)         -> account_id, account_name, account_type, opening_balance, opening_balance_date, currency, is_active, notes, created_at, updated_at
 * 102: Kategori (A1:I1)         -> category_id, category_name, transaction_type, monthly_budget, is_active, sort_order, notes, created_at, updated_at
 * 103: Transaksi (A1:M1)        -> transaction_id, transaction_date, transaction_type, category_id, source_account_id, destination_account_id, amount, description, status, created_at, updated_at, reference_id, notes
 * 104: Anggaran (A1:I1)         -> budget_id, budget_month, category_id, budget_amount, warning_threshold_percent, is_active, notes, created_at, updated_at
 * 105: Target_Tabungan (A1:K1)  -> target_id, target_name, target_amount, current_amount, target_date, linked_account_id, priority, is_active, notes, created_at, updated_at
 * 106: Ringkasan_Bulanan (A1:M1)-> summary_id, summary_month, opening_balance_total, income_total, expense_total, transfer_in_total, transfer_out_total, closing_balance_total, savings_change, transaction_count, last_calculated_at, reconciliation_status, notes
 * 107: Log_Aktivitas (A1:J1)    -> log_id, event_timestamp, actor, action, entity_type, entity_id, change_summary, request_id, result, notes
 * 108: Dokumentasi_DB (A1:D1)   -> section, item, detail, status
 */

var SPREADSHEET_ID = "1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk";
var TIMEZONE = "Asia/Jakarta";

var SHEET_NAMES = {
  PENGATURAN: "Pengaturan",
  REKENING: "Rekening",
  KATEGORI: "Kategori",
  TRANSAKSI: "Transaksi",
  ANGGARAN: "Anggaran",
  TARGET_TABUNGAN: "Target_Tabungan",
  RINGKASAN_BULANAN: "Ringkasan_Bulanan",
  LOG_AKTIVITAS: "Log_Aktivitas",
  DOKUMENTASI_DB: "Dokumentasi_DB"
};

/**
 * Mendapatkan instance Spreadsheet utama
 * @return {GoogleAppsScript.Spreadsheet.Spreadsheet}
 */
function getSpreadsheet() {
  try {
    return SpreadsheetApp.openById(SPREADSHEET_ID);
  } catch (e) {
    throw new Error("Gagal membuka Google Sheets (ID: " + SPREADSHEET_ID + "): " + e.message);
  }
}

/**
 * Mendapatkan sheet berdasarkan nama dengan validasi ketat
 * @param {string} sheetName
 * @return {GoogleAppsScript.Spreadsheet.Sheet}
 */
function getSheetStrict(sheetName) {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    throw new Error("Sheet '" + sheetName + "' tidak ditemukan dalam spreadsheet.");
  }
  return sheet;
}

/**
 * Eksekusi dengan Script Lock untuk menjamin atomic write dan mencegah race condition
 */
function withScriptLock(callback) {
  var lock = LockService.getScriptLock();
  try {
    lock.waitLock(10000); // Tunggu hingga 10 detik
    return callback();
  } catch (e) {
    throw new Error("Gagal memperoleh lock transaksi: " + e.message);
  } finally {
    lock.releaseLock();
  }
}

/**
 * Membaca data sheet dan mengembalikan array of objects sesuai header baris 1
 * Data dimulai persis dari baris 2.
 * @param {string} sheetName
 * @return {Array<Object>}
 */
function readSheetData(sheetName) {
  var sheet = getSheetStrict(sheetName);
  var data = sheet.getDataRange().getValues();
  if (!data || data.length < 2) {
    return [];
  }
  var headers = data[0].map(function(h) {
    return String(h).trim();
  });
  var rows = [];
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    var obj = { _rowIndex: i + 1 };
    var isEmpty = true;
    for (var j = 0; j < headers.length; j++) {
      var val = row[j];
      if (val !== "" && val !== null && val !== undefined) {
        isEmpty = false;
      }
      obj[headers[j]] = val;
    }
    if (!isEmpty) {
      rows.push(obj);
    }
  }
  return rows;
}

/**
 * Menambahkan satu baris baru ke sheet sesuai urutan header persis
 * @param {string} sheetName
 * @param {Object} rowData
 * @return {Object} rowData
 */
function appendRowData(sheetName, rowData) {
  var sheet = getSheetStrict(sheetName);
  var data = sheet.getDataRange().getValues();
  var headers = data[0].map(function(h) {
    return String(h).trim();
  });

  var newRow = [];
  for (var i = 0; i < headers.length; i++) {
    var header = headers[i];
    newRow.push(rowData[header] !== undefined ? rowData[header] : "");
  }
  sheet.appendRow(newRow);
  return rowData;
}

/**
 * Mengupdate baris yang sesuai dengan primary key kolom pertama
 * @param {string} sheetName
 * @param {string} idColName
 * @param {string} idValue
 * @param {Object} updatedFields
 * @return {boolean}
 */
function updateRowById(sheetName, idColName, idValue, updatedFields) {
  var sheet = getSheetStrict(sheetName);
  var data = sheet.getDataRange().getValues();
  var headers = data[0].map(function(h) {
    return String(h).trim();
  });
  var idIndex = headers.indexOf(idColName);
  if (idIndex === -1) {
    throw new Error("Kolom '" + idColName + "' tidak ditemukan di sheet " + sheetName);
  }

  for (var i = 1; i < data.length; i++) {
    if (String(data[i][idIndex]) === String(idValue)) {
      for (var key in updatedFields) {
        var colIndex = headers.indexOf(key);
        if (colIndex !== -1) {
          sheet.getRange(i + 1, colIndex + 1).setValue(updatedFields[key]);
        }
      }
      return true;
    }
  }
  return false;
}

/**
 * Format tanggal dan waktu dalam zona waktu Asia/Jakarta
 */
function nowJakartaString() {
  return Utilities.formatDate(new Date(), TIMEZONE, "yyyy-MM-dd HH:mm:ss");
}

function todayJakartaString() {
  return Utilities.formatDate(new Date(), TIMEZONE, "yyyy-MM-dd");
}

function currentMonthJakartaString() {
  return Utilities.formatDate(new Date(), TIMEZONE, "yyyy-MM");
}
