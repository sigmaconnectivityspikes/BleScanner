package se.sigmaconnectivity.blescanner.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCaseImpl
import se.sigmaconnectivity.blescanner.domain.usecase.GetHumanReadableUserIdUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.GetUserIdHashUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.HasUserHadContactWithInfectedUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.NotifyInfectionUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.TrackHasUserHadContactWithInfectedUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.TrackInfectionsUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.UserUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.UserUseCaseImpl
import se.sigmaconnectivity.blescanner.domain.usecase.device.AdvertiseTxUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.AdvertiseUidUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.ScanBleDevicesUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.SubscribeForBluetoothStatusUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.device.SubscribeForLocationStatusUseCase
import se.sigmaconnectivity.blescanner.service.ScanResultsObserver
import se.sigmaconnectivity.blescanner.ui.MainViewModel
import se.sigmaconnectivity.blescanner.ui.help.HelpViewModel
import se.sigmaconnectivity.blescanner.ui.home.HomeViewModel

val appModule = module {
    single { ScanResultsObserver( get()) }

    factory<PostExecutionThread> { se.sigmaconnectivity.blescanner.executor.PostExecutionThread() }
    factory<ContactUseCase> { ContactUseCaseImpl(get(), get()) }
    factory<UserUseCase> { UserUseCaseImpl(get(), get()) }
    factory { TrackInfectionsUseCase(get(), get()) }
    factory { NotifyInfectionUseCase(get(), get()) }
    factory { GetUserIdHashUseCase(get(), get()) }
    factory { GetHumanReadableUserIdUseCase(get(), get()) }
    factory { HasUserHadContactWithInfectedUseCase(get(), get()) }
    factory { TrackHasUserHadContactWithInfectedUseCase(get(), get(), get()) }
    factory { SubscribeForBluetoothStatusUseCase(get(), get()) }
    factory { ScanBleDevicesUseCase(get(), get()) }
    factory { SubscribeForLocationStatusUseCase(get(), get()) }
    factory { AdvertiseUidUseCase(get(), get(), get()) }
    factory { AdvertiseTxUseCase(get(), get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), androidContext()) }
    viewModel  { HelpViewModel(get()) }
    viewModel  { MainViewModel(get()) }
}