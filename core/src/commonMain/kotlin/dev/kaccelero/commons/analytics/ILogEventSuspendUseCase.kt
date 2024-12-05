package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.IPairSuspendUseCase

interface ILogEventSuspendUseCase :
    IPairSuspendUseCase<IAnalyticsEventName, List<Pair<IAnalyticsEventParameter, IAnalyticsEventValue>>, Unit>
