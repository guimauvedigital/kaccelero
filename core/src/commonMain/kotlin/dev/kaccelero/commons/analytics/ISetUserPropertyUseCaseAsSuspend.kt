package dev.kaccelero.commons.analytics

class ISetUserPropertyUseCaseAsSuspend(private val useCase: ISetUserPropertyUseCase) : ISetUserPropertySuspendUseCase {

    override suspend fun invoke(
        input1: IAnalyticsUserProperty,
        input2: IAnalyticsUserValue,
    ) = useCase(input1, input2)

}
