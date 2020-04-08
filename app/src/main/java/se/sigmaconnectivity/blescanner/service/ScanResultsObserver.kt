package se.sigmaconnectivity.blescanner.service

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.device.DistanceCalculator
import se.sigmaconnectivity.blescanner.domain.HASH_SIZE_BYTES
import se.sigmaconnectivity.blescanner.domain.HashConverter
import se.sigmaconnectivity.blescanner.domain.model.ContactItem
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*

class ScanResultsObserver(
    private val contactUseCase: ContactUseCase,
    private val hashConverter: HashConverter,
    private val distanceCalculator: DistanceCalculator
) {

    fun onNewResults(scanResults: List<ScanResultItem>) {
        val contactItems: Set<ContactItem> = scanResults.toContactItems()
        Timber.d("Contact items: $contactItems")
        refreshPendingLostItems(contactItems)
        val newItems = contactItems - existingScanItems
        Timber.d("New items found: $newItems")
        newItems.forEach {
            processFirstMatch(it)
        }
        val lostItems = existingScanItems - contactItems
        Timber.d("Items lost: $lostItems")
        lostItems.forEach {
            val timestampMillis = System.currentTimeMillis()
            processMatchLost(it.copy(timestamp = timestampMillis))
        }
    }

    fun onClear() {
        compositeDisposable.clear()
    }

    private val compositeDisposable = CompositeDisposable()

    private val existingScanItems = HashSet<ContactItem>()

    private val pendingLostItems = HashMap<ContactItem, Int>()

    private fun processFirstMatch(item: ContactItem) {
        Timber.d("First contact match: $item")
        existingScanItems.add(item)
        contactUseCase.processContactMatch(item.hashId, item.timestamp)
            .subscribe({
                Timber.d("processContactMatch() SUCCESS")
            }, {
                Timber.e(it, "processContactMatch() FAILED")
            }).addTo(compositeDisposable)
    }

    private fun processMatchLost(item: ContactItem) {
        Timber.d("Processing contact lost item: ${item.hashId}")
        val previousValue = pendingLostItems[item] ?: 0
        pendingLostItems[item] = previousValue + 1
        if (pendingLostItems[item] == Consts.MARK_LOST_AFTER_RETRIES) {
            Timber.d("Item confirmed as lost: $item")
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

    private fun refreshPendingLostItems(contactItems: Set<ContactItem>) =
        contactItems.forEach { pendingLostItems.remove(it) }

    private fun List<ScanResultItem>.toContactItems(): Set<ContactItem> =
        groupBy { it.address }
            .map { (_, entries) ->
                val distance =
                    entries.filter { it.serviceUuid == Consts.SERVICE_TX_UUID && it.txPowerLevel != null }
                        .map {
                            distanceCalculator.calculate(
                                rssi = it.rssi,
                                txPower = it.txPowerLevel ?: 0
                            )
                        }.average()
                val timestamp = entries.map { it.timeStamp }.min()
                val hashId =
                    entries.firstOrNull { it.serviceUuid == Consts.SERVICE_USER_HASH_UUID
                            && it.manufacturerSpecificData[Consts.MANUFACTURER_ID] != null }
                        ?.let {
                            assembleUID(it.manufacturerSpecificData[Consts.MANUFACTURER_ID])
                        }
                hashId?.let {
                    checkNotNull(timestamp)
                    Timber.d("WNASILOWSKILOG Contact items $distance")
                    ContactItem(it, timestamp, distance)
                }
            }.filterNotNull().toSet()


    private fun assembleUID(data: ByteArray?): String? {
        return data?.let {
            //TODO: change it to chained rx invocation
            val bytes = ByteBuffer.allocate(8)
                .put(it)
            val hashBytes = bytes.array().sliceArray(0 until HASH_SIZE_BYTES)
            hashConverter.convert(hashBytes)
        }
    }
}
