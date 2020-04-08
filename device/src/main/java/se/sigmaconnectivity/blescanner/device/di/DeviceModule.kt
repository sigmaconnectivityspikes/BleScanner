package se.sigmaconnectivity.blescanner.device.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.device.BleScannerImpl
import se.sigmaconnectivity.blescanner.device.BluetoothStatusRepositoryImpl
import se.sigmaconnectivity.blescanner.device.LocationStatusRepositoryImpl
import se.sigmaconnectivity.blescanner.device.advertiser.BleTxAdvertiser
import se.sigmaconnectivity.blescanner.device.advertiser.BleUIDAdvertiser
import se.sigmaconnectivity.blescanner.domain.LocationStatusRepository
import se.sigmaconnectivity.blescanner.domain.ble.BleScanner
import se.sigmaconnectivity.blescanner.domain.ble.BluetoothStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
    single<BleScanner> { BleScannerImpl(androidApplication()) }
    single<LocationStatusRepository> { LocationStatusRepositoryImpl(androidApplication()) }
    single { BleUIDAdvertiser(androidApplication()) }
    single { BleTxAdvertiser(androidApplication()) }
}