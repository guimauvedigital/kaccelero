package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.IPairUseCase

interface ISetUserPropertyUseCase : IPairUseCase<IAnalyticsUserProperty, IAnalyticsUserValue, Unit> {

    operator fun invoke(input1: String, input2: Any) = invoke(
        AnalyticsUserProperty(input1), AnalyticsUserValue(input2)
    )

}
