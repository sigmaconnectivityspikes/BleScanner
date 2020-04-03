package se.sigmaconnectivity.blescanner.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import se.sigmaconnectivity.blescanner.domain.usecase.TrackHasUserHadContactWithInfectedUseCase
import se.sigmaconnectivity.blescanner.ui.base.BaseViewModel

class MainViewModel(
    trackHasUserHadContactWithInfectedUseCase: TrackHasUserHadContactWithInfectedUseCase
) : BaseViewModel() {

    val showInfectionMessage: LiveData<Unit> =
        LiveDataReactiveStreams.fromPublisher(
            trackHasUserHadContactWithInfectedUseCase.execute()
                .filter { it }
                .map { Unit }
                .toFlowable(BackpressureStrategy.BUFFER)
        )
}