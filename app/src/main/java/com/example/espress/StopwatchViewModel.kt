package com.example.espress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StopwatchState {
    Idle {
        override fun play() = Running
    },
    Running {
        override fun play() = Paused
    },
    Paused {
        override fun play() = Running
    };

    open fun play(): StopwatchState = Running
    open fun reset(): StopwatchState = Idle
//    abstract fun reset(): StopwatchState

}

fun Long.formatTime(): String {
//    val minutes = (this % 36000) / 600
    val remainingSeconds = this / 10
    val deciSeconds = this % 10
    return String.format("%d,%01ds", remainingSeconds, deciSeconds)
}

class StopwatchViewModel : ViewModel() {
    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    private val _state = MutableStateFlow(StopwatchState.Idle)
    val state = _state.asStateFlow()

    inner class Stopwatch(
        val timer: StateFlow<Long>,
        val state: StateFlow<StopwatchState>
    ) {
        fun play() {
            when (_state.value) {
                StopwatchState.Idle -> startTimer()
                StopwatchState.Running -> pauseTimer()
                StopwatchState.Paused -> startTimer()
            }
            _state.update { it.play() }
        }

        fun reset() {
            stopTimer()
            _state.update { it.reset() }
        }

        fun set(timer: Long) {
            _timer.value = timer
        }
    }

    val stopwatch = Stopwatch(_timer.asStateFlow(), _state.asStateFlow())

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(100)
                _timer.value++
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun stopTimer() {
        _timer.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}