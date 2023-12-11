package components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bean.GenshinAccount

@Preview
@Composable
fun UserAccountListPreview() {
    MaterialTheme {
        UserAccountList(
            dataList = listOf(
                GenshinAccount("大号", "32", "323"),
                GenshinAccount("大号", "32", "323"),
                GenshinAccount("大号", "32", "323"),
                GenshinAccount("大号", "32", "323")

            )
        )
    }

}

@Composable
fun UserAccountList(
    dataList: List<GenshinAccount>,
    onAccountClick: (GenshinAccount) -> Unit = {},
    onAddAccountClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            dataList.forEach {
                if (it == dataList.first()) {
                    Spacer(Modifier.fillMaxWidth().height(10.dp))
                }
                ListItem(
                    modifier = Modifier.padding(horizontal = 5.dp).clip(
                        MaterialTheme.shapes.extraLarge
                    ),
                    headlineContent = {
                        Text(text = it.name)
                    },
                    trailingContent = {
                        Button(
                            onClick = {
                                onAccountClick.invoke(it)
                            },
                        ) {
                            Text("启动")
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    )
                )
                Spacer(Modifier.fillMaxWidth().height(10.dp))

            }
        }

        ExtendedFloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(50.dp),
            onClick = {
                onAddAccountClick.invoke()
            }
        ) {
            Text("添加账号")
        }
    }

}