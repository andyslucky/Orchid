package com.eden.orchid.impl.themes.functions

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.compilers.TemplateFunction
import com.eden.orchid.api.options.annotations.BooleanDefault
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.resources.resourcesource.LocalResourceSource
import com.eden.orchid.api.theme.pages.OrchidPage

@Description(value = "Load content from a resource.", name = "Load")
class LoadFunction : TemplateFunction("load", false) {

    @Option
    @Description("The resource to lookup.")
    lateinit var resource: String

    @Option
    @BooleanDefault(true)
    @Description(
        "When true, only resources from local sources are considered, otherwise resources from all plugins " +
            "and from the current theme will also be considered."
    )
    var local: Boolean = true

    @Option
    @BooleanDefault(false)
    @Description("When true, return the Front Matter data from the resource, instead of its content.")
    var frontMatter: Boolean = false

    @Option
    @BooleanDefault(true)
    @Description(
        "When true, content will automatically be compiled according to its extension. If false, the raw " +
            "content will be returned directly as a String, minus any Front Matter."
    )
    var compile: Boolean = true

    override fun parameters() = arrayOf(::resource.name, ::local.name, ::compile.name)

    override fun apply(context: OrchidContext, page: OrchidPage?): Any {
        val foundResource = if (local)
            context.getDefaultResourceSource(LocalResourceSource, null).getResourceEntry(context, resource)
        else
            context.getDefaultResourceSource(null, null).getResourceEntry(context, resource)

        return if (foundResource != null) {
            if (frontMatter) {
                foundResource.embeddedData
            } else if (compile) {
                foundResource.compileContent(context, null)
            } else {
                foundResource.content
            }
        } else ""
    }
}
