package se.sigmaconnectivity.blescanner.device

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.BluetoothStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
    single { BluetoothScanner(androidApplication()) }
    single { LocationStatusRepository(androidApplication()) }

}