package se.sigmaconnectivity.blescanner.device.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.device.BleScannerImpl
import se.sigmaconnectivity.blescanner.device.BluetoothStatusRepositoryImpl
import se.sigmaconnectivity.blescanner.device.LocationStatusRepositoryImpl
import se.sigmaconnectivity.blescanner.device.advertiser.BleTxAdvertiserImpl
import se.sigmaconnectivity.blescanner.device.advertiser.BleUidAdvertiserImpl
import se.sigmaconnectivity.blescanner.domain.LocationStatusRepository
import se.sigmaconnectivity.blescanner.domain.ble.BleScanner
import se.sigmaconnectivity.blescanner.domain.ble.BleTxAdvertiser
import se.sigmaconnectivity.blescanner.domain.ble.BleUidAdvertiser
import se.sigmaconnectivity.blescanner.domain.ble.BluetoothStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
    single<BleScanner> { BleScannerImpl(androidApplication()) }
    single<LocationStatusRepository> { LocationStatusRepositoryImpl(androidApplication()) }
    single<BleUidAdvertiser> { BleUidAdvertiserImpl(androidApplication()) }
    single<BleTxAdvertiser> { BleTxAdvertiserImpl(androidApplication()) }

}