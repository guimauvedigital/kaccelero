package dev.kaccelero.commons.jobs

import dev.kaccelero.usecases.ITripleSuspendUseCase

interface IHandleJobResponseUseCase : ITripleSuspendUseCase<IJobsService, IJobKey, String, Unit>
