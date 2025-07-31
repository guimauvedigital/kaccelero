package dev.kaccelero.commons.exceptions

import dev.kaccelero.usecases.IUseCase

interface ICaptureExceptionUseCase : IUseCase<Throwable, Unit>
