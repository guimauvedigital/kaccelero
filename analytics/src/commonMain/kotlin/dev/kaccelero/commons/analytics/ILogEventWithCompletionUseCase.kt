package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.ITripleUseCase

interface ILogEventWithCompletionUseCase :
    ITripleUseCase<IAnalyticsEventName, Map<IAnalyticsEventParameter, IAnalyticsEventValue>, () -> Unit, Unit>
