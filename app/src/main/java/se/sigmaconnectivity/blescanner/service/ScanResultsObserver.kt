package se.sigmaconnectivity.blescanner.service

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import timber.log.Timber
import java.util.*

class ScanResultsObserver(private val contactUseCase: ContactUseCase) {

    fun onNewResults(scanResults: Set<ScanResultItem>) {
        refreshPendingLostItems(scanResults)
        val newItems = scanResults - existingScanItems
        Timber.d("-BT- new items found: $newItems")
        newItems.forEach {
            processFirstMatch(it)
        }
        val lostItems = existingScanItems - scanResults
        Timber.d("-BT- items lost: $lostItems")
        lostItems.forEach {
            val timestampMillis = System.currentTimeMillis()
            processMatchLost(it.copy(timestamp = timestampMillis))
        }
    }

    fun onClear() {
        compositeDisposable.clear()
    }

    private val compositeDisposable = CompositeDisposable()

    private val existingScanItems = HashSet<ScanResultItem>()

    private val pendingLostItems = HashMap<ScanResultItem, Int>()

    private fun processFirstMatch(item: ScanResultItem) {
        Timber.d("CALLBACK_TYPE_FIRST_MATCH: ${item.hashId}")
        Timber.d("BT- adding $item")
        existingScanItems.add(item)
        contactUseCase.processContactMatch(item.hashId, item.timestamp)
            .subscribe({
                Timber.d("processContactMatch() SUCCESS")
            }, {
                Timber.e(it, "processContactMatch() FAILED")
            }).addTo(compositeDisposable)
    }

    private fun processMatchLost(item: ScanResultItem) {
        Timber.d("CALLBACK_TYPE_MATCH_LOST: ${item.hashId}")
            val previousValue = pendingLostItems[item] ?: 0
            pendingLostItems[item] =  previousValue + 1
        if (pendingLostItems[item] == Consts.MARK_LOST_AFTER_RETRIES) {
            Timber.d("BT- removing $item")
            pendingLostItems.remove(item)
            existingScanItems.remove(item)
            contactUseCase.processContactLost(item.hashId, item.timestamp)
                .subscribe({
                    Timber.d("processContactLost() SUCCESS")
                }, {
                    Timber.d("processContactLost() FAILED \n $it")
                }).addTo(compositeDisposable)
        }
    }

    private fun refreshPendingLostItems(scanResults: Set<ScanResultItem>)
            = scanResults.forEach { pendingLostItems.remove(it) }
}