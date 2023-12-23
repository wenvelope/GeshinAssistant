package viewModel

import BaseViewModelCore
import MutableContainer
import UiEvent
import UiState
import com.wuhongru.jni.WRegistry

class GenshinViewModel : BaseViewModelCore<GenshinViewModel.GenShinState, GenshinViewModel.GenshinEvent>() {
    data class GenShinState(
        val genshinPath: String? = null
    ) : UiState

    sealed interface GenshinEvent : UiEvent {
        data class ChangeGenShinPath(val path: String) : GenshinEvent

        object SearchGenShinPath : GenshinEvent
    }

    override fun initialState(): GenShinState {
        val registry = WRegistry()
        return GenShinState(genshinPath = registry.searchYuanShenPath())
    }

    override suspend fun reduce(container: MutableContainer<GenShinState, GenshinEvent>) {
        container.apply {
            uiEventFlow.collect {
                when (it) {
                    GenshinEvent.SearchGenShinPath -> {
                        val registry = WRegistry()
                        val path = registry.searchYuanShenPath()
                        updateState {
                            copy(genshinPath = path)
                        }
                    }

                    is GenshinEvent.ChangeGenShinPath -> {
                        updateState {
                            copy(genshinPath = it.path)
                        }
                    }
                }
            }
        }
    }


}