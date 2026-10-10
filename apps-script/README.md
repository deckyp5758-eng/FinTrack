# FinTrack AI — Backend Google Apps Script & Database Guide

Direktori ini berisi seluruh source code backend Google Apps Script yang berfungsi sebagai **Single Source of Truth** untuk aplikasi **FinTrack AI — Personal Finance Manager**.

---

## 1. Database Google Sheets

- **Nama Database:** `FinTrack AI — Personal Finance Database`
- **Spreadsheet ID:** `1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk`
- **Zona Waktu:** `Asia/Jakarta`
- **Mata Uang:** `IDR (Rupiah)`

### Struktur Sheet & Kolom Wajib (Baris 1 sebagai Header)

1. **`Transactions`**
   - Kolom: `id`, `date`, `type`, `amount`, `sourceAccountId`, `destAccountId`, `categoryId`, `description`, `tags`, `isVoid`, `createdAt`, `tradingProfitOnly`
2. **`Accounts`**
   - Kolom: `id`, `name`, `type`, `accountNumber`, `isActive`, `notes`
3. **`Categories`**
   - Kolom: `id`, `name`, `type`, `icon`, `color`
4. **`Budgets`**
   - Kolom: `id`, `month`, `categoryId`, `budgetAmount`, `alertThreshold`
5. **`SavingsGoals`**
   - Kolom: `id`, `title`, `targetAmount`, `currentAmount`, `targetDate`, `status`, `notes`
6. **`AuditLog`**
   - Kolom: `id`, `timestamp`, `action`, `entityType`, `entityId`, `details`, `userEmail`

---

## 2. Cara Kerja Frontend & Backend

- **Single Source of Truth:** Frontend tidak pernah menghitung saldo akhir atau mengubah angka dashboard secara mandiri. Seluruh perhitungan saldo, validasi batas anggaran, dan agregasi laporan dilakukan oleh Apps Script.
- **Mode Aplikasi:**
  - **Mode Pengembangan (Development):** Menggunakan mock data lokal bertanda jelas untuk pengujian UI tanpa mengakses Google Sheets produksi.
  - **Mode Produksi (Production):** Hanya aktif jika URL Web App Apps Script sudah dimasukkan dan diverifikasi dengan tombol "Uji Koneksi". Jika koneksi gagal, sistem **tidak** otomatis beralih ke mock data agar tidak menyesatkan pengguna.
- **Aturan Finansial:**
  - Pemasukan menambah saldo rekening tujuan.
  - Pengeluaran mengurangi saldo rekening sumber.
  - Transfer hanya memindahkan saldo antar-rekening (tidak dihitung sebagai pemasukan atau pengeluaran).
  - Modal trading yang ditarik kembali **bukan** keuntungan trading. Hanya keuntungan terealisasi yang diizinkan sebagai pemasukan.
  - Transaksi `isVoid = true` diabaikan dalam kalkulasi saldo.
  - Seluruh transaksi wajib melalui dialog konfirmasi pengguna.

---

## 3. Langkah Deployment Web App (Manual)

1. Buka Google Sheets Anda (`https://docs.google.com/spreadsheets/d/1B35mZMORnmNwKyK1axLtchHkhtSafgYhIp50lzwCyNk/edit`).
2. Klik menu **Extensions** > **Apps Script**.
3. Buat file script sesuai dengan file di folder `apps-script/`:
   - `Code.gs`
   - `Database.gs`
   - `Transactions.gs`
   - `Accounts.gs`
   - `Budgets.gs`
   - `SavingsGoals.gs`
   - `Dashboard.gs`
   - `AuditLog.gs`
   - Pastikan timezone di `appsscript.json` adalah `Asia/Jakarta`.
4. Klik tombol **Deploy** > **New deployment**.
5. Pilih type **Web app**:
   - **Description:** `FinTrack AI Production API`
   - **Execute as:** `Me` (atau Akun Google Anda)
   - **Who has access:** `Anyone` (atau sesuaikan dengan kebutuhan domain Anda)
6. Salin **Web App URL** yang dihasilkan (format: `https://script.google.com/macros/s/.../exec`).
7. Buka aplikasi FinTrack AI di tab **Pengaturan / Database**, pilih **Mode Produksi**, tempelkan URL tersebut, lalu klik **Uji Koneksi**.

---

## 4. Otomatisasi Sinkronisasi dengan GitHub & Clasp

Untuk menyinkronkan repository GitHub ke Google Apps Script secara otomatis:

1. Buat project Apps Script dan catat **Script ID** dari menu *Project Settings*.
2. Install clasp di mesin lokal: `npm install -g @google/clasp`
3. Lakukan login: `clasp login` (akan menghasilkan file `~/.clasprc.json`).
4. **PENTING:** Jangan pernah menyimpan file `.clasprc.json` atau token rahasia ke dalam Git!
5. Buka GitHub repository Anda > **Settings** > **Secrets and variables** > **Actions**.
6. Tambahkan 2 secrets:
   - `SCRIPT_ID`: ID Google Apps Script Anda.
   - `CLASPRC_JSON`: Isi seluruh teks dari file lokal `~/.clasprc.json`.
7. Workflow di `.github/workflows/deploy-apps-script.yml` akan secara otomatis men-deploy folder `apps-script/` setiap kali ada push ke branch `main`.
