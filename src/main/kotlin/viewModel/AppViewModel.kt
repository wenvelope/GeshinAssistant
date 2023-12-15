package viewModel

import BaseViewModelCore
import MutableContainer
import UiEvent
import UiState
import bean.GenshinAccount
import bean.GenshinAccountTable
import com.ctrip.sqllin.dsl.sql.X
import com.ctrip.sqllin.dsl.sql.clause.EQ
import com.ctrip.sqllin.dsl.sql.clause.WHERE
import com.ctrip.sqllin.dsl.sql.statement.SelectStatement
import com.wuhongru.jni.WRegistry
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
        val showDialog: Boolean = false,
        val showTip: Boolean = false,
        val tipMessage: String = "",
        val onTipDialogConfirmClick: (() -> Unit)? = null
    ) : UiState

    enum class RightPage {
        GenshinPage,
        HonkaiImapackPage
    }

    sealed interface AssistantEvent : UiEvent {
        data class NavigationToPage(val page: RightPage) : AssistantEvent
        data class AddGenshinAccount(val name: String) : AssistantEvent

        data class StartMIHOYOGame(val account: GenshinAccount, val path: String) : AssistantEvent
        object RefreshGenshinAccountList : AssistantEvent

        object ShowDialog : AssistantEvent

        object HideDialog : AssistantEvent

        /**
         * @author wu
         *
         * @param tipMessage 提示信息
         * @param onConfirmClick 默认为空 仅仅作为提示信息使用 如果不为空则会显示取消按钮 并且传入确认之后的逻辑
         */
        data class ShowTip(val tipMessage: String, val onConfirmClick: (() -> Unit)? = null) : AssistantEvent
        data class DeleteAccount(val account: GenshinAccount) : AssistantEvent

        object HideTip : AssistantEvent


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

                        withContext(Dispatchers.IO) {
                            val processName = "YuanShen.exe"
                            try {
                                WRegistry().terminateProcessByName(processName)
                                val pathToExe = it.path
                                val file = File(pathToExe)
                                if (!file.exists()) {
                                    sendEvent(
                                        action = AssistantEvent.ShowTip(
                                            tipMessage = "打开失败"
                                        )
                                    )
                                    throw IOException("File does not exist: $pathToExe")
                                } else {
                                    Desktop.getDesktop().open(file)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                sendEvent(
                                    action = AssistantEvent.ShowTip(
                                        tipMessage = "打开失败"
                                    )
                                )
                            }
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

                    AssistantEvent.HideTip -> {
                        updateState {
                            copy(showTip = false, tipMessage = "", onTipDialogConfirmClick = null)
                        }
                    }


                    is AssistantEvent.ShowTip -> {
                        updateState {
                            copy(
                                showTip = true,
                                tipMessage = it.tipMessage,
                                onTipDialogConfirmClick = it.onConfirmClick
                            )
                        }
                    }

                    is AssistantEvent.DeleteAccount -> {
                        GenshinDataBase.database {
                            GenshinAccountTable { table ->
                                table DELETE WHERE(name EQ it.account.name)
                            }
                        }
                        sendEvent(AssistantEvent.RefreshGenshinAccountList)
                    }
                }
            }
        }
    }
}