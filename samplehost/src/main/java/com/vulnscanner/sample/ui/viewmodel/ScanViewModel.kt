package com.vulnscanner.sample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vulnscanner.analyzer.data.model.ScanState
import com.vulnscanner.analyzer.domain.usecase.StartScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ScanViewModel — MVVM ViewModel
 *
 * - Collects the Flow<ScanState> from the use case
 * - Exposes it as StateFlow<ScanState> for Compose UI to observe
 * - viewModelScope ensures coroutines are cancelled on ViewModel death
 *   (survives rotation, no memory leaks)
 *
 * UI layer: scanState.collectAsStateWithLifecycle()
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val startScanUseCase: StartScanUseCase
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    // Track expanded cards in RecyclerView/LazyColumn
    private val _expandedPackages = MutableStateFlow<Set<String>>(emptySet())
    val expandedPackages: StateFlow<Set<String>> = _expandedPackages.asStateFlow()

    /**
     * Launches the scan pipeline.
     * viewModelScope.launch → tied to ViewModel lifecycle (safe from rotation).
     * Flow is collected on Main dispatcher (emits go to UI).
     */
    fun startScan() {
        viewModelScope.launch {
            startScanUseCase()
                .catch { e ->
                    _scanState.value = ScanState.Error(e.message ?: "Unknown error")
                }
                .collect { state ->
                    _scanState.value = state
                }
        }
    }

    fun toggleExpanded(packageName: String) {
        val current = _expandedPackages.value.toMutableSet()
        if (packageName in current) current.remove(packageName) else current.add(packageName)
        _expandedPackages.value = current
    }

    fun resetScan() {
        _scanState.value = ScanState.Idle
        _expandedPackages.value = emptySet()
    }
}
