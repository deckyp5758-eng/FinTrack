/**
 * FinTrack AI — Personal Finance Manager
 * Dashboard.gs — Ringkasan Finansial & Sinkronisasi Ringkasan_Bulanan (A1:M1)
 *
 * Kolom Ringkasan_Bulanan:
 * summary_id | summary_month | opening_balance_total | income_total | expense_total | transfer_in_total | transfer_out_total | closing_balance_total | savings_change | transaction_count | last_calculated_at | reconciliation_status | notes
 */

/**
 * Mengambil ringkasan lengkap data dashboard & menyinkronkan Ringkasan_Bulanan
 * @param {string} month Optional: 'YYYY-MM'
 * @return {Object}
 */
function getDashboardSummary(month) {
  var targetMonth = month || currentMonthJakartaString();
  var accounts = getAllAccountsWithBalances();
  var transactions = readSheetData(SHEET_NAMES.TRANSAKSI);
  var categories = readSheetData(SHEET_NAMES.KATEGORI);

  var categoryMap = {};
  for (var c = 0; c < categories.length; c++) {
    categoryMap[categories[c].category_id] = categories[c];
  }

  // 1. Total Saldo Seluruh Rekening Aktif
  var totalClosingBalance = 0;
  var totalOpeningBalance = 0;
  for (var a = 0; a < accounts.length; a++) {
    if (accounts[a].is_active) {
      totalClosingBalance += accounts[a].calculatedBalance;
      totalOpeningBalance += accounts[a].opening_balance;
    }
  }

  // 2. Arus Kas Bulan Berjalan
  var monthlyIncome = 0;
  var monthlyExpense = 0;
  var monthlyTradingProfit = 0;
  var transferTotal = 0;
  var txCount = 0;
  var expenseByCategory = {};

  for (var i = 0; i < transactions.length; i++) {
    var tx = transactions[i];
    var status = String(tx.status || "").toLowerCase().trim();
    if (status !== "posted") continue;

    var txDate = String(tx.transaction_date || "");
    if (txDate.indexOf(targetMonth) === 0) {
      txCount++;
      var amount = Number(tx.amount) || 0;
      var type = String(tx.transaction_type || "").toLowerCase().trim();
      var catId = tx.category_id || "EXP_OTHER";

      if (type === "income") {
        monthlyIncome += amount;
        if (catId === "INC_TRADING") {
          monthlyTradingProfit += amount;
        }
      } else if (type === "expense") {
        monthlyExpense += amount;
        expenseByCategory[catId] = (expenseByCategory[catId] || 0) + amount;
      } else if (type === "transfer") {
        transferTotal += amount;
      }
    }
  }

  var netCashflow = monthlyIncome - monthlyExpense;

  // Breakdown kategori pengeluaran
  var categoryBreakdown = [];
  for (var catKey in expenseByCategory) {
    var catInfo = categoryMap[catKey] || { category_name: catKey };
    categoryBreakdown.push({
      categoryId: catKey,
      categoryName: catInfo.category_name,
      amount: expenseByCategory[catKey],
      percentage: monthlyExpense > 0 ? Math.round((expenseByCategory[catKey] / monthlyExpense) * 1000) / 10 : 0
    });
  }
  categoryBreakdown.sort(function(x, y) { return y.amount - x.amount; });

  // 3. Sinkronisasi Baris ke Sheet Ringkasan_Bulanan
  syncMonthlySummaryRow(targetMonth, {
    opening_balance_total: totalOpeningBalance,
    income_total: monthlyIncome,
    expense_total: monthlyExpense,
    transfer_in_total: transferTotal,
    transfer_out_total: transferTotal,
    closing_balance_total: totalClosingBalance,
    savings_change: netCashflow,
    transaction_count: txCount
  });

  return {
    database: {
      name: "FinTrack AI — Personal Finance Database",
      spreadsheetId: SPREADSHEET_ID,
      timezone: TIMEZONE,
      locale: "in_ID",
      currency: "IDR",
      status: "CONNECTED",
      lastUpdated: nowJakartaString()
    },
    month: targetMonth,
    financialSummary: {
      totalBalance: totalClosingBalance,
      monthlyIncome: monthlyIncome,
      monthlyExpense: monthlyExpense,
      netCashflow: netCashflow,
      monthlyTradingProfit: monthlyTradingProfit,
      savingRatePercentage: monthlyIncome > 0 ? Math.max(0, Math.round(((monthlyIncome - monthlyExpense) / monthlyIncome) * 1000) / 10) : 0
    },
    accounts: accounts,
    categoryBreakdown: categoryBreakdown,
    budgets: getMonthlyBudgetStatus(targetMonth),
    savingsGoals: getSavingsGoals(),
    recentTransactions: getTransactions({ includeVoid: false }).slice(0, 8)
  };
}

/**
 * Menyinkronkan kalkulasi ke sheet Ringkasan_Bulanan
 */
function syncMonthlySummaryRow(monthStr, metrics) {
  try {
    var rows = readSheetData(SHEET_NAMES.RINGKASAN_BULANAN);
    var found = null;
    for (var i = 0; i < rows.length; i++) {
      if (rows[i].summary_month === monthStr) {
        found = rows[i];
        break;
      }
    }

    var now = nowJakartaString();
    if (found) {
      updateRowById(SHEET_NAMES.RINGKASAN_BULANAN, "summary_id", found.summary_id, {
        opening_balance_total: metrics.opening_balance_total,
        income_total: metrics.income_total,
        expense_total: metrics.expense_total,
        transfer_in_total: metrics.transfer_in_total,
        transfer_out_total: metrics.transfer_out_total,
        closing_balance_total: metrics.closing_balance_total,
        savings_change: metrics.savings_change,
        transaction_count: metrics.transaction_count,
        last_calculated_at: now,
        reconciliation_status: "reconciled"
      });
    } else {
      var summaryId = "SUM-" + monthStr;
      appendRowData(SHEET_NAMES.RINGKASAN_BULANAN, {
        summary_id: summaryId,
        summary_month: monthStr,
        opening_balance_total: metrics.opening_balance_total,
        income_total: metrics.income_total,
        expense_total: metrics.expense_total,
        transfer_in_total: metrics.transfer_in_total,
        transfer_out_total: metrics.transfer_out_total,
        closing_balance_total: metrics.closing_balance_total,
        savings_change: metrics.savings_change,
        transaction_count: metrics.transaction_count,
        last_calculated_at: now,
        reconciliation_status: "reconciled",
        notes: "Dihitung otomatis oleh FinTrack AI"
      });
    }
  } catch (err) {
    Logger.log("Gagal menyinkronkan Ringkasan_Bulanan: " + err.message);
  }
}
