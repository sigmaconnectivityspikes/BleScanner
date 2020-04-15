package se.sigmaconnectivity.blescanner.data

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.data.db.ContactDatabase
import se.sigmaconnectivity.blescanner.data.db.SharedPreferencesDelegates
import se.sigmaconnectivity.blescanner.data.db.UserIdStore
import se.sigmaconnectivity.blescanner.domain.PushNotifier
import se.sigmaconnectivity.blescanner.domain.UserRepository

val dataModule = module {
    single { ContactDatabase.buildDataBase(androidApplication()) }
    single { get<ContactDatabase>().contactDao() }
    single<PushNotifier> { PushNotifierImpl() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single { UserIdStore(get()) }
    single { SharedPreferencesDelegates(androidApplication()) }

}