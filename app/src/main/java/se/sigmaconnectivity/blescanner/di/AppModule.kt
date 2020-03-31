package se.sigmaconnectivity.blescanner.di

import com.polidea.rxandroidble2.RxBleClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.SharedPrefs
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCaseImpl

val appModule = module {
    single { RxBleClient.create(androidApplication()) }
    factory<PostExecutionThread> { se.sigmaconnectivity.blescanner.PostExecutionThread() }
    factory<ContactUseCase> { ContactUseCaseImpl(get(), get()) }
    factory { SharedPrefs(androidApplication()) }
}