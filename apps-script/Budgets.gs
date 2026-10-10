/**
 * FinTrack AI — Personal Finance Manager
 * Budgets.gs — Logika Anggaran pada sheet Anggaran (A1:I1)
 *
 * Kolom: budget_id | budget_month | category_id | budget_amount | warning_threshold_percent | is_active | notes | created_at | updated_at
 * Aturan: Realisasi anggaran tidak ditulis di sheet ini, melainkan dihitung secara real-time dari transaksi posted.
 */

/**
 * Mengambil status anggaran bulanan beserta realisasi pengeluaran aktual
 * @param {string} month Format 'YYYY-MM', e.g. '2026-10'
 * @return {Array<Object>}
 */
function getMonthlyBudgetStatus(month) {
  var targetMonth = month || currentMonthJakartaString();
  var budgets = readSheetData(SHEET_NAMES.ANGGARAN);
  var categories = readSheetData(SHEET_NAMES.KATEGORI);
  var transactions = readSheetData(SHEET_NAMES.TRANSAKSI);

  var categoryMap = {};
  for (var c = 0; c < categories.length; c++) {
    categoryMap[categories[c].category_id] = categories[c];
  }

  // Hitung realisasi pengeluaran posted per kategori
  var actualSpentMap = {};
  for (var i = 0; i < transactions.length; i++) {
    var tx = transactions[i];
    var status = String(tx.status || "").toLowerCase().trim();
    if (status !== "posted") continue;

    var type = String(tx.transaction_type || "").toLowerCase().trim();
    var txDate = String(tx.transaction_date || "");
    if (type === "expense" && txDate.indexOf(targetMonth) === 0) {
      var cat = tx.category_id || "EXP_OTHER";
      actualSpentMap[cat] = (actualSpentMap[cat] || 0) + (Number(tx.amount) || 0);
    }
  }

  // Filter anggaran bulan target
  var monthBudgets = budgets.filter(function(b) {
    var isAct = String(b.is_active).toUpperCase() === "TRUE" || b.is_active === true;
    return b.budget_month === targetMonth && isAct;
  });

  return monthBudgets.map(function(b) {
    var cat = categoryMap[b.category_id] || { category_name: b.category_id };
    var budgetAmount = Number(b.budget_amount) || 0;
    var spent = actualSpentMap[b.category_id] || 0;
    var percentage = budgetAmount > 0 ? (spent / budgetAmount) * 100 : 0;
    var remaining = budgetAmount - spent;
    var threshold = Number(b.warning_threshold_percent) || 80;

    var status = "NORMAL";
    if (percentage >= 100) {
      status = "EXCEEDED";
    } else if (percentage >= threshold) {
      status = "WARNING";
    }

    return {
      budget_id: b.budget_id,
      budget_month: targetMonth,
      category_id: b.category_id,
      category_name: cat.category_name,
      budget_amount: budgetAmount,
      spent_amount: spent,
      remaining_amount: remaining,
      percentage: Math.round(percentage * 10) / 10,
      warning_threshold_percent: threshold,
      status: status
    };
  });
}

/**
 * Menyimpan / memperbarui anggaran
 */
function upsertBudget(budgetData) {
  var month = budgetData.budget_month || budgetData.month || currentMonthJakartaString();
  var catId = budgetData.category_id || budgetData.categoryId;
  var amount = Number(budgetData.budget_amount || budgetData.amount);

  if (!catId) throw new Error("category_id wajib ditentukan untuk anggaran.");
  if (isNaN(amount) || amount < 0) throw new Error("Nominal budget_amount tidak valid.");

  return withScriptLock(function() {
    var budgets = readSheetData(SHEET_NAMES.ANGGARAN);
    var found = null;
    for (var i = 0; i < budgets.length; i++) {
      if (budgets[i].budget_month === month && budgets[i].category_id === catId) {
        found = budgets[i];
        break;
      }
    }

    var now = nowJakartaString();
    var threshold = Number(budgetData.warning_threshold_percent || budgetData.threshold) || 80;

    if (found) {
      updateRowById(SHEET_NAMES.ANGGARAN, "budget_id", found.budget_id, {
        budget_amount: amount,
        warning_threshold_percent: threshold,
        updated_at: now
      });
      logActivity("update_budget", "budget", found.budget_id, "Update anggaran " + month + " kategori " + catId + " ke Rp " + amount, "success");
      return { budget_id: found.budget_id, updated: true };
    } else {
      var newId = "BDG-" + Utilities.getUuid().substring(0, 8).toUpperCase();
      var newEntry = {
        budget_id: newId,
        budget_month: month,
        category_id: catId,
        budget_amount: amount,
        warning_threshold_percent: threshold,
        is_active: true,
        notes: budgetData.notes || "",
        created_at: now,
        updated_at: now
      };
      appendRowData(SHEET_NAMES.ANGGARAN, newEntry);
      logActivity("create_budget", "budget", newId, "Membuat anggaran " + month + " kategori " + catId + " sebesar Rp " + amount, "success");
      return { budget_id: newId, created: true };
    }
  });
}
