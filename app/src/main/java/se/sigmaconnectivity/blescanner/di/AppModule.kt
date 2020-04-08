package se.sigmaconnectivity.blescanner.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.device.advertiser.BleTxAdvertiser
import se.sigmaconnectivity.blescanner.device.advertiser.BleUIDAdvertiser
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.*
import se.sigmaconnectivity.blescanner.domain.usecase.device.*
import se.sigmaconnectivity.blescanner.service.ScanResultsObserver
import se.sigmaconnectivity.blescanner.ui.MainViewModel
import se.sigmaconnectivity.blescanner.ui.help.HelpViewModel
import se.sigmaconnectivity.blescanner.ui.home.HomeViewModel

val appModule = module {
    single { ScanResultsObserver( get(), get(), get()) }

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
    factory { AdvertiseUidUseCase(get(), get<BleUIDAdvertiser>(), get()) }
    factory { AdvertiseTxUseCase(get(), get<BleTxAdvertiser>()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), androidContext()) }
    viewModel  { HelpViewModel(get()) }
    viewModel  { MainViewModel(get()) }
}