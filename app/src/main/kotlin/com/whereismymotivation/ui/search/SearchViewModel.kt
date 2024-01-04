package com.whereismymotivation.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.whereismymotivation.data.model.Content
import com.whereismymotivation.data.model.UniversalSearchResult
import com.whereismymotivation.data.repository.SearchRepository
import com.whereismymotivation.ui.base.BaseViewModel
import com.whereismymotivation.ui.common.progress.Loader
import com.whereismymotivation.ui.common.snackbar.Messenger
import com.whereismymotivation.ui.navigation.Destination
import com.whereismymotivation.ui.navigation.NavTarget
import com.whereismymotivation.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    loader: Loader,
    messenger: Messenger,
    searchRepository: SearchRepository,
    savedStateHandle: SavedStateHandle,
    private val navigator: Navigator
) : BaseViewModel(loader, messenger) {

    companion object {
        const val TAG = "SearchViewModel"
        private const val SEARCH_MODE_SAVED_STATE_KEY = "searchMode"
    }

    private val searchMode = SearchMode.valueOf(
        savedStateHandle.get<String>(SEARCH_MODE_SAVED_STATE_KEY) ?: SearchMode.UNIVERSAL.name
    )

    private val _results = MutableStateFlow<List<UniversalSearchResult>>(emptyList())
    private val _query = MutableStateFlow("")

    val results = _results.asStateFlow()
    val query = _query.asStateFlow()

    private val queryFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        BufferOverflow.DROP_OLDEST
    )

    init {
        queryFlow.debounce(500)
            .distinctUntilChanged()
            .mapLatest {
                if (it.isEmpty() || it.isBlank())
                    return@mapLatest flowOf(emptyList<UniversalSearchResult>())
                return@mapLatest when (searchMode) {
                    SearchMode.UNIVERSAL -> searchRepository.fetchSearchResults(it)
                    SearchMode.MENTOR -> searchRepository.fetchMentorSearchResults(it)
                    SearchMode.TOPIC -> searchRepository.fetchTopicSearchResults(it)
                }
            }
            .onEach {
                it.collect { result ->
                    _results.value = result
                }
            }
            .catch { loader.stop() }
            .launchIn(viewModelScope)
    }

    fun search(query: String) {
        _query.value = query
        queryFlow.tryEmit(query)
    }

    fun selectResult(result: UniversalSearchResult) {
        when (result.category) {
            Content.Category.AUDIO -> {}
            Content.Category.VIDEO -> {}
            Content.Category.IMAGE -> {}
            Content.Category.FACEBOOK_VIDEO -> {}
            Content.Category.ARTICLE -> {}
            Content.Category.QUOTE -> {}
            Content.Category.YOUTUBE -> {
                navigator.navigateTo(NavTarget(Destination.YouTube.createRoute(result.id)))
            }

            Content.Category.MENTOR_INFO -> {
                navigator.navigateTo(NavTarget(Destination.Mentor.createRoute(result.id)))
            }

            Content.Category.TOPIC_INFO -> {
                navigator.navigateTo(NavTarget(Destination.Topic.createRoute(result.id)))
            }
        }
    }

}