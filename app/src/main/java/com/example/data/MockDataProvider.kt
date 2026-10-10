package com.example.data

import com.example.model.*

object MockDataProvider {

  // 12 Kategori Persis Sesuai Audit Google Sheets (Sheet: Kategori A1:I1)
  val categories = listOf(
    // 8 Expense Categories
    Category("EXP_FOOD", "Makan, kopi, rokok", TransactionType.EXPENSE, monthlyBudget = 900000L),
    Category("EXP_TRANSPORT", "Transportasi", TransactionType.EXPENSE, monthlyBudget = 450000L),
    Category("EXP_BILLS", "Tagihan", TransactionType.EXPENSE, monthlyBudget = 800000L),
    Category("EXP_SHOPPING", "Belanja", TransactionType.EXPENSE, monthlyBudget = 600000L),
    Category("EXP_HEALTH", "Kesehatan", TransactionType.EXPENSE, monthlyBudget = 300000L),
    Category("EXP_PERSONAL", "Kebutuhan pribadi", TransactionType.EXPENSE, monthlyBudget = 350000L),
    Category("EXP_ENTERTAINMENT", "Hiburan", TransactionType.EXPENSE, monthlyBudget = 250000L),
    Category("EXP_OTHER", "Lainnya", TransactionType.EXPENSE, monthlyBudget = 200000L),

    // 4 Income Categories
    Category("INC_SALARY", "Gaji", TransactionType.INCOME),
    Category("INC_TRADING", "Keuntungan trading terealisasi", TransactionType.INCOME),
    Category("INC_BUSINESS", "Pendapatan usaha", TransactionType.INCOME),
    Category("INC_OTHER", "Pemasukan lainnya", TransactionType.INCOME)
  )

  val accounts = mutableListOf(
    Account(
      accountId = "ACC_BCA_01",
      accountName = "BCA Rekening Utama",
      accountType = AccountType.BANK,
      openingBalance = 15000000L,
      openingBalanceDate = "2026-10-01",
      currency = "IDR",
      isActive = true,
      notes = "Operasional Harian & Gaji",
      createdAt = "2026-10-01 08:00:00",
      updatedAt = "2026-10-09 20:00:00",
      calculatedBalance = 24350000L
    ),
    Account(
      accountId = "ACC_TRD_02",
      accountName = "Rekening Trading Saham & Crypto",
      accountType = AccountType.TRADING,
      openingBalance = 50000000L,
      openingBalanceDate = "2026-10-01",
      currency = "IDR",
      isActive = true,
      notes = "Modal Trading Terpisah (Penarikan modal bukan profit)",
      createdAt = "2026-10-01 08:00:00",
      updatedAt = "2026-10-09 20:00:00",
      calculatedBalance = 53850000L
    ),
    Account(
      accountId = "ACC_CSH_03",
      accountName = "Kas Tunai Dompet",
      accountType = AccountType.CASH,
      openingBalance = 1500000L,
      openingBalanceDate = "2026-10-01",
      currency = "IDR",
      isActive = true,
      notes = "Uang Tunai Fisik di Dompet",
      createdAt = "2026-10-01 08:00:00",
      updatedAt = "2026-10-09 20:00:00",
      calculatedBalance = 1050000L
    )
  )

  val transactions = mutableListOf(
    Transaction(
      transactionId = "TX-202610-001",
      transactionDate = "2026-10-05",
      transactionType = TransactionType.INCOME,
      categoryId = "INC_SALARY",
      sourceAccountId = null,
      destinationAccountId = "ACC_BCA_01",
      amount = 12000000L,
      description = "Gaji Bulanan Oktober 2026",
      status = TransactionStatus.POSTED,
      createdAt = "2026-10-05 09:00:00",
      updatedAt = "2026-10-05 09:00:00",
      referenceId = "PAYROLL-OCT26"
    ),
    Transaction(
      transactionId = "TX-202610-002",
      transactionDate = "2026-10-06",
      transactionType = TransactionType.EXPENSE,
      categoryId = "EXP_FOOD",
      sourceAccountId = "ACC_CSH_03",
      destinationAccountId = null,
      amount = 450000L,
      description = "Makan siang & belanja kopi mingguan",
      status = TransactionStatus.POSTED,
      createdAt = "2026-10-06 13:00:00",
      updatedAt = "2026-10-06 13:00:00"
    ),
    Transaction(
      transactionId = "TX-202610-003",
      transactionDate = "2026-10-08",
      transactionType = TransactionType.INCOME,
      categoryId = "INC_TRADING",
      sourceAccountId = null,
      destinationAccountId = "ACC_BCA_01",
      amount = 3850000L,
      description = "Realisasi profit swing trade BBCA (Bersih diterima)",
      status = TransactionStatus.POSTED,
      createdAt = "2026-10-08 14:30:00",
      updatedAt = "2026-10-08 14:30:00",
      notes = "Keuntungan terealisasi sah"
    ),
    Transaction(
      transactionId = "TX-202610-004",
      transactionDate = "2026-10-08",
      transactionType = TransactionType.TRANSFER,
      categoryId = null, // Transfer category_id harus kosong
      sourceAccountId = "ACC_BCA_01",
      destinationAccountId = "ACC_CSH_03",
      amount = 500000L,
      description = "Tarik tunai ATM untuk kas dompet",
      status = TransactionStatus.POSTED,
      createdAt = "2026-10-08 15:00:00",
      updatedAt = "2026-10-08 15:00:00"
    )
  )

  val budgets = mutableListOf(
    BudgetStatus(
      budgetId = "BDG-01",
      budgetMonth = "2026-10",
      categoryId = "EXP_FOOD",
      categoryName = "Makan, kopi, rokok",
      budgetAmount = 900000L,
      spentAmount = 450000L,
      remainingAmount = 450000L,
      percentage = 50.0f,
      warningThresholdPercent = 80,
      status = "NORMAL"
    ),
    BudgetStatus(
      budgetId = "BDG-02",
      budgetMonth = "2026-10",
      categoryId = "EXP_BILLS",
      categoryName = "Tagihan",
      budgetAmount = 800000L,
      spentAmount = 720000L,
      remainingAmount = 80000L,
      percentage = 90.0f,
      warningThresholdPercent = 80,
      status = "WARNING"
    )
  )

  val savingsGoals = mutableListOf(
    SavingsGoal(
      targetId = "GOAL-01",
      targetName = "Dana Darurat 6 Bulan",
      targetAmount = 60000000L,
      currentAmount = 35000000L,
      remainingAmount = 25000000L,
      progressPercentage = 58.3f,
      targetDate = "2027-06-30",
      linkedAccountId = "ACC_BCA_01",
      priority = 1,
      isActive = true,
      notes = "Disimpan di instrumen likuid"
    )
  )

  val auditLogs = mutableListOf(
    AuditLogEntry(
      logId = "LOG-001",
      eventTimestamp = "2026-10-08 14:30:00",
      actor = "kingdomedantech@gmail.com",
      action = "create_transaction",
      entityType = "transaction",
      entityId = "TX-202610-003",
      changeSummary = "Realisasi profit trading Rp 3.850.000 ke BCA",
      requestId = "REQ-10821",
      result = "success"
    ),
    AuditLogEntry(
      logId = "LOG-002",
      eventTimestamp = "2026-10-08 15:00:00",
      actor = "kingdomedantech@gmail.com",
      action = "create_transaction",
      entityType = "transaction",
      entityId = "TX-202610-004",
      changeSummary = "Transfer Rp 500.000 dari BCA ke Dompet",
      requestId = "REQ-10822",
      result = "success"
    )
  )

  fun getDashboardSummary(): DashboardSummary {
    val totalBal = accounts.filter { it.isActive }.sumOf { it.calculatedBalance }
    val monthlyInc = 15850000L
    val monthlyExp = 1170000L
    val netCash = monthlyInc - monthlyExp

    return DashboardSummary(
      database = DatabaseMetadata(
        status = "DEVELOPMENT_MOCK",
        lastUpdated = "2026-10-10 00:00:00"
      ),
      month = "2026-10",
      financialSummary = FinancialSummary(
        totalBalance = totalBal,
        monthlyIncome = monthlyInc,
        monthlyExpense = monthlyExp,
        netCashflow = netCash,
        monthlyTradingProfit = 3850000L,
        savingRatePercentage = 92.6f
      ),
      accounts = accounts.toList(),
      categoryBreakdown = listOf(
        CategoryBreakdownItem("EXP_FOOD", "Makan, kopi, rokok", 450000L, 38.5f),
        CategoryBreakdownItem("EXP_BILLS", "Tagihan", 720000L, 61.5f)
      ),
      budgets = budgets.toList(),
      savingsGoals = savingsGoals.toList(),
      recentTransactions = transactions.toList()
    )
  }
}
