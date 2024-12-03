package dev.kaccelero.repositories

import dev.kaccelero.commons.analytics.*

interface IAnalyticsRepository {

    fun logEvent(
        name: IAnalyticsEventName,
        params: Map<IAnalyticsEventParameter, IAnalyticsEventValue>,
        completion: () -> Unit,
    )

    fun setUserProperty(
        name: IAnalyticsUserProperty,
        value: IAnalyticsUserValue,
    )

    fun setUserId(value: String?)

}
