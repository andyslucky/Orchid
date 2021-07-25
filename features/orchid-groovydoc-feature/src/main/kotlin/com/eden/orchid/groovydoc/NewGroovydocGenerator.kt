package com.eden.orchid.groovydoc

import com.copperleaf.kodiak.groovy.GroovydocInvokerImpl
import com.copperleaf.kodiak.groovy.models.GroovyModuleDoc
import com.eden.orchid.api.options.OptionsExtractor
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.theme.permalinks.PermalinkStrategy
import com.eden.orchid.groovydoc.models.GroovyDocModuleConfig
import com.eden.orchid.sourcedoc.SourcedocGenerator
import javax.inject.Inject

@Description(
    "Generate SourceDoc content for Groovy/Java source files",
    name = "Groovydoc"
)
class NewGroovydocGenerator
@Inject
constructor(
    invoker: GroovydocInvokerImpl,
    extractor: OptionsExtractor,
    permalinkStrategy: PermalinkStrategy
) : SourcedocGenerator<GroovyModuleDoc, GroovyDocModuleConfig>(
    GENERATOR_KEY,
    invoker,
    extractor,
    permalinkStrategy
) {

    @Option
    override lateinit var modules: MutableList<GroovyDocModuleConfig>

    @Option
    @Description("The configuration for the default wiki, when no other categories are set up.")
    override lateinit var defaultConfig: GroovyDocModuleConfig

    companion object {
        const val GENERATOR_KEY = "groovydoc"

        val type = "groovy"
        val nodeKinds = listOf("packages", "classes")
        val otherSourceKinds = listOf("java")
    }
}
