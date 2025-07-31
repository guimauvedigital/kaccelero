package dev.kaccelero.commons.permissions

import dev.kaccelero.usecases.IPairUseCase

interface ICheckPermissionUseCase : IPairUseCase<IPermittee, IPermission, Boolean>
