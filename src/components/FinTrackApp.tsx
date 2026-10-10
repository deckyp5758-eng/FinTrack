/**
 * FinTrack AI — Personal Finance Manager
 * React Frontend Mobile-First Component
 */

import React, { useState, useEffect } from 'react';
import { fintrackService } from '../services/backendAdapter';
import { DashboardSummary, AppEnvironmentMode } from '../types';

export const FinTrackApp: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [mode, setMode] = useState<AppEnvironmentMode>('DEVELOPMENT');
  const [prodUrl, setProdUrl] = useState<string>('');
  const [activeTab, setActiveTab] = useState<'dashboard' | 'transactions' | 'accounts' | 'budgets' | 'goals' | 'audit'>('dashboard');

  useEffect(() => {
    loadData();
  }, [mode]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fintrackService.getDashboardSummary();
      setSummary(data);
    } catch (err: any) {
      setError(err.message || 'Gagal memuat data');
    } finally {
      setLoading(false);
    }
  };

  const formatIDR = (val: number) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      maximumFractionDigits: 0
    }).format(val);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Top Header */}
      <header className="bg-slate-900 border-b border-slate-800 p-4 sticky top-0 z-20">
        <div className="max-w-2xl mx-auto flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center font-bold text-emerald-400">
              Rp
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight">FinTrack AI</h1>
              <p className="text-xs text-slate-400">Personal Finance Manager • Asia/Jakarta</p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <span
              className={`px-2.5 py-1 text-xs rounded-full font-semibold border ${
                mode === 'DEVELOPMENT'
                  ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                  : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
              }`}
            >
              {mode === 'DEVELOPMENT' ? 'Mode Mock (Dev)' : 'Produksi (Sheets)'}
            </span>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-2xl w-full mx-auto p-4 space-y-6 pb-24">
        {loading && (
          <div className="p-8 text-center text-slate-400 animate-pulse">
            Memuat data keuangan dari backend...
          </div>
        )}

        {error && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-400 text-sm">
            <p className="font-semibold">Kesalahan Sistem:</p>
            <p>{error}</p>
          </div>
        )}

        {!loading && summary && (
          <>
            {/* Total Balance Card */}
            <div className="bg-gradient-to-br from-slate-900 to-slate-800 p-6 rounded-2xl border border-slate-700/60 shadow-xl relative overflow-hidden">
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Kekayaan Bersih</p>
              <h2 className="text-3xl font-extrabold text-white mt-1">
                {formatIDR(summary.financialSummary.totalBalance)}
              </h2>
              <div className="grid grid-cols-2 gap-4 mt-6 pt-4 border-t border-slate-700/40">
                <div>
                  <p className="text-xs text-emerald-400 font-medium">Pemasukan Bulan Ini</p>
                  <p className="text-lg font-bold text-emerald-300">
                    +{formatIDR(summary.financialSummary.monthlyIncome)}
                  </p>
                </div>
                <div>
                  <p className="text-xs text-rose-400 font-medium">Pengeluaran Bulan Ini</p>
                  <p className="text-lg font-bold text-rose-300">
                    -{formatIDR(summary.financialSummary.monthlyExpense)}
                  </p>
                </div>
              </div>
            </div>

            {/* Account List */}
            <div className="space-y-3">
              <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wider">Saldo Seluruh Rekening</h3>
              <div className="grid gap-3">
                {summary.accounts.map((acc) => (
                  <div
                    key={acc.id}
                    className="bg-slate-900/80 p-4 rounded-xl border border-slate-800 flex items-center justify-between hover:border-slate-700 transition"
                  >
                    <div>
                      <p className="font-medium text-slate-100">{acc.name}</p>
                      <p className="text-xs text-slate-400">
                        {acc.type} • {acc.accountNumber}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-white">{formatIDR(acc.calculatedBalance)}</p>
                      <span className="text-[10px] text-emerald-400 font-mono">Verified Backend</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};
export default FinTrackApp;
