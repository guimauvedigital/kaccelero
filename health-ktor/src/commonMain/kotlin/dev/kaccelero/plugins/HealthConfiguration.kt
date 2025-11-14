package dev.kaccelero.plugins

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class HealthConfiguration internal constructor() {

    var checks: Map<String, Map<String, HealthCheck>> = emptyMap()
        private set
    var noHealth = false
        private set
    var noReady = false
        private set
    var cachingResults: Boolean = false
        private set
    var cachingRefreshInterval: Duration = 60.seconds
        private set
    var cachingCheckTimeout: Duration = 30.seconds
        private set
    var cachingStalenessThreshold: Duration? = null
        private set

    internal fun getChecksWithFunctions(): Map<String, suspend () -> Map<String, Boolean>> =
        checks.mapValues { (_, v) ->
            {
                v.mapValues { it.value() }
            }
        }

    private fun ensureDisableUnambiguous(url: String) {
        checks[url]?.let {
            if (it.isNotEmpty()) throw AssertionError("Cannot disable a check which has been assigned functions")
        }
    }

    /**
     * Calling this disables the default health check on /healthz
     */
    fun disableHealthCheck() {
        noHealth = true
        ensureDisableUnambiguous("healthz")
    }

    /**
     * Calling this disabled the default ready check on /readyz
     */
    fun disableReadyCheck() {
        noReady = true
        ensureDisableUnambiguous("readyz")
    }

    private fun getCheck(url: String) = checks.getOrElse(url) {
        mutableMapOf<String, suspend () -> Boolean>().also {
            checks = checks + (url to it)
        }
    }

    /**
     * Adds a check function to a custom check living at the specified URL
     */
    fun customCheck(url: String, name: String, check: HealthCheck) {
        (getCheck(url.trim('/')) as MutableMap<String, suspend () -> Boolean>)[name] = check
    }

    /**
     * Add a health check giving it a name
     */
    fun healthCheck(name: String, check: HealthCheck) = customCheck("healthz", name, check)

    /**
     * Add a ready check giving it a name
     */
    fun readyCheck(name: String, check: HealthCheck) = customCheck("readyz", name, check)

    /**
     * Enable caching of health check results with background refresh
     * @param refreshInterval How often to refresh the cache in background (must be positive)
     * @param checkTimeout Maximum time a single check can run before being considered failed (must be positive)
     * @param stalenessThreshold Maximum age of cache before it's considered stale (defaults to 3x refreshInterval, must be positive if provided)
     * @throws IllegalArgumentException if any duration is not positive, or if checkTimeout >= refreshInterval
     */
    fun enableCachingResults(
        refreshInterval: Duration = 60.seconds,
        checkTimeout: Duration = 30.seconds,
        stalenessThreshold: Duration? = null,
    ) {
        require(refreshInterval.isPositive()) { "refreshInterval must be positive, got $refreshInterval" }
        require(checkTimeout.isPositive()) { "checkTimeout must be positive, got $checkTimeout" }
        require(checkTimeout < refreshInterval) {
            "checkTimeout ($checkTimeout) must be less than refreshInterval ($refreshInterval)"
        }
        stalenessThreshold?.let {
            require(it.isPositive()) { "stalenessThreshold must be positive, got $it" }
        }

        cachingResults = true
        cachingRefreshInterval = refreshInterval
        cachingCheckTimeout = checkTimeout
        cachingStalenessThreshold = stalenessThreshold
    }

    internal fun ensureWellKnown() {
        if (!noHealth) getCheck("healthz")
        if (!noReady) getCheck("readyz")
    }

}
