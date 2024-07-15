package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bean.GenshinAccount
import components.AddAccountDialog
import components.UserAccountList
import viewModel.AppViewModel
import viewModel.GenshinViewModel

@Composable
fun GenshinPage(appViewModel: AppViewModel) {
    val appState by appViewModel.container.uiStateFlow.collectAsState()
    val genshinViewModel = remember { GenshinViewModel() }
    val genshinState by genshinViewModel.container.uiStateFlow.collectAsState()
    Box {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().height(70.dp)
            ) {
                var textValue by remember { mutableStateOf("") }
                TextField(
                    modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 10.dp),
                    label = {
                        Text(
                            text = genshinState.genshinPath ?: "未搜索到",
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
                        genshinViewModel.sendEvent(GenshinViewModel.GenshinEvent.SearchGenShinPath)
                    }) {
                    Text(text = "自动路径")
                }
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        if (textValue != "") {
                            genshinViewModel.sendEvent(GenshinViewModel.GenshinEvent.ChangeGenShinPath(textValue))
                            textValue = ""
                        }
                    }) {
                    Text(text = "确认修改")
                }
            }
            UserAccountList(
                dataList = appState.genshinAccountList,
                onAccountClick = {
                    appViewModel.sendEvent(AppViewModel.AssistantEvent.StartMIHOYOGame(it as GenshinAccount, genshinState.genshinPath ?: ""))
                }, onAddAccountClick = {
                    appViewModel.sendEvent(AppViewModel.AssistantEvent.ShowDialog)
                }, onDeleteAccountClick = {
                    appViewModel.sendEvent(
                        AppViewModel.AssistantEvent.ShowTip(
                            tipMessage = "是否删除",
                            onConfirmClick = {
                                appViewModel.sendEvent(AppViewModel.AssistantEvent.DeleteGenshinAccount(it as GenshinAccount))
                            })
                    )
                },
                onChangeNameClick = {
                    genshinViewModel.sendEvent(GenshinViewModel.GenshinEvent.ShowChangeNameDialog(it as GenshinAccount))
                }
            )
        }
        if (genshinState.showChangeNameDialog) {
            AddAccountDialog(
                titleName = genshinState.dialogTitle,
                onCancelClick = {
                    genshinViewModel.sendEvent(GenshinViewModel.GenshinEvent.HideAddAccountDialog)
                },
                onDismissRequest = {
                    genshinViewModel.sendEvent(GenshinViewModel.GenshinEvent.HideAddAccountDialog)
                },
                onConfirmClick = {
                    when (genshinState.dialogTitle) {
                        "添加MIHOYO账号" -> {
                            appViewModel.sendEvent(AppViewModel.AssistantEvent.AddGenshinAccount(it))
                        }

                        "修改账号昵称" -> {
                            genshinViewModel.sendEvent(GenshinViewModel.GenshinEvent.ChangeGenShinAccountName(genshinState.selectedAccount?.name ?: "", it,onSuccess = {
                                appViewModel.sendEvent(AppViewModel.AssistantEvent.RefreshGenshinAccountList)
                            }))
                        }
                    }
                }
            )
        }
    }


}
