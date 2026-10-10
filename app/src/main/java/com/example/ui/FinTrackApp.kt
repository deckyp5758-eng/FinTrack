package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinTrackApp(viewModel: FinTrackViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val activeFilter by viewModel.transactionFilter.collectAsStateWithLifecycle()
  val searchQuery by viewModel.transactionSearch.collectAsStateWithLifecycle()
  val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

  val showAddTxDialog by viewModel.showAddTxDialog.collectAsStateWithLifecycle()
  val pendingTxDraft by viewModel.pendingTxDraft.collectAsStateWithLifecycle()
  var showConfirmTxDialog by remember { mutableStateOf(false) }
  var txDraftToConfirm by remember { mutableStateOf<TransactionDraft?>(null) }

  val showVoidDialogFor by viewModel.showVoidDialogFor.collectAsStateWithLifecycle()
  val showReconcileDialogFor by viewModel.showReconcileDialogFor.collectAsStateWithLifecycle()
  val showAddAccountDialog by viewModel.showAddAccountDialog.collectAsStateWithLifecycle()
  val showContributeGoalFor by viewModel.showContributeGoalFor.collectAsStateWithLifecycle()

  val isConnecting by viewModel.isConnecting.collectAsStateWithLifecycle()
  val connectionMessage by viewModel.connectionStatusMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              "FinTrack AI",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 19.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = MaterialTheme.shapes.extraSmall,
              color = if (viewModel.repository.mode == AppEnvironmentMode.PRODUCTION && viewModel.repository.isProductionVerified)
                SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f)
            ) {
              Text(
                if (viewModel.repository.mode == AppEnvironmentMode.PRODUCTION && viewModel.repository.isProductionVerified)
                  "PROD" else "DEV MOCK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.repository.mode == AppEnvironmentMode.PRODUCTION && viewModel.repository.isProductionVerified)
                  SuccessGreen else WarningAmber,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        },
        actions = {
          IconButton(onClick = { viewModel.loadData() }) {
            Icon(Icons.Default.Refresh, contentDescription = "Segarkan Data", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Slate950,
          titleContentColor = Color.White
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = Slate950,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          selected = currentTab == 0,
          onClick = { viewModel.setTab(0) },
          icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
          label = { Text("Dashboard", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Emerald80,
            selectedTextColor = Emerald80,
            indicatorColor = Slate800
          )
        )
        NavigationBarItem(
          selected = currentTab == 1,
          onClick = { viewModel.setTab(1) },
          icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transaksi") },
          label = { Text("Transaksi", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Emerald80,
            selectedTextColor = Emerald80,
            indicatorColor = Slate800
          )
        )
        NavigationBarItem(
          selected = currentTab == 2,
          onClick = { viewModel.setTab(2) },
          icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Rekening") },
          label = { Text("Rekening", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Emerald80,
            selectedTextColor = Emerald80,
            indicatorColor = Slate800
          )
        )
        NavigationBarItem(
          selected = currentTab == 3,
          onClick = { viewModel.setTab(3) },
          icon = { Icon(Icons.Default.Savings, contentDescription = "Anggaran") },
          label = { Text("Anggaran", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Emerald80,
            selectedTextColor = Emerald80,
            indicatorColor = Slate800
          )
        )
        NavigationBarItem(
          selected = currentTab == 4,
          onClick = { viewModel.setTab(4) },
          icon = { Icon(Icons.Default.Storage, contentDescription = "Database") },
          label = { Text("Database", fontSize = 11.sp) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Emerald80,
            selectedTextColor = Emerald80,
            indicatorColor = Slate800
          )
        )
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = Slate950
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (val state = uiState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(color = Emerald80)
              Spacer(modifier = Modifier.height(12.dp))
              Text("Memuat data FinTrack...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
        is UiState.Error -> {
          Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(
              colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
              shape = MaterialTheme.shapes.medium
            ) {
              Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Terjadi Kesalahan", fontWeight = FontWeight.Bold, color = DangerRed)
                Spacer(modifier = Modifier.height(6.dp))
                Text(state.message, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = { viewModel.loadData() },
                  colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                  Text("Coba Lagi")
                }
              }
            }
          }
        }
        is UiState.Success -> {
          when (currentTab) {
            0 -> DashboardScreen(
              summary = state.summary,
              onAddTransaction = { type -> viewModel.openAddTransactionDialog(type) },
              onNavigateToAccounts = { viewModel.setTab(2) },
              onNavigateToBudgets = { viewModel.setTab(3) },
              onNavigateToTransactions = { viewModel.setTab(1) },
              onReconcileAccount = { acc -> viewModel.promptReconcile(acc) }
            )
            1 -> TransactionsScreen(
              transactions = state.summary.recentTransactions,
              activeFilter = activeFilter,
              searchQuery = searchQuery,
              onFilterChange = { viewModel.setFilter(it) },
              onSearchChange = { viewModel.setSearchQuery(it) },
              onAddTransactionClick = { viewModel.openAddTransactionDialog() },
              onVoidClick = { tx -> viewModel.promptVoidTransaction(tx) }
            )
            2 -> AccountsScreen(
              accounts = state.summary.accounts,
              onAddAccountClick = { viewModel.promptAddAccount() },
              onReconcileClick = { acc -> viewModel.promptReconcile(acc) }
            )
            3 -> BudgetsGoalsScreen(
              budgets = state.summary.budgets,
              savingsGoals = state.summary.savingsGoals,
              onContributeGoal = { goal -> viewModel.promptContributeGoal(goal) }
            )
            4 -> AuditAndSettingsScreen(
              currentMode = viewModel.repository.mode,
              isProductionVerified = viewModel.repository.isProductionVerified,
              productionUrl = viewModel.repository.productionWebAppUrl,
              isConnecting = isConnecting,
              connectionMessage = connectionMessage,
              auditLogs = auditLogs,
              onSwitchMode = { mode ->
                viewModel.switchEnvironmentMode(mode) { errorMsg ->
                  coroutineScope.launch { snackbarHostState.showSnackbar(errorMsg) }
                }
              },
              onTestConnection = { url -> viewModel.testConnection(url) }
            )
          }

          // Dialog: Add Transaction
          if (showAddTxDialog && pendingTxDraft != null) {
            AddTransactionDialog(
              accounts = state.summary.accounts,
              initialType = pendingTxDraft!!.type,
              onDismiss = { viewModel.closeAddTransactionDialog() },
              onRequestConfirm = { draft ->
                txDraftToConfirm = draft
                showConfirmTxDialog = true
              }
            )
          }

          // Dialog: Konfirmasi Transaksi (Sebelum Disimpan)
          if (showConfirmTxDialog && txDraftToConfirm != null) {
            ConfirmTransactionDialog(
              draft = txDraftToConfirm!!,
              accounts = state.summary.accounts,
              onDismiss = { showConfirmTxDialog = false },
              onConfirm = {
                val draft = txDraftToConfirm!!
                showConfirmTxDialog = false
                viewModel.confirmAndSaveTransaction(draft) { success, msg ->
                  coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                }
              }
            )
          }

          // Dialog: Void Transaction
          if (showVoidDialogFor != null) {
            VoidTransactionDialog(
              tx = showVoidDialogFor!!,
              onDismiss = { viewModel.closeVoidDialog() },
              onConfirmVoid = { reason ->
                val txId = showVoidDialogFor!!.transactionId
                viewModel.executeVoidTransaction(txId, reason) { success, msg ->
                  coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                }
              }
            )
          }

          // Dialog: Reconcile Account
          if (showReconcileDialogFor != null) {
            ReconcileAccountDialog(
              account = showReconcileDialogFor!!,
              onDismiss = { viewModel.closeReconcileDialog() },
              onConfirmReconcile = { actual, reason ->
                val accId = showReconcileDialogFor!!.accountId
                viewModel.executeReconcile(accId, actual, reason) { success, msg ->
                  coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                }
              }
            )
          }

          // Dialog: Add Account
          if (showAddAccountDialog) {
            AddAccountDialog(
              onDismiss = { viewModel.closeAddAccountDialog() },
              onConfirm = { name, type, openBal, notes ->
                viewModel.executeCreateAccount(name, type, openBal, notes) { success, msg ->
                  coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                }
              }
            )
          }

          // Dialog: Contribute Goal
          if (showContributeGoalFor != null) {
            ContributeGoalDialog(
              goal = showContributeGoalFor!!,
              onDismiss = { viewModel.closeContributeGoalDialog() },
              onConfirm = { amt ->
                val gId = showContributeGoalFor!!.targetId
                viewModel.executeContributeGoal(gId, amt) { success, msg ->
                  coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                }
              }
            )
          }
        }
      }
    }
  }
}
