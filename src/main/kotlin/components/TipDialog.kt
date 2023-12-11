package components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import viewModel.AppViewModel

@Preview
@Composable
fun TipDialogPreview() {
    MaterialTheme {
        TipDialog(
            message = "这是一个提示框"
        )
    }
}

@Composable
fun TipDialog(
    onDismissRequest: () -> Unit = {},
    message: String,
    showCancel: Boolean = false,
    onConfirmClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmClick.invoke()
                    onDismissRequest.invoke()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "确定")
            }
        },
        dismissButton = if (showCancel) {
            {
                Button(
                    onClick = {
                        onDismissRequest.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "取消")
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    )
}


@Composable
fun AddAccountDialog(appViewModel: AppViewModel) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = {
        appViewModel.sendEvent(AppViewModel.AssistantEvent.HideDialog)
    }) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(vertical = 160.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "添加MIHOYO账号", modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextField(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        .padding(horizontal = 20.dp),
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    label = {
                        Text(text = "账号昵称")
                    }
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.size(80.dp, 60.dp).align(Alignment.CenterVertically),
                        onClick = {
                            appViewModel.sendEvent(AppViewModel.AssistantEvent.HideDialog)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "取消")
                    }
                    Button(
                        modifier = Modifier.size(100.dp, 60.dp).align(Alignment.CenterVertically)
                            .padding(horizontal = 10.dp),
                        onClick = {
                            appViewModel.sendEvent(AppViewModel.AssistantEvent.AddGenshinAccount(text))
                            appViewModel.sendEvent(AppViewModel.AssistantEvent.HideDialog)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "确定")
                    }
                }
            }

        }
    }
}