package com.vulnscanner.sample.di

import android.content.pm.PackageManager
import com.vulnscanner.analyzer.data.repository.ScanRepository
import com.vulnscanner.analyzer.domain.usecase.StartScanUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import javax.inject.Singleton

/**
 * Hilt DI module — wires the clean architecture layers together.
 *
 * Dependency graph:
 *   PackageManager → ScanRepository → StartScanUseCase → ScanViewModel
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePackageManager(
        @ApplicationContext context: Context
    ): PackageManager = context.packageManager

    @Provides
    @Singleton
    fun provideScanRepository(
        packageManager: PackageManager
    ): ScanRepository = ScanRepository(packageManager)

    @Provides
    fun provideStartScanUseCase(
        repository: ScanRepository
    ): StartScanUseCase = StartScanUseCase(repository)
}
