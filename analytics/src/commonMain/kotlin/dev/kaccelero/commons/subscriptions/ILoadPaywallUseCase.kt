package dev.kaccelero.commons.subscriptions

import dev.kaccelero.usecases.IPairUseCase

interface ILoadPaywallUseCase : IPairUseCase<String, () -> Unit, Unit> {

    operator fun invoke(input: String) = invoke(input) {}

}
