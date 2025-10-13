import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dc.entities.PostTag // Make sure to import your PostTag enum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionBottomSheet(
    onDismiss: () -> Unit,
    onTagsSelected: (List<PostTag>) -> Unit,
    initiallySelectedTags: List<PostTag> = emptyList()
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedTags by remember { mutableStateOf(initiallySelectedTags.toSet()) }

    ModalBottomSheet(
        onDismissRequest = {
            onTagsSelected(selectedTags.toList()) // Pass the final selection back
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Select Tags", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)

            // List all available tags from the enum
            PostTag.values().forEach { tag ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Toggle selection
                            val newSelection = selectedTags.toMutableSet()
                            if (tag in selectedTags) {
                                newSelection.remove(tag)
                            } else {
                                newSelection.add(tag)
                            }
                            selectedTags = newSelection
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = tag in selectedTags,
                        onCheckedChange = null // Handled by the Row's clickable
                    )
                    Text(
                        text = tag.name, // Or a more descriptive string if you have one
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
