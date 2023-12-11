package viewModel

import BaseViewModelCore
import MutableContainer
import UiEvent
import UiState
import bean.GenshinAccount
import bean.GenshinAccountTable
import com.ctrip.sqllin.dsl.sql.X
import com.ctrip.sqllin.dsl.sql.statement.SelectStatement
import com.wuhongru.jini.WRegistry
import dataBase.GenshinDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.IOException

class AppViewModel : BaseViewModelCore<AppViewModel.AssistantState, AppViewModel.AssistantEvent>() {
    data class AssistantState(
        val rightPage: RightPage = RightPage.GenshinPage,
        val genshinAccountList: List<GenshinAccount> = emptyList(),
        val showDialog: Boolean = false
    ) : UiState

    enum class RightPage {
        GenshinPage,
        HonkaiImapackPage
    }

    sealed interface AssistantEvent : UiEvent {
        data class NavigationToPage(val page: RightPage) : AssistantEvent
        data class AddGenshinAccount(val name: String) : AssistantEvent

        data class StartMIHOYOGame(val account: GenshinAccount) : AssistantEvent
        object RefreshGenshinAccountList : AssistantEvent

        object ShowDialog : AssistantEvent

        object HideDialog : AssistantEvent


    }

    override fun initialState(): AssistantState {
        lateinit var selectStatement: SelectStatement<GenshinAccount>
        GenshinDataBase.database {
            GenshinAccountTable { table ->
                selectStatement = table SELECT X
            }
        }
        return AssistantState(genshinAccountList = selectStatement.getResults())
    }

    override suspend fun reduce(container: MutableContainer<AssistantState, AssistantEvent>) {
        container.apply {
            uiEventFlow.collect { it ->
                when (it) {
                    is AssistantEvent.NavigationToPage -> {
                        updateState {
                            copy(rightPage = it.page)
                        }
                    }

                    is AssistantEvent.StartMIHOYOGame -> {
                        WRegistry().apply {
                            setRegistryValue(WRegistry.key1, it.account.value1)
                            setRegistryValue(WRegistry.key2, it.account.value2)
                        }
                        try {
                            withContext(Dispatchers.IO) {
                                val pathToExe = "E:\\Genshin Impact\\Genshin Impact Game\\YuanShen.exe"
                                val file = File(pathToExe)
                                Desktop.getDesktop().open(file)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    is AssistantEvent.AddGenshinAccount -> {
                        val registry = WRegistry()
                        val map = registry.getAccountInfoFromReg()
                        GenshinDataBase.database {
                            GenshinAccountTable { table ->
                                table INSERT GenshinAccount(
                                    value1 = map[WRegistry.key1] ?: "",
                                    value2 = map[WRegistry.key2] ?: "",
                                    name = it.name
                                )
                            }
                        }
                        sendEvent(AssistantEvent.RefreshGenshinAccountList)

                    }

                    AssistantEvent.RefreshGenshinAccountList -> {
                        lateinit var selectStatement: SelectStatement<GenshinAccount>
                        GenshinDataBase.database {
                            GenshinAccountTable { table ->
                                selectStatement = table SELECT X
                            }
                        }
                        updateState {
                            copy(genshinAccountList = selectStatement.getResults())
                        }
                    }

                    AssistantEvent.HideDialog -> {
                        updateState {
                            copy(showDialog = false)
                        }
                    }

                    AssistantEvent.ShowDialog -> {
                        updateState {
                            copy(showDialog = true)
                        }
                    }
                }
            }
        }
    }
}