package com.vulnscanner.sample.di;

import com.vulnscanner.analyzer.data.repository.ScanRepository;
import com.vulnscanner.analyzer.domain.usecase.StartScanUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideStartScanUseCaseFactory implements Factory<StartScanUseCase> {
  private final Provider<ScanRepository> repositoryProvider;

  public AppModule_ProvideStartScanUseCaseFactory(Provider<ScanRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public StartScanUseCase get() {
    return provideStartScanUseCase(repositoryProvider.get());
  }

  public static AppModule_ProvideStartScanUseCaseFactory create(
      Provider<ScanRepository> repositoryProvider) {
    return new AppModule_ProvideStartScanUseCaseFactory(repositoryProvider);
  }

  public static StartScanUseCase provideStartScanUseCase(ScanRepository repository) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideStartScanUseCase(repository));
  }
}
