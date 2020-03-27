package se.sigmaconnectivity.blescanner

import com.polidea.rxandroidble2.RxBleClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCaseImpl

val appModule = module {
    single { RxBleClient.create(androidApplication()) }
    factory<PostExecutionThread> { PostExecutionThread() }
    factory<ContactUseCase> { ContactUseCaseImpl(get(), get()) }
}