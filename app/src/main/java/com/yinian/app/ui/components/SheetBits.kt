package com.yinian.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yinian.app.ui.theme.LocalAppColors

/** Small muted caption used above a field, with an optional right-hand hint. */
@Composable
fun SheetLabel(label: String, hint: String? = null) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = colors.muted,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        if (hint != null) {
            Text(
                text = hint,
                color = colors.muted.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
