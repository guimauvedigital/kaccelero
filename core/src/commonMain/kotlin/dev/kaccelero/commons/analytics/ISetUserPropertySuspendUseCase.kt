package dev.kaccelero.commons.analytics

import dev.kaccelero.usecases.IPairSuspendUseCase

interface ISetUserPropertySuspendUseCase : IPairSuspendUseCase<IAnalyticsUserProperty, IAnalyticsUserValue, Unit>
