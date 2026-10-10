/**
 * FinTrack AI — Personal Finance Manager
 * Code.gs — Main Entry Point Google Apps Script Web App
 *
 * Mengatur routing API GET & POST untuk integrasi frontend.
 * Mengembalikan respons berformat JSON murni dengan CORS.
 */

function doGet(e) {
  return handleRequest(e, "GET");
}

function doPost(e) {
  return handleRequest(e, "POST");
}

/**
 * Dispatcher utama request
 */
function handleRequest(e, method) {
  try {
    var params = {};
    if (e && e.parameter) {
      params = e.parameter;
    }

    var postData = {};
    if (e && e.postData && e.postData.contents) {
      try {
        postData = JSON.parse(e.postData.contents);
      } catch (parseErr) {
        postData = {};
      }
    }

    var action = params.action || postData.action || "testConnection";
    var result = {};

    switch (action) {
      case "testConnection":
        result = {
          status: "SUCCESS",
          message: "Koneksi ke FinTrack AI Database berhasil terverifikasi.",
          spreadsheetId: SPREADSHEET_ID,
          serverTime: nowJakartaString(),
          timezone: TIMEZONE,
          locale: "in_ID",
          currency: "IDR"
        };
        break;

      case "getDashboardSummary":
        var month = params.month || postData.month || currentMonthJakartaString();
        result = getDashboardSummary(month);
        break;

      case "getAccounts":
        result = getAllAccountsWithBalances();
        break;

      case "getTransactions":
        var filter = {
          startDate: params.startDate || postData.startDate,
          endDate: params.endDate || postData.endDate,
          type: params.type || postData.type,
          accountId: params.accountId || postData.accountId,
          categoryId: params.categoryId || postData.categoryId,
          includeVoid: params.includeVoid === "true" || postData.includeVoid === true
        };
        result = getTransactions(filter);
        break;

      case "getCategories":
        result = readSheetData(SHEET_NAMES.KATEGORI);
        break;

      case "getBudgets":
        var bMonth = params.month || postData.month || currentMonthJakartaString();
        result = getMonthlyBudgetStatus(bMonth);
        break;

      case "getSavingsGoals":
        result = getSavingsGoals();
        break;

      case "getAuditLogs":
        var limit = Number(params.limit || postData.limit) || 40;
        result = getRecentAuditLogs(limit);
        break;

      case "recordTransaction":
        result = recordTransaction(postData.data || postData);
        break;

      case "voidTransaction":
        var txId = postData.transactionId || params.transactionId;
        var reason = postData.reason || params.reason;
        result = voidTransaction(txId, reason);
        break;

      case "createAccount":
        result = createAccount(postData.data || postData);
        break;

      case "reconcileAccount":
        var accId = postData.accountId || params.accountId;
        var actualBalance = Number(postData.actualBalance !== undefined ? postData.actualBalance : params.actualBalance);
        var rReason = postData.reason || params.reason;
        result = reconcileAccountBalance(accId, actualBalance, rReason);
        break;

      case "upsertBudget":
        result = upsertBudget(postData.data || postData);
        break;

      case "createSavingsGoal":
        result = createSavingsGoal(postData.data || postData);
        break;

      case "addSavingsContribution":
        var gId = postData.targetId || postData.goalId || params.goalId;
        var cAmount = Number(postData.amount || params.amount);
        result = addSavingsContribution(gId, cAmount);
        break;

      default:
        throw new Error("Aksi tidak dikenali: '" + action + "'");
    }

    return createJsonResponse({
      success: true,
      action: action,
      timestamp: nowJakartaString(),
      data: result
    });

  } catch (error) {
    return createJsonResponse({
      success: false,
      error: error.message || "Terjadi kesalahan internal pada backend Apps Script."
    });
  }
}

/**
 * Membentuk output HTTP ContentService berformat JSON
 */
function createJsonResponse(data) {
  var output = ContentService.createTextOutput(JSON.stringify(data));
  output.setMimeType(ContentService.MimeType.JSON);
  return output;
}
