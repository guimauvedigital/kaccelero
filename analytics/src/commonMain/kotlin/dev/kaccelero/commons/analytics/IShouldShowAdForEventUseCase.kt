package dev.kaccelero.commons.analytics

import dev.kaccelero.commons.ads.AdKind
import dev.kaccelero.usecases.IUseCase

interface IShouldShowAdForEventUseCase : IUseCase<String, AdKind?>
