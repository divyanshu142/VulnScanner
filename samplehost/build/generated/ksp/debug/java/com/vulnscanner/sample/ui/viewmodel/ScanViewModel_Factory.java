package com.vulnscanner.sample.ui.viewmodel;

import com.vulnscanner.analyzer.domain.usecase.StartScanUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ScanViewModel_Factory implements Factory<ScanViewModel> {
  private final Provider<StartScanUseCase> startScanUseCaseProvider;

  public ScanViewModel_Factory(Provider<StartScanUseCase> startScanUseCaseProvider) {
    this.startScanUseCaseProvider = startScanUseCaseProvider;
  }

  @Override
  public ScanViewModel get() {
    return newInstance(startScanUseCaseProvider.get());
  }

  public static ScanViewModel_Factory create(Provider<StartScanUseCase> startScanUseCaseProvider) {
    return new ScanViewModel_Factory(startScanUseCaseProvider);
  }

  public static ScanViewModel newInstance(StartScanUseCase startScanUseCase) {
    return new ScanViewModel(startScanUseCase);
  }
}
