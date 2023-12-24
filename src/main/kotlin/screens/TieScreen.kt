package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bean.TieAccount
import components.AddAccountDialog
import components.TipDialog
import components.UserAccountList
import viewModel.HonkaiImapackViewModel

@Composable
fun TieScreen() {
    val honkaiViewModel = remember { HonkaiImapackViewModel() }
    val honkaiState by honkaiViewModel.container.uiStateFlow.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().height(70.dp)
            ) {
                var textValue by remember { mutableStateOf("") }

                TextField(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 10.dp),
                    label = {
                        Text(
                            text = honkaiState.honkaiImapackPath ?: "未搜索到",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    value = textValue,
                    onValueChange = {
                        textValue = it
                    },

                    )
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(end = 10.dp),
                    onClick = {
                        honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.SearchHonkaiImapackPath)
                    }) {
                    Text(text = "自动路径")
                }
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        if (textValue != "") {
                            honkaiViewModel.sendEvent(
                                HonkaiImapackViewModel.HonkaiImapackEvent.ChangeHonkaiImapackPath(
                                    textValue
                                )
                            )
                            textValue = ""
                        }
                    }) {
                    Text(text = "确认修改")
                }
            }
            UserAccountList(
                dataList = honkaiState.honkaiImapackAccountList,
                onAccountClick = {
                    honkaiViewModel.sendEvent(
                        HonkaiImapackViewModel.HonkaiImapackEvent.StartHonkaiImapackGame(
                            it as TieAccount,
                            honkaiState.honkaiImapackPath ?: ""
                        )
                    )
                }, onAddAccountClick = {
                    honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.ShowDialog)
                }, onDeleteAccountClick = {
                    honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.DeleteHonkaiImapackAccount(it as TieAccount))
                }
            )
        }
        if (honkaiState.showAddHonkaiImapackAccount) {
            AddAccountDialog(
                onCancelClick = {
                    honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.HideDialog)
                },
                onDismissRequest = {
                    honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.HideDialog)
                },
                onConfirmClick = {
                    honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.AddHonkaiImapackAccount(it))
                    honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.HideDialog)
                }
            )
        }

        if (honkaiState.showTip) {
            TipDialog(
                message = honkaiState.tipMessage,
                showCancel = false,
                onConfirmClick = { honkaiViewModel.sendEvent(HonkaiImapackViewModel.HonkaiImapackEvent.HideTip) },
            )
        }


    }


}