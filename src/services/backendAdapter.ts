/**
 * FinTrack AI — Personal Finance Manager
 * Backend Adapter Service (Sesuai Kontrak Audit Google Sheets)
 */

import {
  DashboardSummary,
  Account,
  Transaction,
  BudgetStatus,
  SavingsGoal,
  AuditLogEntry,
  AppEnvironmentMode
} from '../types';
import {
  MOCK_DASHBOARD,
  MOCK_ACCOUNTS,
  MOCK_TRANSACTIONS,
  MOCK_BUDGETS,
  MOCK_SAVINGS_GOALS,
  MOCK_AUDIT_LOGS
} from './mockData';

export interface BackendAdapterConfig {
  mode: AppEnvironmentMode;
  productionWebAppUrl?: string;
}

export class FinTrackBackendAdapter {
  private mode: AppEnvironmentMode = 'DEVELOPMENT';
  private productionWebAppUrl: string = '';
  private isProductionVerified: boolean = false;

  constructor(config?: BackendAdapterConfig) {
    if (config) {
      this.mode = config.mode;
      this.productionWebAppUrl = config.productionWebAppUrl || '';
    }
  }

  public setMode(mode: AppEnvironmentMode) {
    if (mode === 'PRODUCTION' && !this.isProductionVerified && !this.productionWebAppUrl) {
      throw new Error("Mode produksi hanya dapat diaktifkan setelah URL Web App Apps Script dikonfigurasi dan diverifikasi.");
    }
    this.mode = mode;
  }

  public getMode(): AppEnvironmentMode {
    return this.mode;
  }

  public setProductionUrl(url: string) {
    this.productionWebAppUrl = url.trim();
    this.isProductionVerified = false;
  }

  public getProductionUrl(): string {
    return this.productionWebAppUrl;
  }

  public isVerified(): boolean {
    return this.isProductionVerified;
  }

  public async testProductionConnection(url?: string): Promise<{ success: boolean; message: string; details?: any }> {
    const targetUrl = url || this.productionWebAppUrl;
    if (!targetUrl) {
      return { success: false, message: 'URL Web App Google Apps Script belum dimasukkan.' };
    }

    try {
      const response = await fetch(`${targetUrl}?action=testConnection`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
      });

      if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status} ${response.statusText}`);
      }

      const json = await response.json();
      if (json.success) {
        this.productionWebAppUrl = targetUrl;
        this.isProductionVerified = true;
        return {
          success: true,
          message: 'Koneksi ke Google Sheets (ID: 1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk) terverifikasi aktif!',
          details: json.data
        };
      } else {
        throw new Error(json.error || 'Backend merespons dengan kesalahan.');
      }
    } catch (err: any) {
      this.isProductionVerified = false;
      return {
        success: false,
        message: `Gagal menghubungkan ke Google Sheets: ${err.message}. Pastikan Web App sudah dideploy dengan akses "Anyone".`
      };
    }
  }

  private async callProductionApi(action: string, postData?: any): Promise<any> {
    if (!this.productionWebAppUrl) {
      throw new Error('URL Web App Google Apps Script belum dikonfigurasi.');
    }

    try {
      const url = `${this.productionWebAppUrl}?action=${encodeURIComponent(action)}`;
      const options: RequestInit = {
        method: postData ? 'POST' : 'GET',
        headers: { 'Accept': 'application/json' },
        body: postData ? JSON.stringify(postData) : undefined
      };

      const response = await fetch(url, options);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const json = await response.json();
      if (!json.success) {
        throw new Error(json.error || `Aksi ${action} gagal.`);
      }

      return json.data;
    } catch (error: any) {
      // JANGAN PERNAH fallback otomatis ke mock saat koneksi produksi gagal!
      throw new Error(`Kesalahan Database Produksi: ${error.message}`);
    }
  }

  public async getDashboardSummary(month?: string): Promise<DashboardSummary> {
    if (this.mode === 'DEVELOPMENT') {
      return MOCK_DASHBOARD;
    }
    return this.callProductionApi('getDashboardSummary', { month });
  }

  public async getAccounts(): Promise<Account[]> {
    if (this.mode === 'DEVELOPMENT') {
      return MOCK_ACCOUNTS;
    }
    return this.callProductionApi('getAccounts');
  }

  public async getTransactions(filter?: any): Promise<Transaction[]> {
    if (this.mode === 'DEVELOPMENT') {
      return MOCK_TRANSACTIONS;
    }
    return this.callProductionApi('getTransactions', filter);
  }

  public async recordTransaction(txData: any): Promise<any> {
    if (!txData.isUserConfirmed) {
      throw new Error('Transaksi harus dikonfirmasi oleh pengguna sebelum disimpan.');
    }

    if (this.mode === 'DEVELOPMENT') {
      const newTx: Transaction = {
        transaction_id: `TX-DEV-${Date.now()}`,
        transaction_date: txData.transaction_date || new Date().toISOString().split('T')[0],
        transaction_type: txData.transaction_type,
        category_id: txData.category_id,
        source_account_id: txData.source_account_id,
        destination_account_id: txData.destination_account_id,
        amount: Number(txData.amount),
        description: txData.description,
        status: 'posted',
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      };
      MOCK_TRANSACTIONS.unshift(newTx);
      return { success: true, transaction: newTx };
    }

    return this.callProductionApi('recordTransaction', { data: txData });
  }

  public async voidTransaction(transactionId: string, reason: string): Promise<any> {
    if (this.mode === 'DEVELOPMENT') {
      const tx = MOCK_TRANSACTIONS.find(t => t.transaction_id === transactionId);
      if (tx) tx.status = 'void';
      return { success: true, message: `Transaksi ${transactionId} dibatalkan (void)` };
    }
    return this.callProductionApi('voidTransaction', { transactionId, reason });
  }

  public async createAccount(accountData: any): Promise<any> {
    if (this.mode === 'DEVELOPMENT') {
      const acc: Account = {
        account_id: `ACC-DEV-${Date.now()}`,
        account_name: accountData.account_name,
        account_type: accountData.account_type || 'bank',
        opening_balance: Number(accountData.opening_balance) || 0,
        opening_balance_date: new Date().toISOString().split('T')[0],
        currency: 'IDR',
        is_active: true,
        notes: accountData.notes,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
        calculatedBalance: Number(accountData.opening_balance) || 0
      };
      MOCK_ACCOUNTS.push(acc);
      return acc;
    }
    return this.callProductionApi('createAccount', { data: accountData });
  }

  public async reconcileAccount(accountId: string, actualBalance: number, reason: string): Promise<any> {
    if (this.mode === 'DEVELOPMENT') {
      const acc = MOCK_ACCOUNTS.find(a => a.account_id === accountId);
      if (!acc) throw new Error('Rekening tidak ditemukan.');
      const diff = actualBalance - acc.calculatedBalance;
      acc.calculatedBalance = actualBalance;
      return { status: 'RECONCILED', previousBalance: actualBalance - diff, newBalance: actualBalance, difference: diff };
    }
    return this.callProductionApi('reconcileAccount', { accountId, actualBalance, reason });
  }

  public async upsertBudget(budgetData: any): Promise<any> {
    return this.callProductionApi('upsertBudget', { data: budgetData });
  }

  public async createSavingsGoal(goalData: any): Promise<any> {
    return this.callProductionApi('createSavingsGoal', { data: goalData });
  }

  public async addSavingsContribution(targetId: string, amount: number): Promise<any> {
    return this.callProductionApi('addSavingsContribution', { targetId, amount });
  }

  public async getAuditLogs(limit: number = 40): Promise<AuditLogEntry[]> {
    if (this.mode === 'DEVELOPMENT') {
      return MOCK_AUDIT_LOGS;
    }
    return this.callProductionApi('getAuditLogs', { limit });
  }
}

export const fintrackService = new FinTrackBackendAdapter();
