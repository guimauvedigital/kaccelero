package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.IPairUseCase
import kotlin.js.JsExport

@JsExport
interface ILogEventUseCase :
    IPairUseCase<IAnalyticsEventName, Map<IAnalyticsEventParameter, IAnalyticsEventValue>, Unit> {

    operator fun invoke(input: IAnalyticsEventName) = invoke(input, emptyMap())
    operator fun invoke(input: String) = invoke(input, emptyMap())

    operator fun invoke(input1: String, input2: Map<String, Any>) = invoke(
        AnalyticsEventName(input1),
        input2.mapKeys { AnalyticsEventParameter(it.key) }.mapValues { AnalyticsEventValue(it.value) }
    )

}
