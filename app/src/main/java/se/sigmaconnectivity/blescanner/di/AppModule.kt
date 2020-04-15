package se.sigmaconnectivity.blescanner.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import se.sigmaconnectivity.blescanner.domain.executor.PostExecutionThread
import se.sigmaconnectivity.blescanner.ui.MainViewModel
import se.sigmaconnectivity.blescanner.ui.help.HelpViewModel
import se.sigmaconnectivity.blescanner.ui.home.HomeViewModel

val appModule = module {
    factory<PostExecutionThread> { se.sigmaconnectivity.blescanner.executor.PostExecutionThread() }
}

val viewModelModule = module {
    viewModel { HomeViewModel() }
    viewModel  { HelpViewModel() }
    viewModel  { MainViewModel() }
}