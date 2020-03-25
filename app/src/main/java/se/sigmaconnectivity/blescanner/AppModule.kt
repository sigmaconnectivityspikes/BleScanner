package se.sigmaconnectivity.blescanner

import com.polidea.rxandroidble2.RxBleClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { RxBleClient.create(androidApplication()) }
}