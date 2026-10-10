package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FinTrackRepository
import com.example.data.MockDataProvider
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

sealed interface UiState {
  object Loading : UiState
  data class Success(val summary: DashboardSummary) : UiState
  data class Error(val message: String) : UiState
}

data class TransactionDraft(
  val type: TransactionType = TransactionType.EXPENSE,
  val amount: Long = 0L,
  val sourceAccountId: String? = null,
  val destinationAccountId: String? = null,
  val categoryId: String? = "EXP_FOOD",
  val description: String = "",
  val referenceId: String? = null,
  val notes: String = "",
  val isTradingRelated: Boolean = false,
  val tradingProfitOnly: Boolean = false
)

class FinTrackViewModel(
  val repository: FinTrackRepository = FinTrackRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _transactionFilter = MutableStateFlow("ALL")
  val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

  private val _transactionSearch = MutableStateFlow("")
  val transactionSearch: StateFlow<String> = _transactionSearch.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
  val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

  // Dialog & Action States
  private val _showAddTxDialog = MutableStateFlow(false)
  val showAddTxDialog: StateFlow<Boolean> = _showAddTxDialog.asStateFlow()

  private val _pendingTxDraft = MutableStateFlow<TransactionDraft?>(null)
  val pendingTxDraft: StateFlow<TransactionDraft?> = _pendingTxDraft.asStateFlow()

  private val _showVoidDialogFor = MutableStateFlow<Transaction?>(null)
  val showVoidDialogFor: StateFlow<Transaction?> = _showVoidDialogFor.asStateFlow()

  private val _showReconcileDialogFor = MutableStateFlow<Account?>(null)
  val showReconcileDialogFor: StateFlow<Account?> = _showReconcileDialogFor.asStateFlow()

  private val _showAddAccountDialog = MutableStateFlow(false)
  val showAddAccountDialog: StateFlow<Boolean> = _showAddAccountDialog.asStateFlow()

  private val _showContributeGoalFor = MutableStateFlow<SavingsGoal?>(null)
  val showContributeGoalFor: StateFlow<SavingsGoal?> = _showContributeGoalFor.asStateFlow()

  private val _connectionStatusMessage = MutableStateFlow<String?>(null)
  val connectionStatusMessage: StateFlow<String?> = _connectionStatusMessage.asStateFlow()

  private val _isConnecting = MutableStateFlow(false)
  val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

  init {
    loadData()
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
    if (index == 4) {
      loadAuditLogs()
    }
  }

  fun setFilter(filter: String) {
    _transactionFilter.value = filter
  }

  fun setSearchQuery(query: String) {
    _transactionSearch.value = query
  }

  fun loadData() {
    viewModelScope.launch {
      _uiState.value = UiState.Loading
      val result = repository.getDashboardSummary()
      result.onSuccess { summary ->
        _uiState.value = UiState.Success(summary)
      }.onFailure { err ->
        _uiState.value = UiState.Error(err.message ?: "Gagal memuat data keuangan.")
      }
    }
  }

  fun loadAuditLogs() {
    viewModelScope.launch {
      val res = repository.getAuditLogs()
      res.onSuccess {
        _auditLogs.value = it
      }
    }
  }

  fun openAddTransactionDialog(defaultType: TransactionType = TransactionType.EXPENSE) {
    _pendingTxDraft.value = TransactionDraft(
      type = defaultType,
      categoryId = if (defaultType == TransactionType.INCOME) "INC_SALARY" else if (defaultType == TransactionType.EXPENSE) "EXP_FOOD" else null
    )
    _showAddTxDialog.value = true
  }

  fun closeAddTransactionDialog() {
    _showAddTxDialog.value = false
    _pendingTxDraft.value = null
  }

  fun confirmAndSaveTransaction(draft: TransactionDraft, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val result = repository.recordTransaction(
        type = draft.type,
        amount = draft.amount,
        sourceAccountId = draft.sourceAccountId,
        destinationAccountId = draft.destinationAccountId,
        categoryId = draft.categoryId,
        description = draft.description,
        referenceId = draft.referenceId,
        notes = draft.notes,
        isTradingRelated = draft.isTradingRelated,
        tradingProfitOnly = draft.tradingProfitOnly
      )

      result.onSuccess { id ->
        _showAddTxDialog.value = false
        _pendingTxDraft.value = null
        loadData()
        onComplete(true, "Transaksi berhasil disimpan (ID: $id)")
      }.onFailure { err ->
        onComplete(false, err.message ?: "Gagal menyimpan transaksi")
      }
    }
  }

  fun promptVoidTransaction(tx: Transaction) {
    _showVoidDialogFor.value = tx
  }

  fun closeVoidDialog() {
    _showVoidDialogFor.value = null
  }

  fun executeVoidTransaction(txId: String, reason: String, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = repository.voidTransaction(txId, reason)
      res.onSuccess { msg ->
        _showVoidDialogFor.value = null
        loadData()
        onComplete(true, msg)
      }.onFailure { err ->
        onComplete(false, err.message ?: "Gagal membatalkan transaksi")
      }
    }
  }

  fun promptReconcile(account: Account) {
    _showReconcileDialogFor.value = account
  }

  fun closeReconcileDialog() {
    _showReconcileDialogFor.value = null
  }

  fun executeReconcile(accountId: String, actualPhysicalBalance: Long, reason: String, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = repository.reconcileAccount(accountId, actualPhysicalBalance, reason)
      res.onSuccess { msg ->
        _showReconcileDialogFor.value = null
        loadData()
        onComplete(true, msg)
      }.onFailure { err ->
        onComplete(false, err.message ?: "Gagal rekonsiliasi")
      }
    }
  }

  fun promptAddAccount() {
    _showAddAccountDialog.value = true
  }

  fun closeAddAccountDialog() {
    _showAddAccountDialog.value = false
  }

  fun executeCreateAccount(name: String, type: AccountType, openingBalance: Long, notes: String, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = repository.createAccount(name, type, openingBalance, notes)
      res.onSuccess {
        _showAddAccountDialog.value = false
        loadData()
        onComplete(true, "Rekening '${it.accountName}' berhasil dibuat.")
      }.onFailure { err ->
        onComplete(false, err.message ?: "Gagal membuat rekening")
      }
    }
  }

  fun promptContributeGoal(goal: SavingsGoal) {
    _showContributeGoalFor.value = goal
  }

  fun closeContributeGoalDialog() {
    _showContributeGoalFor.value = null
  }

  fun executeContributeGoal(targetId: String, amount: Long, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = repository.addSavingsContribution(targetId, amount)
      res.onSuccess {
        _showContributeGoalFor.value = null
        loadData()
        onComplete(true, "Setoran tabungan berhasil dicatat.")
      }.onFailure { err ->
        onComplete(false, err.message ?: "Gagal menyetor tabungan")
      }
    }
  }

  fun testConnection(url: String) {
    viewModelScope.launch {
      _isConnecting.value = true
      _connectionStatusMessage.value = "Menghubungi Google Apps Script & Sheets..."
      val res = repository.testProductionConnection(url)
      _isConnecting.value = false
      res.onSuccess { msg ->
        _connectionStatusMessage.value = "SUKSES: $msg"
      }.onFailure { err ->
        _connectionStatusMessage.value = "GAGAL: ${err.message}"
      }
    }
  }

  fun switchEnvironmentMode(mode: AppEnvironmentMode, onFail: (String) -> Unit) {
    if (mode == AppEnvironmentMode.PRODUCTION && (!repository.isProductionVerified || repository.productionWebAppUrl.isBlank())) {
      onFail("Mode Produksi hanya bisa diaktifkan setelah URL Web App diverifikasi dengan tombol 'Uji Koneksi'.")
      return
    }
    repository.mode = mode
    loadData()
  }

  companion object {
    fun formatRupiah(amount: Long): String {
      val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
      format.maximumFractionDigits = 0
      return format.format(amount).replace("Rp", "Rp ")
    }
  }
}
