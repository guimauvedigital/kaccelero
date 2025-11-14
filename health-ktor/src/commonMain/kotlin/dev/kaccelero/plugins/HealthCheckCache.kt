package dev.kaccelero.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class HealthCheckCache {

    private val mutex = Mutex()
    private val cache = mutableMapOf<String, CachedResult>()

    data class CachedResult(
        val results: Map<String, Boolean>,
        val lastUpdate: Instant,
    )

    suspend fun get(url: String): CachedResult? = mutex.withLock {
        cache[url]
    }

    suspend fun update(url: String, results: Map<String, Boolean>) = mutex.withLock {
        cache[url] = CachedResult(
            results = results,
            lastUpdate = Clock.System.now(),
        )
    }

    suspend fun isStale(url: String, stalenessThreshold: Duration): Boolean = mutex.withLock {
        val cached = cache[url] ?: return@withLock true
        val age = Clock.System.now() - cached.lastUpdate
        age > stalenessThreshold
    }

}
