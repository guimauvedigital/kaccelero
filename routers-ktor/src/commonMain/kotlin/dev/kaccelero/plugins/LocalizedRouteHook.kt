package dev.kaccelero.plugins

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object LocalizedRouteHook : Hook<suspend (PipelineContext<Unit, PipelineCall>) -> Unit> {

    private val localizedRoutePhase = PipelinePhase("LocalizedRoute")

    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (PipelineContext<Unit, PipelineCall>) -> Unit,
    ) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, localizedRoutePhase)
        pipeline.intercept(localizedRoutePhase) { handler(this) }
    }

}
