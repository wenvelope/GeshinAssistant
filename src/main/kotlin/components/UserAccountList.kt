package components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bean.GenshinAccount
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme

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
    onAddAccountClick: () -> Unit = {},
    onDeleteAccountClick: (GenshinAccount) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(dataList) {
                if (it == dataList.first()) {
                    Spacer(Modifier.fillMaxWidth().height(10.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .clip(MaterialTheme.shapes.extraLarge),
                        headlineContent = {
                            Text(text = it.name)
                        },
                        trailingContent = {
                            Row {
                                Button(
                                    onClick = {
                                        onAccountClick.invoke(it)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("启动")
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        onDeleteAccountClick.invoke(it)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("删除")
                                }
                            }

                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        shadowElevation = 10.dp
                    )
                }
                Spacer(Modifier.fillMaxWidth().height(10.dp))
            }

        }

        ExtendedFloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(50.dp),
            onClick = {
                onAddAccountClick.invoke()
            },
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text("添加账号")
        }
    }
}