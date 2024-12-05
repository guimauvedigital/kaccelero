package dev.kaccelero.commons.analytics

class ILogEventUseCaseAsSuspend(private val useCase: ILogEventUseCase) : ILogEventSuspendUseCase {

    override suspend fun invoke(
        input1: IAnalyticsEventName,
        input2: List<Pair<IAnalyticsEventParameter, IAnalyticsEventValue>>,
    ) = useCase(input1, input2)

}
