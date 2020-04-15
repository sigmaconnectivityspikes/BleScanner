package se.sigmaconnectivity.blescanner.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.OnGetBridgeDataUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.OnSetBridgeDataUseCase
import se.sigmaconnectivity.blescanner.ui.MainViewModel
import se.sigmaconnectivity.blescanner.ui.common.PushNotificationManager
import se.sigmaconnectivity.blescanner.ui.home.HomeViewModel

val appModule = module {
    factory { PushNotificationManager(get()) }

    factory { OnGetBridgeDataUseCase() }
    factory { OnSetBridgeDataUseCase(get()) }

    factory<PostExecutionThread> { se.sigmaconnectivity.blescanner.executor.PostExecutionThread() }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { MainViewModel() }
}