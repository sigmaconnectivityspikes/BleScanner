package se.sigmaconnectivity.blescanner.service

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import se.sigmaconnectivity.blescanner.Consts
import se.sigmaconnectivity.blescanner.device.DistanceCalculator
import se.sigmaconnectivity.blescanner.domain.HASH_PREFIX_SIZE_BYTES
import se.sigmaconnectivity.blescanner.domain.HASH_SIZE_BYTES
import se.sigmaconnectivity.blescanner.domain.asHexString
import se.sigmaconnectivity.blescanner.domain.model.ContactItem
import se.sigmaconnectivity.blescanner.domain.model.ScanResultItem
import se.sigmaconnectivity.blescanner.domain.usecase.ContactUseCase
import timber.log.Timber
import java.util.*

class ScanResultsObserver(
    private val contactUseCase: ContactUseCase,
    private val distanceCalculator: DistanceCalculator
) {

    fun onNewResults(scanResults: List<ScanResultItem>) {
        val contactItems: Set<ContactItem> = scanResults.toContactItems()
        Timber.d("-DIST- Contact items: $contactItems")
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
        asSequence().map {
            ProcessedScanItem.createOf(it)

        }.filterNotNull()
            .groupBy { it.hashId.slice(0 until 8) }
            .map { (hashPrefix, entries) ->
                val timestamp = entries.map { it.timestamp }.min()
                checkNotNull(timestamp)

                val distances =
                    entries.filter { it.txPowerLevel != null }.map { item ->
                        checkNotNull(item.txPowerLevel)
                        distanceCalculator.calculate(
                            rssi = item.rssi,
                            txPower = item.txPowerLevel
                        ).also { result ->
                            Timber.d("-DIST-- -Distance hash: $hashPrefix rssi: ${item.rssi} tx power: ${item.txPowerLevel} result: $result")
                        }
                    }
                val averageDistance = distances.average()
                val fullHashId = entries.firstOrNull { it.hashId.length == 16 }?.hashId
                fullHashId?.let {
                    ContactItem(
                        hashId = it,
                        timestamp = timestamp,
                        distance = averageDistance
                    )
                }
            }.filterNotNull()
            .toSet()
}

data class ProcessedScanItem(
    val hashId: String,
    val timestamp: Long,
    val txPowerLevel: Int?,
    val rssi: Int
) {
    companion object {
        fun createOf(item: ScanResultItem) = with(item) {
            val hashId =
                assembleUID(manufacturerSpecificData[Consts.MANUFACTURER_ID])
            hashId?.let {
                ProcessedScanItem(
                    hashId = it,
                    txPowerLevel = if (serviceUuid == Consts.SERVICE_TX_UUID) txPowerLevel else null,
                    timestamp = timestamp,
                    rssi = rssi
                )
            }
        }

        private fun assembleUID(data: ByteArray?): String? {
            return data?.let {
                val dataSize = it.size
                if (dataSize == HASH_SIZE_BYTES || dataSize == HASH_PREFIX_SIZE_BYTES) {
                    it.asHexString()
                } else {
                    null
                }
            }
        }
    }
}