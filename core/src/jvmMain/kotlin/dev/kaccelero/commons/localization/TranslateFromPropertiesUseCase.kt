package dev.kaccelero.commons.localization

import java.text.Format
import java.text.MessageFormat
import java.util.*

class TranslateFromPropertiesUseCase(
    private val baseName: String = DEFAULT_RESOURCE_BUNDLE,
    private val control: ResourceBundle.Control = UTF8Control(),
) : ITranslateUseCase {

    companion object {

        const val DEFAULT_RESOURCE_BUNDLE = "i18n.Messages"

    }

    private val cache = mutableMapOf<Pair<String, Locale>, Format>()

    override fun invoke(input1: Locale, input2: String, input3: List<String>): String {
        val bundle = ResourceBundle.getBundle(baseName, input1, control)
        val string = bundle.getString(input2)
        return input3.takeUnless { it.isEmpty() }?.let {
            cache.computeIfAbsent(Pair(string, bundle.locale)) {
                MessageFormat(string, bundle.locale)
            }.format(it.toTypedArray())
        } ?: string
    }

}
