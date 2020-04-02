package se.sigmaconnectivity.blescanner.data

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.data.db.ContactDatabase
import se.sigmaconnectivity.blescanner.data.db.SharedPrefs
import se.sigmaconnectivity.blescanner.domain.ContactRepository
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.PushNotifier
import se.sigmaconnectivity.blescanner.domain.UidRepository

val dataModule = module {
    single { ContactDatabase.buildDataBase(androidApplication()) }
    single { get<ContactDatabase>().contactDao() }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single<PushNotifier> { PushNotifierImpl() }
    single<UidRepository> { UidRepositoryImpl(get()) }
    single { SharedPrefs(androidApplication()) }

    factory<HashConverter> { HexHashConverter() }
}