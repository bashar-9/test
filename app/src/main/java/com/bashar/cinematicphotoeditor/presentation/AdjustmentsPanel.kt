package com.bashar.cinematicphotoeditor.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdjustmentsPanel(
    tools: List<AdjustmentTool>,
    selectedTool: AdjustmentTool?,
    onToolClick: (AdjustmentTool) -> Unit,
    adjustmentValues: Map<String, Float>,
    onAdjustmentChange: (String, Float) -> Unit,
    onResetAdjustment: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
    ) {
        // Show the slider for the currently selected tool
        if (selectedTool != null) {
            val currentValue = adjustmentValues[selectedTool.name] ?: selectedTool.initialValue
            AdjustmentSlider(
                label = selectedTool.name,
                value = currentValue,
                valueRange = selectedTool.valueRange,
                onValueChange = { newValue -> onAdjustmentChange(selectedTool.name, newValue) },
                onReset = { onResetAdjustment(selectedTool.name) }
            )
        } else {
            // Add a spacer to maintain a consistent height when no slider is shown
            Spacer(modifier = Modifier.height(68.dp))
        }

        // Horizontally scrolling list of adjustment tools
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tools) { tool ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onToolClick(tool) }
                        .padding(horizontal = 8.dp)
                ) {
                    val boxModifier = if (tool.name == selectedTool?.name) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = boxModifier
                            .padding(2.dp)
                            .size(56.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = tool.icon, contentDescription = tool.name)
                    }
                    Text(text = tool.name, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
