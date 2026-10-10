/**
 * FinTrack AI — Personal Finance Manager
 * Frontend TypeScript Type Definitions (Sesuai Audit Kontrak Google Sheets)
 */

export type TransactionType = 'income' | 'expense' | 'transfer';
export type TransactionStatus = 'posted' | 'void';
export type AccountType = 'bank' | 'cash' | 'trading' | 'other';

export interface Account {
  account_id: string;
  account_name: string;
  account_type: AccountType;
  opening_balance: number;
  opening_balance_date: string;
  currency: 'IDR';
  is_active: boolean;
  notes?: string;
  created_at: string;
  updated_at: string;
  calculatedBalance: number; // Single source of truth dari backend
}

export interface Category {
  category_id: string;
  category_name: string;
  transaction_type: 'income' | 'expense';
  monthly_budget?: number;
  is_active: boolean;
  sort_order?: number;
  notes?: string;
  created_at: string;
  updated_at: string;
}

export interface Transaction {
  transaction_id: string;
  transaction_date: string; // YYYY-MM-DD
  transaction_type: TransactionType;
  category_id?: string;
  source_account_id?: string;
  destination_account_id?: string;
  amount: number;
  description: string;
  status: TransactionStatus;
  created_at: string;
  updated_at: string;
  reference_id?: string;
  notes?: string;
}

export interface BudgetStatus {
  budget_id: string;
  budget_month: string; // YYYY-MM
  category_id: string;
  category_name: string;
  budget_amount: number;
  spent_amount: number;
  remaining_amount: number;
  percentage: number;
  warning_threshold_percent: number;
  status: 'NORMAL' | 'WARNING' | 'EXCEEDED';
}

export interface SavingsGoal {
  target_id: string;
  target_name: string;
  target_amount: number;
  current_amount: number;
  remaining_amount: number;
  progressPercentage: number;
  target_date: string;
  linked_account_id?: string;
  priority?: number;
  is_active: boolean;
  notes?: string;
}

export interface AuditLogEntry {
  log_id: string;
  event_timestamp: string;
  actor: string;
  action: string;
  entity_type: string;
  entity_id: string;
  change_summary: string;
  request_id: string;
  result: 'success' | 'failure' | 'void';
  notes?: string;
}

export interface DashboardSummary {
  database: {
    name: string;
    spreadsheetId: string;
    timezone: string;
    locale: string;
    currency: string;
    status: 'CONNECTED' | 'DISCONNECTED' | 'DEVELOPMENT_MOCK';
    lastUpdated: string;
  };
  month: string;
  financialSummary: {
    totalBalance: number;
    monthlyIncome: number;
    monthlyExpense: number;
    netCashflow: number;
    monthlyTradingProfit: number;
    savingRatePercentage: number;
  };
  accounts: Account[];
  categoryBreakdown: {
    categoryId: string;
    categoryName: string;
    amount: number;
    percentage: number;
  }[];
  budgets: BudgetStatus[];
  savingsGoals: SavingsGoal[];
  recentTransactions: Transaction[];
}

export type AppEnvironmentMode = 'DEVELOPMENT' | 'PRODUCTION';
