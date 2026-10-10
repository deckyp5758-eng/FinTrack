/**
 * FinTrack AI — Personal Finance Manager
 * SavingsGoals.gs — Logika Target Tabungan pada sheet Target_Tabungan (A1:K1)
 *
 * Kolom: target_id | target_name | target_amount | current_amount | target_date | linked_account_id | priority | is_active | notes | created_at | updated_at
 * Aturan: Target tabungan bukan uang tambahan. Backend mencegah double counting saldo rekening.
 */

/**
 * Mengambil daftar target tabungan
 * @return {Array<Object>}
 */
function getSavingsGoals() {
  var rows = readSheetData(SHEET_NAMES.TARGET_TABUNGAN);
  return rows.map(function(goal) {
    var target = Number(goal.target_amount) || 0;
    var current = Number(goal.current_amount) || 0;
    var pct = target > 0 ? (current / target) * 100 : 0;
    var isActive = String(goal.is_active).toUpperCase() === "TRUE" || goal.is_active === true;

    return {
      target_id: String(goal.target_id),
      target_name: String(goal.target_name),
      target_amount: target,
      current_amount: current,
      remaining_amount: Math.max(0, target - current),
      progressPercentage: Math.min(100, Math.round(pct * 10) / 10),
      target_date: String(goal.target_date || "-"),
      linked_account_id: String(goal.linked_account_id || ""),
      priority: Number(goal.priority) || 1,
      is_active: isActive,
      notes: String(goal.notes || "")
    };
  });
}

/**
 * Membuat target tabungan baru
 */
function createSavingsGoal(goalData) {
  if (!goalData.target_name || !goalData.target_name.trim()) {
    throw new Error("target_name wajib diisi.");
  }
  var target = Number(goalData.target_amount);
  if (isNaN(target) || target <= 0) {
    throw new Error("target_amount harus berupa angka lebih dari 0.");
  }

  return withScriptLock(function() {
    var now = nowJakartaString();
    var newId = "GOAL-" + Utilities.getUuid().substring(0, 8).toUpperCase();
    var current = Number(goalData.current_amount || goalData.initialDeposit) || 0;

    var newGoal = {
      target_id: newId,
      target_name: goalData.target_name.trim(),
      target_amount: target,
      current_amount: current,
      target_date: goalData.target_date || goalData.targetDate || "-",
      linked_account_id: goalData.linked_account_id || goalData.linkedAccountId || "",
      priority: Number(goalData.priority) || 1,
      is_active: true,
      notes: goalData.notes || "",
      created_at: now,
      updated_at: now
    };

    appendRowData(SHEET_NAMES.TARGET_TABUNGAN, newGoal);
    logActivity("create_saving_target", "saving_target", newId, "Membuat target tabungan: " + newGoal.target_name + " (Target: Rp " + target + ")", "success");
    return newGoal;
  });
}

/**
 * Menambahkan alokasi/setoran ke target tabungan
 */
function addSavingsContribution(targetId, amount) {
  var num = Number(amount);
  if (isNaN(num) || num <= 0) throw new Error("Nominal setoran harus lebih dari 0.");

  return withScriptLock(function() {
    var goals = readSheetData(SHEET_NAMES.TARGET_TABUNGAN);
    var target = null;
    for (var i = 0; i < goals.length; i++) {
      if (goals[i].target_id === targetId) {
        target = goals[i];
        break;
      }
    }

    if (!target) throw new Error("Target tabungan tidak ditemukan: " + targetId);

    var newCur = (Number(target.current_amount) || 0) + num;
    var now = nowJakartaString();

    updateRowById(SHEET_NAMES.TARGET_TABUNGAN, "target_id", targetId, {
      current_amount: newCur,
      updated_at: now
    });

    logActivity("contribute_saving_target", "saving_target", targetId, "Menyetor Rp " + num + " (Total baru: Rp " + newCur + ")", "success");

    return {
      target_id: targetId,
      current_amount: newCur,
      target_amount: Number(target.target_amount) || 0
    };
  });
}
