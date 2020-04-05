package se.sigmaconnectivity.blescanner.device

import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.BluetoothStatusRepository

val deviceModule = module {
    single<BluetoothStatusRepository> { BluetoothStatusRepositoryImpl() }
}