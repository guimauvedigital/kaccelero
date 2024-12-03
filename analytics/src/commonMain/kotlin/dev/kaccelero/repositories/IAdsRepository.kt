package dev.kaccelero.repositories

import dev.kaccelero.commons.ads.AdKind

interface IAdsRepository {

    fun showAd(kind: AdKind, event: String, completion: () -> Unit)

}
