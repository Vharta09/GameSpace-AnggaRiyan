package com.example.gamespace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamespace.AppEntry
import com.example.gamespace.AppRepository
import com.example.gamespace.GameController
import com.example.gamespace.GameMonitorService
import com.example.gamespace.GameSettings
import com.example.gamespace.PerfMonitor
import com.example.gamespace.Perms
import com.example.gamespace.Prefs
import com.example.gamespace.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PermState(
    val usage: Boolean = false,
    val overlay: Boolean = false,
    val dnd: Boolean = false,
    val write: Boolean = false,
    val accessibility: Boolean = false
) {
    val essential get() = usage && overlay
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val prefs = Prefs(app)
    private val controller = GameController(app)

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private val _pinned = MutableStateFlow(prefs.pinned)
    val pinned: StateFlow<Set<String>> = _pinned.asStateFlow()

    private val _hidden = MutableStateFlow(prefs.hidden)
    val hidden: StateFlow<Set<String>> = _hidden.asStateFlow()

    private val _playTime = MutableStateFlow<Map<String, Long>>(emptyMap())
    val playTime: StateFlow<Map<String, Long>> = _playTime.asStateFlow()

    private val _stats = MutableStateFlow<Stats?>(null)
    val stats: StateFlow<Stats?> = _stats.asStateFlow()

    private val _perms = MutableStateFlow(PermState())
    val perms: StateFlow<PermState> = _perms.asStateFlow()

    private val _serviceOn = MutableStateFlow(prefs.serviceEnabled)
    val serviceOn: StateFlow<Boolean> = _serviceOn.asStateFlow()

    init {
        refreshApps()
        viewModelScope.launch {
            while (true) {
                _stats.value = withContext(Dispatchers.Default) { PerfMonitor.read(getApplication()) }
                delay(2000)
            }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _apps.value = withContext(Dispatchers.IO) { AppRepository.load(getApplication()) }
            refreshPlayTime()
        }
    }

    fun refreshPlayTime() {
        viewModelScope.launch {
            _playTime.value = withContext(Dispatchers.IO) {
                if (Perms.usage(getApplication())) AppRepository.playTimeToday(getApplication())
                else emptyMap()
            }
        }
    }

    fun refreshPerms() {
        val c = getApplication<Application>()
        _perms.value = PermState(
            usage = Perms.usage(c),
            overlay = Perms.overlay(c),
            dnd = Perms.dnd(c),
            write = Perms.writeSettings(c),
            accessibility = Perms.accessibility(c)
        )
        _serviceOn.value = prefs.serviceEnabled
        refreshPlayTime()
    }

    fun setService(on: Boolean) {
        val c = getApplication<Application>()
        if (on) GameMonitorService.start(c) else GameMonitorService.stop(c)
        _serviceOn.value = on
    }

    /** Game aktif = ditandai otomatis (kategori game) atau disematkan, dan tidak disembunyikan. */
    fun isGame(a: AppEntry) = (a.isGame || a.pkg in _pinned.value) && a.pkg !in _hidden.value

    fun addGame(pkg: String) {
        _pinned.value = _pinned.value + pkg
        _hidden.value = _hidden.value - pkg
        prefs.pinned = _pinned.value
        prefs.hidden = _hidden.value
    }

    fun removeGame(pkg: String) {
        _pinned.value = _pinned.value - pkg
        _hidden.value = _hidden.value + pkg
        prefs.pinned = _pinned.value
        prefs.hidden = _hidden.value
    }

    fun settings(pkg: String) = prefs.settings(pkg)

    fun save(pkg: String, s: GameSettings) = prefs.save(pkg, s)

    fun cleanNow() {
        viewModelScope.launch(Dispatchers.IO) { controller.cleanMemory(null) }
    }
}
