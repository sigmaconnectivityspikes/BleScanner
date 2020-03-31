package se.sigmaconnectivity.blescanner.di

import com.polidea.rxandroidble2.RxBleClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.SharedPrefs
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCaseImpl
import se.sigmaconnectivity.blescanner.domain.usecase.NotifyInfectionUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.TrackInfectionsUseCase

val appModule = module {
    single { RxBleClient.create(androidApplication()) }
    factory<PostExecutionThread> { se.sigmaconnectivity.blescanner.PostExecutionThread() }
    factory<ContactUseCase> { ContactUseCaseImpl(get(), get()) }
    factory { TrackInfectionsUseCase(get(), get()) }
    factory { NotifyInfectionUseCase(get(), get()) }
    factory { SharedPrefs(androidApplication()) }
}