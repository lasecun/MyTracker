package com.itram.mytracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itram.mytracker.db.Run
import com.itram.mytracker.other.SortType
import com.itram.mytracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByMillis()
    private val runsSortedByAvg = mainRepository.getAllRunsSortedByAvgSpeed()

    private val _state = MutableStateFlow(RunState())
    val state: StateFlow<RunState> = _state

    init {
        viewModelScope.launch {
            runsSortedByDate.collect {
                _state.update { it.copy(runs = it.runs) }
            }
        }
    }

    fun onEvent(event: RunEvent) {
        when (event) {
            is RunEvent.SortData -> {
                sortRuns(event.sortBy)
            }
        }
    }

    private fun sortRuns(sortType: SortType) {
        viewModelScope.launch {
            when (sortType) {
                SortType.DATE -> runsSortedByDate.collect { byDate ->
                    _state.update { it.copy(runs = byDate) }
                }

                SortType.RUNNING_TIME -> runsSortedByTimeInMillis.collect { byTimeInMillis ->
                    _state.update { it.copy(runs = byTimeInMillis) }
                }

                SortType.AVG_SPEED -> runsSortedByAvg.collect { byAvg ->
                    _state.update { it.copy(runs = byAvg) }
                }

                SortType.DISTANCE -> runsSortedByDistance.collect { byDistance ->
                    _state.update { it.copy(runs = byDistance) }
                }

                SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.collect { byCaloriesBurned ->
                    _state.update { it.copy(runs = byCaloriesBurned) }
                }
            }
        }
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

}

data class RunState(
    val runs: List<Run> = emptyList()
)

sealed class RunEvent {
    data class SortData(val sortBy: SortType) : RunEvent()
}