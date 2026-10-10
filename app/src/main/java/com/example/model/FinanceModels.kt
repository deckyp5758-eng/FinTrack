package com.example.model

enum class TransactionType(val wireName: String) {
  INCOME("income"),
  EXPENSE("expense"),
  TRANSFER("transfer");

  companion object {
    fun fromWire(wire: String): TransactionType {
      return when (wire.lowercase().trim()) {
        "income" -> INCOME
        "expense" -> EXPENSE
        "transfer" -> TRANSFER
        else -> EXPENSE
      }
    }
  }
}

enum class TransactionStatus(val wireName: String) {
  POSTED("posted"),
  VOID("void");

  companion object {
    fun fromWire(wire: String): TransactionStatus {
      return when (wire.lowercase().trim()) {
        "posted" -> POSTED
        "void" -> VOID
        else -> POSTED
      }
    }
  }
}

enum class AccountType(val wireName: String, val displayName: String) {
  BANK("bank", "Bank"),
  CASH("cash", "Uang Tunai"),
  TRADING("trading", "Trading & Investasi"),
  OTHER("other", "Lainnya");

  companion object {
    fun fromWire(wire: String): AccountType {
      return when (wire.lowercase().trim()) {
        "bank" -> BANK
        "cash" -> CASH
        "trading" -> TRADING
        "other" -> OTHER
        else -> BANK
      }
    }
  }
}

data class Account(
  val accountId: String,
  val accountName: String,
  val accountType: AccountType,
  val openingBalance: Long = 0L,
  val openingBalanceDate: String = "",
  val currency: String = "IDR",
  val isActive: Boolean = true,
  val notes: String = "",
  val createdAt: String = "",
  val updatedAt: String = "",
  val calculatedBalance: Long = 0L // Single source of truth dari backend
)

data class Category(
  val categoryId: String,
  val categoryName: String,
  val transactionType: TransactionType,
  val monthlyBudget: Long? = null,
  val isActive: Boolean = true,
  val sortOrder: Int = 0,
  val notes: String = ""
)

data class Transaction(
  val transactionId: String,
  val transactionDate: String, // YYYY-MM-DD
  val transactionType: TransactionType,
  val categoryId: String? = null,
  val sourceAccountId: String? = null,
  val destinationAccountId: String? = null,
  val amount: Long,
  val description: String,
  val status: TransactionStatus = TransactionStatus.POSTED,
  val createdAt: String = "",
  val updatedAt: String = "",
  val referenceId: String? = null,
  val notes: String = ""
)

data class BudgetStatus(
  val budgetId: String,
  val budgetMonth: String, // YYYY-MM
  val categoryId: String,
  val categoryName: String,
  val budgetAmount: Long,
  val spentAmount: Long,
  val remainingAmount: Long,
  val percentage: Float,
  val warningThresholdPercent: Int = 80,
  val status: String // NORMAL, WARNING, EXCEEDED
)

data class SavingsGoal(
  val targetId: String,
  val targetName: String,
  val targetAmount: Long,
  val currentAmount: Long,
  val remainingAmount: Long,
  val progressPercentage: Float,
  val targetDate: String,
  val linkedAccountId: String? = null,
  val priority: Int = 1,
  val isActive: Boolean = true,
  val notes: String = ""
)

data class AuditLogEntry(
  val logId: String,
  val eventTimestamp: String,
  val actor: String,
  val action: String,
  val entityType: String,
  val entityId: String,
  val changeSummary: String,
  val requestId: String = "",
  val result: String = "success",
  val notes: String = ""
)

data class DatabaseMetadata(
  val name: String = "FinTrack AI — Personal Finance Database",
  val spreadsheetId: String = "1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk",
  val timezone: String = "Asia/Jakarta",
  val locale: String = "in_ID",
  val currency: String = "IDR",
  val status: String = "CONNECTED",
  val lastUpdated: String = ""
)

data class FinancialSummary(
  val totalBalance: Long = 0L,
  val monthlyIncome: Long = 0L,
  val monthlyExpense: Long = 0L,
  val netCashflow: Long = 0L,
  val monthlyTradingProfit: Long = 0L,
  val savingRatePercentage: Float = 0f
)

data class CategoryBreakdownItem(
  val categoryId: String,
  val categoryName: String,
  val amount: Long,
  val percentage: Float
)

data class DashboardSummary(
  val database: DatabaseMetadata,
  val month: String,
  val financialSummary: FinancialSummary,
  val accounts: List<Account>,
  val categoryBreakdown: List<CategoryBreakdownItem>,
  val budgets: List<BudgetStatus>,
  val savingsGoals: List<SavingsGoal>,
  val recentTransactions: List<Transaction>
)

enum class AppEnvironmentMode {
  DEVELOPMENT,
  PRODUCTION
}
