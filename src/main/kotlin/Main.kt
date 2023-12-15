import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import components.AddAccountDialog
import components.GenshinPage
import components.TipDialog
import viewModel.AppViewModel


@Composable
@Preview
fun App() {
    val appViewModel = rememberSaveable {
        AppViewModel()
    }
    val appState by appViewModel.container.uiStateFlow.collectAsState()
    GenshinAssistantTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                LeftBanner(appViewModel = appViewModel, gameItemList)
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                RightBanner(appViewModel = appViewModel)
            }
            if (appState.showDialog) {
                AddAccountDialog(appViewModel)
            }
            if (appState.showTip) {
                TipDialog(
                    message = appState.tipMessage,
                    onDismissRequest = {
                        appViewModel.sendEvent(AppViewModel.AssistantEvent.HideTip)
                    },
                    onConfirmClick = { appState.onTipDialogConfirmClick?.invoke() },

                    showCancel = appState.onTipDialogConfirmClick != null
                )
            }
        }
    }
}


@Composable
fun RightBanner(appViewModel: AppViewModel) {
    val appState by appViewModel.container.uiStateFlow.collectAsState()
    when (appState.rightPage) {
        AppViewModel.RightPage.GenshinPage -> {
            GenshinPage(appViewModel)
        }

        AppViewModel.RightPage.HonkaiImapackPage -> {

        }
    }
}


data class GameItem(
    val pageName: AppViewModel.RightPage,
    val contentDescription: String? = null,
    val iconPath: String
)

val gameItemList = listOf(
    GameItem(pageName = AppViewModel.RightPage.GenshinPage, iconPath = "icon_genshin.png"),
    GameItem(pageName = AppViewModel.RightPage.HonkaiImapackPage, iconPath = "icon_honkai.png")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeftBanner(
    appViewModel: AppViewModel,
    gameItemList: List<GameItem>
) {
    val appState by appViewModel.container.uiStateFlow.collectAsState()
    Column(
        modifier = Modifier.width(110.dp).fillMaxHeight()
    ) {
        gameItemList.forEach { gameItem ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                onClick = {
                    appViewModel.sendEvent(AppViewModel.AssistantEvent.NavigationToPage(gameItem.pageName))
                },
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (appState.rightPage == gameItem.pageName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Image(
                    painter = painterResource(gameItem.iconPath),
                    contentScale = ContentScale.Crop,
                    contentDescription = gameItem.contentDescription,
                    modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally).clip(CircleShape)
                )
            }
            if (gameItem != gameItemList.last()) {
                Divider(modifier = Modifier.fillMaxWidth().height(1.dp))
            }
        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "MiHoYo Assistant", undecorated = false) {
        App()
    }
}
