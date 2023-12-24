package viewModel

import BaseViewModelCore
import MutableContainer
import UiEvent
import UiState
import bean.TieAccount
import bean.TieAccountTable
import bean.TieGamePath
import bean.TieGamePathTable
import com.ctrip.sqllin.dsl.sql.X
import com.ctrip.sqllin.dsl.sql.clause.EQ
import com.ctrip.sqllin.dsl.sql.clause.SET
import com.ctrip.sqllin.dsl.sql.clause.WHERE
import com.ctrip.sqllin.dsl.sql.statement.SelectStatement
import com.wuhongru.jni.WRegistry
import dataBase.GenshinDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.IOException

class HonkaiImapackViewModel :
    BaseViewModelCore<HonkaiImapackViewModel.HonkaiImapackState, HonkaiImapackViewModel.HonkaiImapackEvent>() {
    data class HonkaiImapackState(
        val honkaiImapackPath: String? = null,
        val honkaiImapackAccountList: List<TieAccount> = emptyList(),
        val showAddHonkaiImapackAccount: Boolean = false,
        val showTip: Boolean = false,
        val tipMessage: String = "",
    ) : UiState

    sealed interface HonkaiImapackEvent : UiEvent {
        data class ChangeHonkaiImapackPath(val path: String) : HonkaiImapackEvent

        object SearchHonkaiImapackPath : HonkaiImapackEvent

        object RefreshHonkaiImapackAccount : HonkaiImapackEvent

        data class AddHonkaiImapackAccount(val name: String) : HonkaiImapackEvent

        data class DeleteHonkaiImapackAccount(val account: TieAccount) : HonkaiImapackEvent

        data class StartHonkaiImapackGame(val account: TieAccount, val path: String) : HonkaiImapackEvent

        object ShowDialog : HonkaiImapackEvent

        object HideDialog : HonkaiImapackEvent
        data class ShowTip(val tipMessage: String) : HonkaiImapackEvent
        object HideTip : HonkaiImapackEvent
    }

    override fun initialState(): HonkaiImapackState {
        lateinit var selectStatement: SelectStatement<TieAccount>
        GenshinDataBase.database {
            TieAccountTable { table ->
                selectStatement = table SELECT X
            }
        }
        lateinit var pathSelectStatement: SelectStatement<TieGamePath>
        GenshinDataBase.database {
            TieGamePathTable { table ->
                pathSelectStatement = table SELECT WHERE(id EQ 1)
            }
        }
        val path = if (pathSelectStatement.getResults().isEmpty()) {
            val path = WRegistry().searchTiePath()
            GenshinDataBase.database {
                TieGamePathTable { table ->
                    table INSERT TieGamePath(
                        id = 1,
                        value = path ?: ""
                    )
                }
            }
            path
        } else {
            pathSelectStatement.getResults().find { it.id == 1 }?.value
        }

        return HonkaiImapackState(
            honkaiImapackAccountList = selectStatement.getResults(),
            honkaiImapackPath = path
        )
    }

    override suspend fun reduce(container: MutableContainer<HonkaiImapackState, HonkaiImapackEvent>) {
        container.apply {
            uiEventFlow.collect {
                when (it) {
                    is HonkaiImapackEvent.ChangeHonkaiImapackPath -> {
                        updateState {
                            copy(honkaiImapackPath = it.path)
                        }
                        GenshinDataBase.database {
                            TieGamePathTable { table ->
                                table UPDATE SET {
                                    value = it.path
                                } WHERE (id EQ 1)
                            }
                        }
                    }

                    HonkaiImapackEvent.SearchHonkaiImapackPath -> {
                        val registry = WRegistry()
                        val path = registry.searchTiePath()
                        GenshinDataBase.database {
                            TieGamePathTable { table ->
                                table UPDATE SET {
                                    value = path ?: ""
                                } WHERE (id EQ 1)
                            }
                        }
                        updateState {
                            copy(honkaiImapackPath = path)
                        }
                    }

                    HonkaiImapackEvent.RefreshHonkaiImapackAccount -> {
                        lateinit var selectStatement: SelectStatement<TieAccount>
                        GenshinDataBase.database {
                            TieAccountTable { table ->
                                selectStatement = table SELECT X
                            }
                        }
                        updateState {
                            copy(honkaiImapackAccountList = selectStatement.getResults())
                        }
                    }

                    is HonkaiImapackEvent.AddHonkaiImapackAccount -> {
                        val registry = WRegistry()
                        val accountInfo = registry.getTieAccountInfoFromReg()
                        GenshinDataBase.database {
                            TieAccountTable { table ->
                                table INSERT TieAccount(
                                    value = accountInfo,
                                    name = it.name
                                )
                            }
                        }
                        sendEvent(HonkaiImapackEvent.RefreshHonkaiImapackAccount)
                    }

                    is HonkaiImapackEvent.DeleteHonkaiImapackAccount -> {
                        GenshinDataBase.database {
                            TieAccountTable { table ->
                                table DELETE WHERE(name EQ it.account.name)
                            }
                        }
                        sendEvent(HonkaiImapackEvent.RefreshHonkaiImapackAccount)
                    }

                    is HonkaiImapackEvent.StartHonkaiImapackGame -> {
                        WRegistry().apply {
                            setTieRegistryValue(WRegistry.TIE_KEY, it.account.value)
                        }

                        withContext(Dispatchers.IO) {
                            val processName = "StarRail.exe"
                            try {
                                WRegistry().terminateProcessByName(processName)
                                val pathToExe = it.path
                                val file = File(pathToExe)
                                if (!file.exists()) {
                                    //打开失败
                                    sendEvent(HonkaiImapackEvent.ShowTip("打开失败"))
                                    throw IOException("File does not exist: $pathToExe")
                                } else {
                                    Desktop.getDesktop().open(file)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                sendEvent(HonkaiImapackEvent.ShowTip("打开失败"))
                            }
                        }
                    }

                    HonkaiImapackEvent.ShowDialog -> {
                        updateState {
                            copy(showAddHonkaiImapackAccount = true)
                        }
                    }

                    HonkaiImapackEvent.HideDialog -> {
                        updateState {
                            copy(showAddHonkaiImapackAccount = false)
                        }
                    }

                    HonkaiImapackEvent.HideTip -> {
                        updateState {
                            copy(showTip = false)
                        }
                    }

                    is HonkaiImapackEvent.ShowTip -> {
                        updateState {
                            copy(showTip = true, tipMessage = it.tipMessage)
                        }
                    }
                }
            }
        }
    }
}