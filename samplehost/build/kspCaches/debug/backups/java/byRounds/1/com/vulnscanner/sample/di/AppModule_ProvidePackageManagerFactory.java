package com.vulnscanner.sample.di;

import android.content.Context;
import android.content.pm.PackageManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvidePackageManagerFactory implements Factory<PackageManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvidePackageManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PackageManager get() {
    return providePackageManager(contextProvider.get());
  }

  public static AppModule_ProvidePackageManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvidePackageManagerFactory(contextProvider);
  }

  public static PackageManager providePackageManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePackageManager(context));
  }
}
