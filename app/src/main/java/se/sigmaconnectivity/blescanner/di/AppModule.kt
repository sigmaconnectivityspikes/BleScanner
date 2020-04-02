package se.sigmaconnectivity.blescanner.di

import com.polidea.rxandroidble2.RxBleClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCaseImpl
import se.sigmaconnectivity.blescanner.domain.usecase.GetHumanReadableUserIdUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.GetUserIdHashUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.NotifyInfectionUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.TrackInfectionsUseCase
import se.sigmaconnectivity.blescanner.ui.help.HelpViewModel
import se.sigmaconnectivity.blescanner.ui.home.HomeViewModel

val appModule = module {
    single { RxBleClient.create(androidApplication()) }
    factory<PostExecutionThread> { se.sigmaconnectivity.blescanner.ui.executor.PostExecutionThread() }
    factory<ContactUseCase> { ContactUseCaseImpl(get(), get()) }
    factory { TrackInfectionsUseCase(get(), get()) }
    factory { NotifyInfectionUseCase(get(), get()) }
    factory { GetUserIdHashUseCase(get(), get()) }
    factory { GetHumanReadableUserIdUseCase(get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel  { HelpViewModel(get()) }
}