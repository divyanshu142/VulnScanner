package com.vulnscanner.sample.di;

import android.content.pm.PackageManager;
import com.vulnscanner.analyzer.data.repository.ScanRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideScanRepositoryFactory implements Factory<ScanRepository> {
  private final Provider<PackageManager> packageManagerProvider;

  public AppModule_ProvideScanRepositoryFactory(Provider<PackageManager> packageManagerProvider) {
    this.packageManagerProvider = packageManagerProvider;
  }

  @Override
  public ScanRepository get() {
    return provideScanRepository(packageManagerProvider.get());
  }

  public static AppModule_ProvideScanRepositoryFactory create(
      Provider<PackageManager> packageManagerProvider) {
    return new AppModule_ProvideScanRepositoryFactory(packageManagerProvider);
  }

  public static ScanRepository provideScanRepository(PackageManager packageManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideScanRepository(packageManager));
  }
}
