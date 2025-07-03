package com.bashar.cinematicphotoeditor.presentation

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current // Get context for the Toast message

    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTool != null) {
                val currentValue = adjustmentValues[selectedTool.name] ?: selectedTool.initialValue
                AdjustmentSlider(
                    label = selectedTool.name,
                    value = currentValue,
                    valueRange = selectedTool.valueRange,
                    onValueChange = { newValue -> onAdjustmentChange(selectedTool.name, newValue) },
                    onReset = { onResetAdjustment(selectedTool.name) }
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tools) { tool ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        // The clickable modifier now checks if the tool is implemented
                        .clickable {
                            if (tool.isImplemented) {
                                onToolClick(tool)
                            } else {
                                Toast.makeText(context, "${tool.name} is coming soon!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    val isSelected = tool.name == selectedTool?.name
                    val isAdjusted = (adjustmentValues[tool.name] ?: tool.initialValue) != tool.initialValue

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(56.dp)
                            .then(
                                if (isAdjusted && tool.isImplemented) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // The icon is faded if not implemented
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = tool.name,
                            modifier = Modifier.alpha(if (tool.isImplemented) 1f else 0.4f)
                        )
                    }

                    // The text is also faded if not implemented
                    Text(
                        text = tool.name,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(if (tool.isImplemented) 1f else 0.4f)
                    )
                }
            }
        }
    }
}