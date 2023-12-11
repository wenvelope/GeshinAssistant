import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import components.UserAccountList
import viewModel.AppViewModel


@Composable
@Preview
fun App() {
    val appViewModel = rememberSaveable {
        AppViewModel()
    }
    val appState by appViewModel.container.uiStateFlow.collectAsState()
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                LeftBanner(appViewModel = appViewModel)
                RightBanner(appViewModel = appViewModel)
            }
            if (appState.showDialog) {
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
                            Text(text = "添加MIHOYO账号", modifier = Modifier.padding(top = 20.dp, start = 20.dp))

                            TextField(
                                modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 10.dp)
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
                                    modifier = Modifier.size(80.dp, 60.dp).padding(top = 10.dp),
                                    onClick = {
                                        appViewModel.sendEvent(AppViewModel.AssistantEvent.HideDialog)
                                    }
                                ) {
                                    Text(text = "取消")
                                }
                                Button(
                                    modifier = Modifier.size(100.dp, 60.dp).padding(top = 10.dp).padding(horizontal = 10.dp),
                                    onClick = {
                                        appViewModel.sendEvent(AppViewModel.AssistantEvent.AddGenshinAccount(text))
                                        appViewModel.sendEvent(AppViewModel.AssistantEvent.HideDialog)
                                    }
                                ) {
                                    Text(text = "确定")
                                }
                            }
                        }

                    }
                }

            }
        }
    }
}

@Composable
fun RightBanner(appViewModel: AppViewModel) {
    val appState by appViewModel.container.uiStateFlow.collectAsState()
    when (appState.rightPage) {
        AppViewModel.RightPage.GenshinPage -> {
            UserAccountList(
                dataList = appState.genshinAccountList,
                onAccountClick = {
                    appViewModel.sendEvent(AppViewModel.AssistantEvent.StartMIHOYOGame(it))
                }, onAddAccountClick = {
                    appViewModel.sendEvent(AppViewModel.AssistantEvent.ShowDialog)
                })
        }

        AppViewModel.RightPage.HonkaiImapackPage -> {

        }
    }

}

@Composable
fun LeftBanner(appViewModel: AppViewModel) {
    Column(
        modifier = Modifier.fillMaxHeight().width(50.dp)
    ) {
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "MiHoYO Assistant", undecorated = false) {
        App()
    }
}
