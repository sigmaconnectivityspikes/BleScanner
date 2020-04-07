package se.sigmaconnectivity.blescanner.device.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.device.BluetoothScanner
import se.sigmaconnectivity.blescanner.device.BluetoothStatusRepositoryImpl
import se.sigmaconnectivity.blescanner.device.LocationStatusRepository
import se.sigmaconnectivity.blescanner.device.advertiser.BluetoothTxAdvertiser
import se.sigmaconnectivity.blescanner.device.advertiser.BluetoothUIDAdvertiser
import se.sigmaconnectivity.blescanner.domain.BluetoothStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
    single {
        BluetoothScanner(
            androidApplication()
        )
    }
    single {
        LocationStatusRepository(
            androidApplication()
        )
    }
    single {
        BluetoothUIDAdvertiser(
            androidApplication()
        )
    }

    single {
        BluetoothTxAdvertiser(
            androidApplication()
        )
    }
}