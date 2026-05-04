package com.vulnscanner.analyzer.domain.usecase

import com.vulnscanner.analyzer.data.model.ScanState
import com.vulnscanner.analyzer.data.repository.ScanRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: StartScan
 *
 * Sits between the ViewModel and the Repository.
 * In Clean Architecture, the use case encapsulates one business action —
 * here: triggering the full vulnerability scan and streaming progress.
 *
 * The ViewModel depends on this abstraction, not the repository directly.
 */
class StartScanUseCase(private val repository: ScanRepository) {

    /**
     * Invoke operator allows calling the use case as a function:
     *   val flow = startScanUseCase()
     */
    operator fun invoke(): Flow<ScanState> = repository.startScan()
}
