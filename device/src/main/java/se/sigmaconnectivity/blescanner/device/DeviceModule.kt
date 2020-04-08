package se.sigmaconnectivity.blescanner.device

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.LocationStatusRepository
import se.sigmaconnectivity.blescanner.domain.ble.BleScanner
import se.sigmaconnectivity.blescanner.domain.ble.BluetoothStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
    single<BleScanner> { BleScannerImpl(androidApplication()) }
    single<LocationStatusRepository> { LocationStatusRepositoryImpl(androidApplication()) }

}