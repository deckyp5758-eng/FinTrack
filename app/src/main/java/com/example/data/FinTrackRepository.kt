package com.example.data

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FinTrackRepository {

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

  var mode: AppEnvironmentMode = AppEnvironmentMode.DEVELOPMENT
  var productionWebAppUrl: String = ""
  var isProductionVerified: Boolean = false
  var lastVerifiedMessage: String = ""

  val SPREADSHEET_ID = "1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk"
  val DATABASE_NAME = "FinTrack AI — Personal Finance Database"

  /**
   * Menguji koneksi nyata ke Google Apps Script Web App
   */
  suspend fun testProductionConnection(targetUrl: String): Result<String> = withContext(Dispatchers.IO) {
    if (targetUrl.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("URL Web App Google Apps Script tidak boleh kosong."))
    }

    try {
      val testUrl = if (targetUrl.contains("?")) "$targetUrl&action=testConnection" else "$targetUrl?action=testConnection"
      val request = Request.Builder()
        .url(testUrl)
        .header("Accept", "application/json")
        .get()
        .build()

      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        isProductionVerified = false
        return@withContext Result.failure(Exception("Gagal menghubungi server: HTTP ${response.code} ${response.message}"))
      }

      val json = JSONObject(bodyStr)
      if (json.optBoolean("success", false)) {
        productionWebAppUrl = targetUrl
        isProductionVerified = true
        lastVerifiedMessage = "Koneksi terverifikasi aktif ke Google Sheets (ID: $SPREADSHEET_ID)"
        return@withContext Result.success(lastVerifiedMessage)
      } else {
        isProductionVerified = false
        val errMsg = json.optString("error", "Respons backend tidak berhasil.")
        return@withContext Result.failure(Exception("Error backend: $errMsg"))
      }
    } catch (e: Exception) {
      isProductionVerified = false
      return@withContext Result.failure(Exception("Gagal terhubung ke Google Sheets: ${e.message}"))
    }
  }

  suspend fun getDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      return@withContext Result.success(MockDataProvider.getDashboardSummary())
    }

    if (!isProductionVerified || productionWebAppUrl.isBlank()) {
      return@withContext Result.failure(IllegalStateException("Mode Produksi memerlukan URL Web App yang sudah terverifikasi."))
    }

    try {
      val url = "$productionWebAppUrl?action=getDashboardSummary"
      val request = Request.Builder().url(url).get().build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        return@withContext Result.failure(Exception("HTTP ${response.code}: Gagal mengambil data produksi."))
      }

      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error", "Kesalahan saat memuat dashboard.")))
      }

      val data = json.getJSONObject("data")
      val summary = parseDashboardJson(data)
      Result.success(summary)
    } catch (e: Exception) {
      Result.failure(Exception("Gagal mengambil data dari Google Sheets: ${e.message}"))
    }
  }

  suspend fun getAccounts(): Result<List<Account>> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      return@withContext Result.success(MockDataProvider.accounts.toList())
    }

    try {
      val url = "$productionWebAppUrl?action=getAccounts"
      val request = Request.Builder().url(url).get().build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""

      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }

      val arr = json.getJSONArray("data")
      val list = mutableListOf<Account>()
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
          Account(
            accountId = o.optString("account_id"),
            accountName = o.optString("account_name"),
            accountType = AccountType.fromWire(o.optString("account_type")),
            openingBalance = o.optLong("opening_balance", 0L),
            openingBalanceDate = o.optString("opening_balance_date", ""),
            currency = o.optString("currency", "IDR"),
            isActive = o.optBoolean("is_active", true),
            notes = o.optString("notes", ""),
            createdAt = o.optString("created_at", ""),
            updatedAt = o.optString("updated_at", ""),
            calculatedBalance = o.optLong("calculatedBalance", 0L)
          )
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getTransactions(filterType: String? = null): Result<List<Transaction>> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      val filtered = if (filterType != null && filterType != "ALL") {
        MockDataProvider.transactions.filter { it.transactionType.wireName.equals(filterType, ignoreCase = true) }
      } else {
        MockDataProvider.transactions
      }
      return@withContext Result.success(filtered.toList())
    }

    try {
      var url = "$productionWebAppUrl?action=getTransactions"
      if (!filterType.isNullOrBlank() && filterType != "ALL") {
        url += "&type=$filterType"
      }
      val request = Request.Builder().url(url).get().build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""

      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }

      val arr = json.getJSONArray("data")
      val list = mutableListOf<Transaction>()
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
          Transaction(
            transactionId = o.optString("transaction_id"),
            transactionDate = o.optString("transaction_date"),
            transactionType = TransactionType.fromWire(o.optString("transaction_type")),
            categoryId = o.optString("category_id").takeIf { it.isNotBlank() },
            sourceAccountId = o.optString("source_account_id").takeIf { it.isNotBlank() },
            destinationAccountId = o.optString("destination_account_id").takeIf { it.isNotBlank() },
            amount = o.optLong("amount"),
            description = o.optString("description"),
            status = TransactionStatus.fromWire(o.optString("status")),
            createdAt = o.optString("created_at"),
            updatedAt = o.optString("updated_at"),
            referenceId = o.optString("reference_id").takeIf { it.isNotBlank() },
            notes = o.optString("notes", "")
          )
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun recordTransaction(
    type: TransactionType,
    amount: Long,
    sourceAccountId: String?,
    destinationAccountId: String?,
    categoryId: String?,
    description: String,
    referenceId: String? = null,
    notes: String = "",
    isTradingRelated: Boolean = false,
    tradingProfitOnly: Boolean = false
  ): Result<String> = withContext(Dispatchers.IO) {
    if (amount <= 0) {
      return@withContext Result.failure(IllegalArgumentException("Nominal harus lebih dari 0."))
    }

    // Aturan Finansial: Penarikan modal trading bukan keuntungan trading!
    if ((categoryId == "INC_TRADING" || isTradingRelated) && type == TransactionType.INCOME) {
      if (!tradingProfitOnly) {
        return@withContext Result.failure(
          IllegalArgumentException("Penarikan modal trading BUKAN keuntungan! Hanya keuntungan terealisasi yang boleh dicatat sebagai pemasukan.")
        )
      }
    }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val finalCat = if (type == TransactionType.TRANSFER) null else categoryId

    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      val isDuplicate = MockDataProvider.transactions.any {
        it.status == TransactionStatus.POSTED && it.amount == amount && it.transactionType == type &&
            it.description.equals(description, ignoreCase = true) &&
            it.transactionDate == todayStr
      }
      if (isDuplicate) {
        return@withContext Result.failure(IllegalStateException("Pencegahan Duplikasi: Transaksi serupa sudah tersimpan hari ini."))
      }

      val newId = "TX-DEV-${System.currentTimeMillis() % 100000}"
      val newTx = Transaction(
        transactionId = newId,
        transactionDate = todayStr,
        transactionType = type,
        categoryId = finalCat,
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        amount = amount,
        description = description,
        status = TransactionStatus.POSTED,
        createdAt = nowStr,
        updatedAt = nowStr,
        referenceId = referenceId,
        notes = notes
      )
      MockDataProvider.transactions.add(0, newTx)

      // Update mock account balances
      if (type == TransactionType.INCOME && destinationAccountId != null) {
        val acc = MockDataProvider.accounts.find { it.accountId == destinationAccountId }
        if (acc != null) {
          val idx = MockDataProvider.accounts.indexOf(acc)
          MockDataProvider.accounts[idx] = acc.copy(calculatedBalance = acc.calculatedBalance + amount)
        }
      } else if (type == TransactionType.EXPENSE && sourceAccountId != null) {
        val acc = MockDataProvider.accounts.find { it.accountId == sourceAccountId }
        if (acc != null) {
          val idx = MockDataProvider.accounts.indexOf(acc)
          MockDataProvider.accounts[idx] = acc.copy(calculatedBalance = acc.calculatedBalance - amount)
        }
      } else if (type == TransactionType.TRANSFER && sourceAccountId != null && destinationAccountId != null) {
        val src = MockDataProvider.accounts.find { it.accountId == sourceAccountId }
        val dst = MockDataProvider.accounts.find { it.accountId == destinationAccountId }
        if (src != null) {
          val sIdx = MockDataProvider.accounts.indexOf(src)
          MockDataProvider.accounts[sIdx] = src.copy(calculatedBalance = src.calculatedBalance - amount)
        }
        if (dst != null) {
          val dIdx = MockDataProvider.accounts.indexOf(dst)
          MockDataProvider.accounts[dIdx] = dst.copy(calculatedBalance = dst.calculatedBalance + amount)
        }
      }

      MockDataProvider.auditLogs.add(0, AuditLogEntry(
        logId = "LOG-${System.currentTimeMillis() % 10000}",
        eventTimestamp = nowStr,
        actor = "kingdomedantech@gmail.com",
        action = "create_transaction",
        entityType = "transaction",
        entityId = newId,
        changeSummary = "${type.wireName} Rp $amount: $description",
        requestId = referenceId ?: "REQ-LOCAL",
        result = "success"
      ))

      return@withContext Result.success(newId)
    }

    // PRODUCTION MODE: Kirim sesuai kontrak Apps Script
    try {
      val payload = JSONObject().apply {
        put("action", "recordTransaction")
        put("data", JSONObject().apply {
          put("isUserConfirmed", true)
          put("transaction_type", type.wireName)
          put("amount", amount)
          put("source_account_id", sourceAccountId ?: "")
          put("destination_account_id", destinationAccountId ?: "")
          put("category_id", finalCat ?: "")
          put("description", description)
          put("reference_id", referenceId ?: "")
          put("notes", notes)
          put("transaction_date", todayStr)
          put("isTradingRelated", isTradingRelated)
          put("tradingProfitOnly", tradingProfitOnly)
        })
      }

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = payload.toString().toRequestBody(mediaType)
      val request = Request.Builder().url(productionWebAppUrl).post(body).build()

      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""

      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error", "Gagal menyimpan ke Google Sheets.")))
      }

      val txObj = json.getJSONObject("data").getJSONObject("transaction")
      Result.success(txObj.optString("transaction_id"))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun voidTransaction(transactionId: String, reason: String): Result<String> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      val tx = MockDataProvider.transactions.find { it.transactionId == transactionId }
        ?: return@withContext Result.failure(Exception("Transaksi tidak ditemukan."))
      val idx = MockDataProvider.transactions.indexOf(tx)
      MockDataProvider.transactions[idx] = tx.copy(status = TransactionStatus.VOID)

      // Revert balances
      if (tx.transactionType == TransactionType.INCOME && tx.destinationAccountId != null) {
        val acc = MockDataProvider.accounts.find { it.accountId == tx.destinationAccountId }
        if (acc != null) {
          val aIdx = MockDataProvider.accounts.indexOf(acc)
          MockDataProvider.accounts[aIdx] = acc.copy(calculatedBalance = acc.calculatedBalance - tx.amount)
        }
      } else if (tx.transactionType == TransactionType.EXPENSE && tx.sourceAccountId != null) {
        val acc = MockDataProvider.accounts.find { it.accountId == tx.sourceAccountId }
        if (acc != null) {
          val aIdx = MockDataProvider.accounts.indexOf(acc)
          MockDataProvider.accounts[aIdx] = acc.copy(calculatedBalance = acc.calculatedBalance + tx.amount)
        }
      } else if (tx.transactionType == TransactionType.TRANSFER && tx.sourceAccountId != null && tx.destinationAccountId != null) {
        val src = MockDataProvider.accounts.find { it.accountId == tx.sourceAccountId }
        val dst = MockDataProvider.accounts.find { it.accountId == tx.destinationAccountId }
        if (src != null) {
          val sIdx = MockDataProvider.accounts.indexOf(src)
          MockDataProvider.accounts[sIdx] = src.copy(calculatedBalance = src.calculatedBalance + tx.amount)
        }
        if (dst != null) {
          val dIdx = MockDataProvider.accounts.indexOf(dst)
          MockDataProvider.accounts[dIdx] = dst.copy(calculatedBalance = dst.calculatedBalance - tx.amount)
        }
      }

      val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
      MockDataProvider.auditLogs.add(0, AuditLogEntry(
        logId = "LOG-${System.currentTimeMillis() % 10000}",
        eventTimestamp = nowStr,
        actor = "kingdomedantech@gmail.com",
        action = "void_transaction",
        entityType = "transaction",
        entityId = transactionId,
        changeSummary = "Void transaksi $transactionId (Alasan: $reason)",
        result = "void"
      ))
      return@withContext Result.success("Transaksi berhasil dibatalkan (void).")
    }

    try {
      val payload = JSONObject().apply {
        put("action", "voidTransaction")
        put("transactionId", transactionId)
        put("reason", reason)
      }
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = payload.toString().toRequestBody(mediaType)
      val request = Request.Builder().url(productionWebAppUrl).post(body).build()

      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""

      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }
      Result.success(json.getJSONObject("data").optString("message", "Transaksi void sukses."))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun createAccount(name: String, type: AccountType, openingBalance: Long, notes: String): Result<Account> = withContext(Dispatchers.IO) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      val newId = "ACC-DEV-${System.currentTimeMillis() % 1000}"
      val newAcc = Account(
        accountId = newId,
        accountName = name,
        accountType = type,
        openingBalance = openingBalance,
        openingBalanceDate = todayStr,
        currency = "IDR",
        isActive = true,
        notes = notes,
        createdAt = nowStr,
        updatedAt = nowStr,
        calculatedBalance = openingBalance
      )
      MockDataProvider.accounts.add(newAcc)
      return@withContext Result.success(newAcc)
    }

    try {
      val payload = JSONObject().apply {
        put("action", "createAccount")
        put("data", JSONObject().apply {
          put("account_name", name)
          put("account_type", type.wireName)
          put("opening_balance", openingBalance)
          put("opening_balance_date", todayStr)
          put("notes", notes)
        })
      }
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = payload.toString().toRequestBody(mediaType)
      val request = Request.Builder().url(productionWebAppUrl).post(body).build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""
      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }
      val d = json.getJSONObject("data")
      Result.success(Account(
        accountId = d.optString("account_id"),
        accountName = d.optString("account_name"),
        accountType = type,
        openingBalance = openingBalance,
        openingBalanceDate = todayStr,
        currency = "IDR",
        isActive = true,
        notes = notes,
        calculatedBalance = openingBalance
      ))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun reconcileAccount(accountId: String, actualPhysicalBalance: Long, reason: String): Result<String> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      val acc = MockDataProvider.accounts.find { it.accountId == accountId }
        ?: return@withContext Result.failure(Exception("Rekening tidak ditemukan."))
      val diff = actualPhysicalBalance - acc.calculatedBalance
      val idx = MockDataProvider.accounts.indexOf(acc)
      MockDataProvider.accounts[idx] = acc.copy(calculatedBalance = actualPhysicalBalance)

      val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
      MockDataProvider.auditLogs.add(0, AuditLogEntry(
        logId = "LOG-${System.currentTimeMillis() % 10000}",
        eventTimestamp = nowStr,
        actor = "kingdomedantech@gmail.com",
        action = "reconcile_account",
        entityType = "account",
        entityId = accountId,
        changeSummary = "Rekonsiliasi fisik ke Rp $actualPhysicalBalance (Selisih: Rp $diff, Ket: $reason)",
        result = "success"
      ))
      return@withContext Result.success("Rekonsiliasi selesai. Selisih: Rp $diff")
    }

    try {
      val payload = JSONObject().apply {
        put("action", "reconcileAccount")
        put("accountId", accountId)
        put("actualBalance", actualPhysicalBalance)
        put("reason", reason)
      }
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = payload.toString().toRequestBody(mediaType)
      val request = Request.Builder().url(productionWebAppUrl).post(body).build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""
      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }
      Result.success("Rekonsiliasi berhasil disimpan di Google Sheets.")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun addSavingsContribution(targetId: String, amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      val g = MockDataProvider.savingsGoals.find { it.targetId == targetId }
        ?: return@withContext Result.failure(Exception("Target tidak ditemukan."))
      val idx = MockDataProvider.savingsGoals.indexOf(g)
      val newCur = g.currentAmount + amount
      val newRem = (g.targetAmount - newCur).coerceAtLeast(0)
      val newPct = ((newCur.toFloat() / g.targetAmount.toFloat()) * 100f).coerceAtMost(100f)
      MockDataProvider.savingsGoals[idx] = g.copy(
        currentAmount = newCur,
        remainingAmount = newRem,
        progressPercentage = newPct
      )
      return@withContext Result.success(Unit)
    }

    try {
      val payload = JSONObject().apply {
        put("action", "addSavingsContribution")
        put("targetId", targetId)
        put("amount", amount)
      }
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = payload.toString().toRequestBody(mediaType)
      val request = Request.Builder().url(productionWebAppUrl).post(body).build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""
      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getAuditLogs(): Result<List<AuditLogEntry>> = withContext(Dispatchers.IO) {
    if (mode == AppEnvironmentMode.DEVELOPMENT) {
      return@withContext Result.success(MockDataProvider.auditLogs.toList())
    }

    try {
      val url = "$productionWebAppUrl?action=getAuditLogs&limit=40"
      val request = Request.Builder().url(url).get().build()
      val response = okHttpClient.newCall(request).execute()
      val bodyStr = response.body?.string() ?: ""
      val json = JSONObject(bodyStr)
      if (!json.optBoolean("success", false)) {
        return@withContext Result.failure(Exception(json.optString("error")))
      }
      val arr = json.getJSONArray("data")
      val list = mutableListOf<AuditLogEntry>()
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
          AuditLogEntry(
            logId = o.optString("log_id"),
            eventTimestamp = o.optString("event_timestamp"),
            actor = o.optString("actor"),
            action = o.optString("action"),
            entityType = o.optString("entity_type"),
            entityId = o.optString("entity_id"),
            changeSummary = o.optString("change_summary"),
            requestId = o.optString("request_id"),
            result = o.optString("result", "success"),
            notes = o.optString("notes", "")
          )
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun parseDashboardJson(data: JSONObject): DashboardSummary {
    val dbObj = data.getJSONObject("database")
    val meta = DatabaseMetadata(
      name = dbObj.optString("name", DATABASE_NAME),
      spreadsheetId = dbObj.optString("spreadsheetId", SPREADSHEET_ID),
      timezone = dbObj.optString("timezone", "Asia/Jakarta"),
      locale = dbObj.optString("locale", "in_ID"),
      currency = dbObj.optString("currency", "IDR"),
      status = dbObj.optString("status", "CONNECTED"),
      lastUpdated = dbObj.optString("lastUpdated", "")
    )

    val finObj = data.getJSONObject("financialSummary")
    val fin = FinancialSummary(
      totalBalance = finObj.optLong("totalBalance"),
      monthlyIncome = finObj.optLong("monthlyIncome"),
      monthlyExpense = finObj.optLong("monthlyExpense"),
      netCashflow = finObj.optLong("netCashflow"),
      monthlyTradingProfit = finObj.optLong("monthlyTradingProfit"),
      savingRatePercentage = finObj.optDouble("savingRatePercentage", 0.0).toFloat()
    )

    val accountsArr = data.optJSONArray("accounts") ?: JSONArray()
    val accounts = mutableListOf<Account>()
    for (i in 0 until accountsArr.length()) {
      val o = accountsArr.getJSONObject(i)
      accounts.add(
        Account(
          accountId = o.optString("account_id"),
          accountName = o.optString("account_name"),
          accountType = AccountType.fromWire(o.optString("account_type")),
          openingBalance = o.optLong("opening_balance", 0L),
          openingBalanceDate = o.optString("opening_balance_date", ""),
          currency = o.optString("currency", "IDR"),
          isActive = o.optBoolean("is_active", true),
          notes = o.optString("notes", ""),
          createdAt = o.optString("created_at", ""),
          updatedAt = o.optString("updated_at", ""),
          calculatedBalance = o.optLong("calculatedBalance", 0L)
        )
      )
    }

    val catArr = data.optJSONArray("categoryBreakdown") ?: JSONArray()
    val breakdown = mutableListOf<CategoryBreakdownItem>()
    for (i in 0 until catArr.length()) {
      val o = catArr.getJSONObject(i)
      breakdown.add(
        CategoryBreakdownItem(
          categoryId = o.optString("categoryId"),
          categoryName = o.optString("categoryName"),
          amount = o.optLong("amount"),
          percentage = o.optDouble("percentage", 0.0).toFloat()
        )
      )
    }

    val budArr = data.optJSONArray("budgets") ?: JSONArray()
    val budgets = mutableListOf<BudgetStatus>()
    for (i in 0 until budArr.length()) {
      val o = budArr.getJSONObject(i)
      budgets.add(
        BudgetStatus(
          budgetId = o.optString("budget_id"),
          budgetMonth = o.optString("budget_month"),
          categoryId = o.optString("category_id"),
          categoryName = o.optString("category_name"),
          budgetAmount = o.optLong("budget_amount"),
          spentAmount = o.optLong("spent_amount"),
          remainingAmount = o.optLong("remaining_amount"),
          percentage = o.optDouble("percentage", 0.0).toFloat(),
          warningThresholdPercent = o.optInt("warning_threshold_percent", 80),
          status = o.optString("status", "NORMAL")
        )
      )
    }

    val savArr = data.optJSONArray("savingsGoals") ?: JSONArray()
    val savings = mutableListOf<SavingsGoal>()
    for (i in 0 until savArr.length()) {
      val o = savArr.getJSONObject(i)
      savings.add(
        SavingsGoal(
          targetId = o.optString("target_id"),
          targetName = o.optString("target_name"),
          targetAmount = o.optLong("target_amount"),
          currentAmount = o.optLong("current_amount"),
          remainingAmount = o.optLong("remaining_amount"),
          progressPercentage = o.optDouble("progressPercentage", 0.0).toFloat(),
          targetDate = o.optString("target_date", "-"),
          linkedAccountId = o.optString("linked_account_id"),
          priority = o.optInt("priority", 1),
          isActive = o.optBoolean("is_active", true),
          notes = o.optString("notes", "")
        )
      )
    }

    val txArr = data.optJSONArray("recentTransactions") ?: JSONArray()
    val txs = mutableListOf<Transaction>()
    for (i in 0 until txArr.length()) {
      val o = txArr.getJSONObject(i)
      txs.add(
        Transaction(
          transactionId = o.optString("transaction_id"),
          transactionDate = o.optString("transaction_date"),
          transactionType = TransactionType.fromWire(o.optString("transaction_type")),
          categoryId = o.optString("category_id").takeIf { it.isNotBlank() },
          sourceAccountId = o.optString("source_account_id").takeIf { it.isNotBlank() },
          destinationAccountId = o.optString("destination_account_id").takeIf { it.isNotBlank() },
          amount = o.optLong("amount"),
          description = o.optString("description"),
          status = TransactionStatus.fromWire(o.optString("status")),
          createdAt = o.optString("created_at"),
          updatedAt = o.optString("updated_at"),
          referenceId = o.optString("reference_id").takeIf { it.isNotBlank() },
          notes = o.optString("notes", "")
        )
      )
    }

    return DashboardSummary(
      database = meta,
      month = data.optString("month", "2026-10"),
      financialSummary = fin,
      accounts = accounts,
      categoryBreakdown = breakdown,
      budgets = budgets,
      savingsGoals = savings,
      recentTransactions = txs
    )
  }
}
