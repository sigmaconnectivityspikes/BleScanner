package se.sigmaconnectivity.blescanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import se.sigmaconnectivity.blescanner.domain.usecase.GetHumanReadableUserIdUseCase

class MainViewModel(private val getHumanReadableUserIdUseCase: GetHumanReadableUserIdUseCase) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    val userId: LiveData<String> =
        LiveDataReactiveStreams.fromPublisher(getHumanReadableUserIdUseCase.execute().toFlowable())
}