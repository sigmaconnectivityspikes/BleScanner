package se.sigmaconnectivity.blescanner.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.map
import com.snakydesign.livedataextensions.filter
import io.reactivex.BackpressureStrategy
import se.sigmaconnectivity.blescanner.domain.model.InfectionItem
import se.sigmaconnectivity.blescanner.domain.usecase.TrackInfectionsUseCase
import se.sigmaconnectivity.blescanner.ui.base.BaseViewModel

class MainViewModel(trackInfectionsUseCase: TrackInfectionsUseCase) : BaseViewModel() {

    private val _infectionItem: LiveData<InfectionItem> =
        LiveDataReactiveStreams.fromPublisher(
            trackInfectionsUseCase.execute().toFlowable(
                BackpressureStrategy.BUFFER
            )
        )

    val showInfectionMessage: LiveData<Unit> = _infectionItem.map{true}.filter { it == true }.map{Unit}

}