package dev.kaccelero.commons.analytics

class LogEventUseCase(
    private val logEventWithCompletionUseCase: ILogEventWithCompletionUseCase,
) : ILogEventUseCase {

    override fun invoke(
        input1: IAnalyticsEventName,
        input2: List<Pair<IAnalyticsEventParameter, IAnalyticsEventValue>>,
    ) = logEventWithCompletionUseCase(input1, input2) {}

}
