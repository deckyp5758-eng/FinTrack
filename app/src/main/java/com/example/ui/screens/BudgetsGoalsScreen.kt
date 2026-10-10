package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.FinTrackViewModel
import com.example.ui.theme.*

@Composable
fun BudgetsGoalsScreen(
  budgets: List<BudgetStatus>,
  savingsGoals: List<SavingsGoal>,
  onContributeGoal: (SavingsGoal) -> Unit
) {
  var selectedSubTab by remember { mutableStateOf(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    TabRow(
      selectedTabIndex = selectedSubTab,
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(vertical = 12.dp)
    ) {
      Tab(
        selected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        text = { Text("Anggaran Bulanan (${budgets.size})", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        text = { Text("Target Tabungan (${savingsGoals.size})", fontWeight = FontWeight.Bold) }
      )
    }

    if (selectedSubTab == 0) {
      // Budgets Tab
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
      ) {
        val totalBudget = budgets.sumOf { it.budgetAmount }
        val totalSpent = budgets.sumOf { it.spentAmount }

        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text("TOTAL ANGGARAN (SHEET: ANGGARAN A1:I1)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                FinTrackViewModel.formatRupiah(totalBudget),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Realisasi: ${FinTrackViewModel.formatRupiah(totalSpent)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sisa: ${FinTrackViewModel.formatRupiah(totalBudget - totalSpent)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
              }
            }
          }
        }

        items(budgets) { b ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(b.categoryName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text("ID: ${b.categoryId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(
                  onClick = {},
                  label = {
                    Text(
                      when (b.status) {
                        "EXCEEDED" -> "Melebihi Batas!"
                        "WARNING" -> "Waspada (≥${b.warningThresholdPercent}%)"
                        else -> "Aman"
                      },
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  },
                  colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (b.status) {
                      "EXCEEDED" -> DangerRed.copy(alpha = 0.2f)
                      "WARNING" -> WarningAmber.copy(alpha = 0.2f)
                      else -> SuccessGreen.copy(alpha = 0.2f)
                    },
                    labelColor = when (b.status) {
                      "EXCEEDED" -> DangerRed
                      "WARNING" -> WarningAmber
                      else -> SuccessGreen
                    }
                  ),
                  modifier = Modifier.height(26.dp)
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              LinearProgressIndicator(
                progress = { (b.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = when (b.status) {
                  "EXCEEDED" -> DangerRed
                  "WARNING" -> WarningAmber
                  else -> Emerald40
                },
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              )

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  "${FinTrackViewModel.formatRupiah(b.spentAmount)} / ${FinTrackViewModel.formatRupiah(b.budgetAmount)} (${b.percentage}%)",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  if (b.remainingAmount >= 0) "Sisa ${FinTrackViewModel.formatRupiah(b.remainingAmount)}" else "Over ${FinTrackViewModel.formatRupiah(-b.remainingAmount)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (b.remainingAmount >= 0) SuccessGreen else DangerRed
                )
              }
            }
          }
        }
      }
    } else {
      // Savings Goals Tab
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
      ) {
        items(savingsGoals) { goal ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(goal.targetName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  Text("Target: ${goal.targetDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                  "${goal.progressPercentage}%",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 18.sp,
                  color = if (goal.progressPercentage >= 100f) SuccessGreen else InfoCyan
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              LinearProgressIndicator(
                progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(10.dp)
                  .clip(RoundedCornerShape(5.dp)),
                color = if (goal.progressPercentage >= 100f) SuccessGreen else InfoCyan,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              )

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  "Terkumpul: ${FinTrackViewModel.formatRupiah(goal.currentAmount)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  "Goal: ${FinTrackViewModel.formatRupiah(goal.targetAmount)}",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (goal.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(goal.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Button(
                  onClick = { onContributeGoal(goal) },
                  colors = ButtonDefaults.buttonColors(containerColor = Emerald40),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(34.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Setor Tabungan", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}
