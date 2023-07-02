package com.eden.orchid.impl.commands

import clog.Clog
import com.copperleaf.krow.builder.column
import com.copperleaf.krow.builder.krow
import com.copperleaf.krow.formatters.html.DefaultHtmlAttributes
import com.copperleaf.krow.formatters.html.HtmlTableFormatter
import com.copperleaf.krow.model.Krow
import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.OptionHolderDescription
import com.eden.orchid.api.options.OptionsExtractor
import com.eden.orchid.api.options.OptionsHolder
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.server.OrchidServer
import com.eden.orchid.api.tasks.OrchidCommand
import com.eden.orchid.utilities.OrchidUtils
import javax.inject.Inject

@Description("This will show a table will all the available options for a type, along with other relevant data.")
class HelpCommand
@Inject
constructor(
    private val server: OrchidServer?
) : OrchidCommand("help") {

    enum class HelpType {
        CLASS, CONFIG, PAGE
    }

    @Option
    @Description(
        "CLASS: The fully-qualified name of a class to describe." +
            "CONFIG: An object query for a given property in the site config." +
            "PAGE: A page type."
    )
    lateinit var type: HelpType

    @Option
    lateinit var query: String

    override fun parameters() = arrayOf(::type.name, ::query.name)

    @OptIn(ExperimentalStdlibApi::class)
    override fun run(context: OrchidContext, commandName: String) {
        val parsedClass = getDescribedClass()
        if (OptionsHolder::class.java.isAssignableFrom(parsedClass)) {
            val extractor = context.resolve(OptionsExtractor::class.java)
            val description = extractor.describeAllOptions(parsedClass)
            val table = getDescriptionTable(description)

            val asciiTable = OrchidUtils.defaultTableFormatter.print(table)
            Clog.i("\n{}", asciiTable)

            if (server?.websocket != null) {
                val htmlTable = HtmlTableFormatter(
                    attrs = object : DefaultHtmlAttributes() {
                        override val tableClasses = listOf("table")
                    }
                ).print(table)
                server.websocket!!.sendMessage("describe", htmlTable)
            }
        }
    }

    fun getDescriptionTable(optionsHolderDescription: OptionHolderDescription): Krow.Table {
        return krow {
            header {
                column("Type") { width = 45 }
                column("Default Value") { width = 15 }
                column("Description") { width = 15 }
            }
            body {
                optionsHolderDescription.optionsDescriptions.forEach { option ->
                    row(option.key) {
                        cell(option.optionType.simpleName)
                        cell(option.defaultValue)
                        cell(option.description)
                    }
                }
            }
        }
    }

    private fun getDescribedClass(): Class<*> {
        return Class.forName(query)
    }
}
