/**
 * FinTrack AI — Personal Finance Manager
 * AuditLog.gs — Jejak Audit pada sheet Log_Aktivitas (A1:J1)
 *
 * Kolom: log_id | event_timestamp | actor | action | entity_type | entity_id | change_summary | request_id | result | notes
 */

/**
 * Mencatat entri log aktivitas baru
 * @param {string} action Nama tindakan (e.g., 'create_transaction', 'void_transaction', 'transfer', 'reconcile_account')
 * @param {string} entityType Jenis entitas (e.g., 'transaction', 'account', 'budget', 'saving_target')
 * @param {string} entityId ID entitas terkait
 * @param {string} changeSummary Ringkasan perubahan
 * @param {string} result 'success' | 'failure' | 'void'
 * @param {string} notes Catatan opsional
 * @param {string} requestId ID idempotensi / request
 */
function logActivity(action, entityType, entityId, changeSummary, result, notes, requestId) {
  try {
    var email = Session.getActiveUser().getEmail() || "owner@fintrack.ai";
    var logEntry = {
      log_id: "LOG-" + Utilities.getUuid(),
      event_timestamp: nowJakartaString(),
      actor: email,
      action: action,
      entity_type: entityType,
      entity_id: entityId || "",
      change_summary: typeof changeSummary === "object" ? JSON.stringify(changeSummary) : String(changeSummary || ""),
      request_id: requestId || ("REQ-" + Utilities.getUuid().substring(0, 8)),
      result: result || "success",
      notes: notes || ""
    };
    appendRowData(SHEET_NAMES.LOG_AKTIVITAS, logEntry);
    return logEntry;
  } catch (err) {
    Logger.log("Peringatan: Gagal menyimpan Log_Aktivitas: " + err.message);
    return null;
  }
}

/**
 * Mengambil riwayat log aktivitas terbaru
 * @param {number} limit Maksimal entri
 * @return {Array<Object>}
 */
function getRecentAuditLogs(limit) {
  var max = limit || 40;
  var logs = readSheetData(SHEET_NAMES.LOG_AKTIVITAS);
  logs.sort(function(a, b) {
    return new Date(b.event_timestamp).getTime() - new Date(a.event_timestamp).getTime();
  });
  return logs.slice(0, max);
}
