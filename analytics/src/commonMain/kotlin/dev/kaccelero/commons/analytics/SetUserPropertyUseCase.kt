package dev.kaccelero.commons.analytics

import dev.kaccelero.repositories.IAnalyticsRepository

class SetUserPropertyUseCase(
    private val analyticsRepository: IAnalyticsRepository,
) : ISetUserPropertyUseCase {

    override fun invoke(input1: IAnalyticsUserProperty, input2: IAnalyticsUserValue) =
        analyticsRepository.setUserProperty(input1, input2)

}
