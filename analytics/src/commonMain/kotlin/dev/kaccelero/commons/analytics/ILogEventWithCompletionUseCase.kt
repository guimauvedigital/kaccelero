package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.ITripleUseCase

interface ILogEventWithCompletionUseCase :
    ITripleUseCase<IAnalyticsEventName, List<Pair<IAnalyticsEventParameter, IAnalyticsEventValue>>, () -> Unit, Unit> {

    operator fun invoke(
        input1: IAnalyticsEventName,
        vararg input2: Pair<IAnalyticsEventParameter, IAnalyticsEventValue>,
        input3: () -> Unit,
    ) = invoke(input1, input2.toList(), input3)

    operator fun invoke(input1: String, vararg input2: Pair<String, Any>, input3: () -> Unit) = invoke(
        AnalyticsEventName(input1),
        input2.map { AnalyticsEventParameter(it.first) to AnalyticsEventValue(it.second) },
        input3
    )

}
