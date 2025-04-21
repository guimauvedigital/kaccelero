package dev.kaccelero.commons.jobs

import dev.kaccelero.usecases.ITripleSuspendUseCase

interface IHandleJobUseCase : ITripleSuspendUseCase<IJobsService, IJobKey, String, Unit>
