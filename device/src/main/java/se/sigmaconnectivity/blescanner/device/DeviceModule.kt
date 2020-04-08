package se.sigmaconnectivity.blescanner.device

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.BleScanner
import se.sigmaconnectivity.blescanner.domain.BluetoothStatusRepository
import se.sigmaconnectivity.blescanner.domain.LocationStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
    single<BleScanner> { BleScannerImpl(androidApplication()) }
    single<LocationStatusRepository> { LocationStatusRepositoryImpl(androidApplication()) }

}