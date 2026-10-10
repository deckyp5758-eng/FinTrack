package com.example

import com.example.data.FinTrackRepository
import com.example.data.MockDataProvider
import com.example.model.AccountType
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.ui.FinTrackViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

  private lateinit var repository: FinTrackRepository

  @Before
  fun setUp() {
    repository = FinTrackRepository()
  }

  @Test
  fun testRupiahFormat() {
    val formatted = FinTrackViewModel.formatRupiah(15000000L)
    assertTrue(formatted.contains("15.000.000"))
  }

  @Test
  fun testTradingProfitRule_WithdrawalNotProfit() = runBlocking {
    // Modal trading yang ditarik BUKAN keuntungan (harus ditolak jika diinput sebagai pemasukan tanpa profit terealisasi)
    val result = repository.recordTransaction(
      type = TransactionType.INCOME,
      amount = 5000000L,
      sourceAccountId = null,
      destinationAccountId = "ACC_BCA_01",
      categoryId = "INC_TRADING",
      description = "Penarikan modal trading ke bank",
      referenceId = "REF-WD-01",
      isTradingRelated = true,
      tradingProfitOnly = false // Tidak valid: penarikan modal bukan profit
    )
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("Penarikan modal trading BUKAN keuntungan") == true)
  }

  @Test
  fun testTradingProfitRule_RealizedProfitAllowed() = runBlocking {
    val result = repository.recordTransaction(
      type = TransactionType.INCOME,
      amount = 1200000L,
      sourceAccountId = null,
      destinationAccountId = "ACC_BCA_01",
      categoryId = "INC_TRADING",
      description = "Realized profit swing trade",
      referenceId = "REF-PROFIT-01",
      isTradingRelated = true,
      tradingProfitOnly = true // Sah: keuntungan terealisasi
    )
    assertTrue(result.isSuccess)
  }

  @Test
  fun testTransferLogic_MaintainsAccountingIntegrity() = runBlocking {
    val src = MockDataProvider.accounts.find { it.accountId == "ACC_BCA_01" }!!
    val dst = MockDataProvider.accounts.find { it.accountId == "ACC_CSH_03" }!!
    val initialSrc = src.calculatedBalance
    val initialDst = dst.calculatedBalance

    val transferAmount = 500000L
    val result = repository.recordTransaction(
      type = TransactionType.TRANSFER,
      amount = transferAmount,
      sourceAccountId = src.accountId,
      destinationAccountId = dst.accountId,
      categoryId = null, // Kontrak audit: Transfer categoryId harus null/kosong
      description = "Tarik tunai ATM",
      referenceId = "REF-TRF-99"
    )
    assertTrue(result.isSuccess)

    val updatedSrc = MockDataProvider.accounts.find { it.accountId == src.accountId }!!
    val updatedDst = MockDataProvider.accounts.find { it.accountId == dst.accountId }!!

    assertEquals(initialSrc - transferAmount, updatedSrc.calculatedBalance)
    assertEquals(initialDst + transferAmount, updatedDst.calculatedBalance)
  }

  @Test
  fun testVoidTransaction_RevertsBalance() = runBlocking {
    val initialAcc = MockDataProvider.accounts.find { it.accountId == "ACC_BCA_01" }!!
    val initialBal = initialAcc.calculatedBalance

    val amount = 750000L
    val txRes = repository.recordTransaction(
      type = TransactionType.INCOME,
      amount = amount,
      sourceAccountId = null,
      destinationAccountId = initialAcc.accountId,
      categoryId = "INC_OTHER",
      description = "Pendapatan cashback",
      referenceId = "REF-CB-01"
    )
    val txId = txRes.getOrThrow()
    assertEquals(initialBal + amount, MockDataProvider.accounts.find { it.accountId == initialAcc.accountId }!!.calculatedBalance)

    // Batalkan transaksi
    val voidRes = repository.voidTransaction(txId, "Salah input akun penerima")
    assertTrue(voidRes.isSuccess)

    // Saldo kembali seperti semula
    assertEquals(initialBal, MockDataProvider.accounts.find { it.accountId == initialAcc.accountId }!!.calculatedBalance)
  }
}
