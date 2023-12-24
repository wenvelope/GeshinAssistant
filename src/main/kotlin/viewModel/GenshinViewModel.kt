package viewModel

import BaseViewModelCore
import MutableContainer
import UiEvent
import UiState
import bean.GenshinGamePath
import bean.GenshinGamePathTable
import com.ctrip.sqllin.dsl.sql.clause.EQ
import com.ctrip.sqllin.dsl.sql.clause.SET
import com.ctrip.sqllin.dsl.sql.clause.WHERE
import com.ctrip.sqllin.dsl.sql.statement.SelectStatement
import com.wuhongru.jni.WRegistry
import dataBase.GenshinDataBase

class GenshinViewModel : BaseViewModelCore<GenshinViewModel.GenShinState, GenshinViewModel.GenshinEvent>() {
    data class GenShinState(
        val genshinPath: String? = null
    ) : UiState

    sealed interface GenshinEvent : UiEvent {
        data class ChangeGenShinPath(val path: String) : GenshinEvent

        object SearchGenShinPath : GenshinEvent
    }

    override fun initialState(): GenShinState {
        lateinit var pathSelectStatement: SelectStatement<GenshinGamePath>
        GenshinDataBase.database {
            GenshinGamePathTable {
                pathSelectStatement = it SELECT WHERE(it.id EQ 1)
            }
        }
        val path = if (pathSelectStatement.getResults().isEmpty()) {
            val path = WRegistry().searchYuanShenPath()
            GenshinDataBase.database {
                GenshinGamePathTable {
                    it INSERT GenshinGamePath(value = path ?: "", id = 1)
                }
            }
            path
        } else {
            pathSelectStatement.getResults().find { it.id == 1 }?.value
        }
        return GenShinState(genshinPath = path)
    }

    override suspend fun reduce(container: MutableContainer<GenShinState, GenshinEvent>) {
        container.apply {
            uiEventFlow.collect {
                when (it) {
                    GenshinEvent.SearchGenShinPath -> {
                        val registry = WRegistry()
                        val path = registry.searchYuanShenPath()
                        GenshinDataBase.database {
                            GenshinGamePathTable { table ->
                                table UPDATE SET { value = path ?: "" } WHERE (id EQ 1)
                            }
                        }
                        updateState {
                            copy(genshinPath = path)
                        }
                    }

                    is GenshinEvent.ChangeGenShinPath -> {
                        updateState {
                            copy(genshinPath = it.path)
                        }
                        GenshinDataBase.database{
                            GenshinGamePathTable { table ->
                                table UPDATE SET { value = it.path } WHERE (id EQ 1)
                            }
                        }
                    }
                }
            }
        }
    }


}