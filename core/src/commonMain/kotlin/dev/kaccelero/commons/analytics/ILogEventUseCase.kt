package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.IPairUseCase

interface ILogEventUseCase :
    IPairUseCase<IAnalyticsEventName, List<Pair<IAnalyticsEventParameter, IAnalyticsEventValue>>, Unit> {

    operator fun invoke(
        input1: IAnalyticsEventName,
        vararg input2: Pair<IAnalyticsEventParameter, IAnalyticsEventValue>,
    ) = invoke(input1, input2.toList())

    operator fun invoke(input1: String, vararg input2: Pair<String, Any>) = invoke(
        AnalyticsEventName(input1),
        input2.map { AnalyticsEventParameter(it.first) to AnalyticsEventValue(it.second) }
    )

}
