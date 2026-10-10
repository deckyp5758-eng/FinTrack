/**
 * FinTrack AI — Development Mock Data
 * KHUSUS MODE PENGEMBANGAN — SESUAI KONTRAK AUDIT GOOGLE SHEETS
 */

import { DashboardSummary, Account, Category, Transaction, BudgetStatus, SavingsGoal, AuditLogEntry } from '../types';

export const MOCK_CATEGORIES: Category[] = [
  // 8 Expense Categories (Persis Sesuai Audit)
  { category_id: 'EXP_FOOD', category_name: 'Makan, kopi, rokok', transaction_type: 'expense', monthly_budget: 900000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_TRANSPORT', category_name: 'Transportasi', transaction_type: 'expense', monthly_budget: 450000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_BILLS', category_name: 'Tagihan', transaction_type: 'expense', monthly_budget: 800000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_SHOPPING', category_name: 'Belanja', transaction_type: 'expense', monthly_budget: 600000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_HEALTH', category_name: 'Kesehatan', transaction_type: 'expense', monthly_budget: 300000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_PERSONAL', category_name: 'Kebutuhan pribadi', transaction_type: 'expense', monthly_budget: 350000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_ENTERTAINMENT', category_name: 'Hiburan', transaction_type: 'expense', monthly_budget: 250000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'EXP_OTHER', category_name: 'Lainnya', transaction_type: 'expense', monthly_budget: 200000, is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },

  // 4 Income Categories (Persis Sesuai Audit)
  { category_id: 'INC_SALARY', category_name: 'Gaji', transaction_type: 'income', is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'INC_TRADING', category_name: 'Keuntungan trading terealisasi', transaction_type: 'income', is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'INC_BUSINESS', category_name: 'Pendapatan usaha', transaction_type: 'income', is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' },
  { category_id: 'INC_OTHER', category_name: 'Pemasukan lainnya', transaction_type: 'income', is_active: true, created_at: '2026-10-10 00:00:00', updated_at: '2026-10-10 00:00:00' }
];

export const MOCK_ACCOUNTS: Account[] = [
  {
    account_id: 'ACC_BCA_01',
    account_name: 'BCA Utama',
    account_type: 'bank',
    opening_balance: 15000000,
    opening_balance_date: '2026-10-01',
    currency: 'IDR',
    is_active: true,
    notes: 'Rekening Operasional & Pengeluaran Utama',
    created_at: '2026-10-01 08:00:00',
    updated_at: '2026-10-09 20:00:00',
    calculatedBalance: 24350000
  },
  {
    account_id: 'ACC_TRD_02',
    account_name: 'Rekening Trading Saham & Crypto',
    account_type: 'trading',
    opening_balance: 50000000,
    opening_balance_date: '2026-10-01',
    currency: 'IDR',
    is_active: true,
    notes: 'Modal Trading (Penarikan modal bukan profit)',
    created_at: '2026-10-01 08:00:00',
    updated_at: '2026-10-09 20:00:00',
    calculatedBalance: 53850000
  },
  {
    account_id: 'ACC_CSH_03',
    account_name: 'Kas Tunai Dompet',
    account_type: 'cash',
    opening_balance: 1500000,
    opening_balance_date: '2026-10-01',
    currency: 'IDR',
    is_active: true,
    notes: 'Uang Tunai Fisik di Dompet',
    created_at: '2026-10-01 08:00:00',
    updated_at: '2026-10-09 20:00:00',
    calculatedBalance: 1050000
  }
];

export const MOCK_TRANSACTIONS: Transaction[] = [
  {
    transaction_id: 'TX-202610-001',
    transaction_date: '2026-10-05',
    transaction_type: 'income',
    category_id: 'INC_SALARY',
    destination_account_id: 'ACC_BCA_01',
    amount: 12000000,
    description: 'Gaji Bulanan Oktober 2026',
    status: 'posted',
    created_at: '2026-10-05 09:00:00',
    updated_at: '2026-10-05 09:00:00',
    reference_id: 'PAYROLL-OCT26'
  },
  {
    transaction_id: 'TX-202610-002',
    transaction_date: '2026-10-06',
    transaction_type: 'expense',
    category_id: 'EXP_FOOD',
    source_account_id: 'ACC_CSH_03',
    amount: 450000,
    description: 'Makan siang & belanja kopi mingguan',
    status: 'posted',
    created_at: '2026-10-06 13:00:00',
    updated_at: '2026-10-06 13:00:00'
  },
  {
    transaction_id: 'TX-202610-003',
    transaction_date: '2026-10-08',
    transaction_type: 'income',
    category_id: 'INC_TRADING',
    destination_account_id: 'ACC_BCA_01',
    amount: 3850000,
    description: 'Realisasi profit swing trade BBCA (Bersih diterima)',
    status: 'posted',
    created_at: '2026-10-08 14:30:00',
    updated_at: '2026-10-08 14:30:00',
    notes: 'Keuntungan terealisasi sah'
  },
  {
    transaction_id: 'TX-202610-004',
    transaction_date: '2026-10-08',
    transaction_type: 'transfer',
    source_account_id: 'ACC_BCA_01',
    destination_account_id: 'ACC_CSH_03',
    amount: 500000,
    description: 'Tarik tunai ATM untuk kas dompet',
    status: 'posted',
    created_at: '2026-10-08 15:00:00',
    updated_at: '2026-10-08 15:00:00'
  }
];

export const MOCK_BUDGETS: BudgetStatus[] = [
  {
    budget_id: 'BDG-01',
    budget_month: '2026-10',
    category_id: 'EXP_FOOD',
    category_name: 'Makan, kopi, rokok',
    budget_amount: 900000,
    spent_amount: 450000,
    remaining_amount: 450000,
    percentage: 50.0,
    warning_threshold_percent: 80,
    status: 'NORMAL'
  },
  {
    budget_id: 'BDG-02',
    budget_month: '2026-10',
    category_id: 'EXP_BILLS',
    category_name: 'Tagihan',
    budget_amount: 800000,
    spent_amount: 720000,
    remaining_amount: 80000,
    percentage: 90.0,
    warning_threshold_percent: 80,
    status: 'WARNING'
  }
];

export const MOCK_SAVINGS_GOALS: SavingsGoal[] = [
  {
    target_id: 'GOAL-01',
    target_name: 'Dana Darurat 6 Bulan',
    target_amount: 60000000,
    current_amount: 35000000,
    remaining_amount: 25000000,
    progressPercentage: 58.3,
    target_date: '2027-06-30',
    linked_account_id: 'ACC_BCA_01',
    priority: 1,
    is_active: true,
    notes: 'Disimpan di instrumen likuid'
  }
];

export const MOCK_AUDIT_LOGS: AuditLogEntry[] = [
  {
    log_id: 'LOG-001',
    event_timestamp: '2026-10-08 14:30:00',
    actor: 'kingdomedantech@gmail.com',
    action: 'create_transaction',
    entity_type: 'transaction',
    entity_id: 'TX-202610-003',
    change_summary: 'Realisasi profit trading Rp 3.850.000 ke BCA',
    request_id: 'REQ-10821',
    result: 'success'
  }
];

export const MOCK_DASHBOARD: DashboardSummary = {
  database: {
    name: 'FinTrack AI — Personal Finance Database',
    spreadsheetId: '1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk',
    timezone: 'Asia/Jakarta',
    locale: 'in_ID',
    currency: 'IDR',
    status: 'DEVELOPMENT_MOCK',
    lastUpdated: '2026-10-10 00:00:00'
  },
  month: '2026-10',
  financialSummary: {
    totalBalance: 79250000,
    monthlyIncome: 15850000,
    monthlyExpense: 1170000,
    netCashflow: 14680000,
    monthlyTradingProfit: 3850000,
    savingRatePercentage: 92.6
  },
  accounts: MOCK_ACCOUNTS,
  categoryBreakdown: [
    { categoryId: 'EXP_FOOD', categoryName: 'Makan, kopi, rokok', amount: 450000, percentage: 38.5 },
    { categoryId: 'EXP_BILLS', categoryName: 'Tagihan', amount: 720000, percentage: 61.5 }
  ],
  budgets: MOCK_BUDGETS,
  savingsGoals: MOCK_SAVINGS_GOALS,
  recentTransactions: MOCK_TRANSACTIONS
};
