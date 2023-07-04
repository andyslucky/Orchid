package com.eden.orchid.sourcedoc.menu

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.options.annotations.StringDefault
import com.eden.orchid.api.theme.menus.MenuItem
import com.eden.orchid.api.theme.menus.OrchidMenuFactory
import com.eden.orchid.api.theme.pages.OrchidPage
import com.eden.orchid.sourcedoc.model.SourceDocModel
import com.eden.orchid.sourcedoc.page.SourceDocPage
import java.util.*

@Description(
    "Locate all source pages of a given kind.",
    name = "Sourcedoc Source Pages"
)
class SourceDocPagesMenuItemType : OrchidMenuFactory("sourcedocPages") {

    enum class ItemTitleType {
        NAME, ID
    }

    @Option
    lateinit var title: String

    @Option
    lateinit var moduleType: String

    @Option
    lateinit var moduleName: String

    @Option
    lateinit var node: String

    @Option
    @StringDefault("NAME")
    lateinit var itemTitleType: ItemTitleType

    @Option
    @StringDefault("")
    lateinit var transform: String

    @Option
    @StringDefault("peb")
    lateinit var transformAs: String

    override fun getMenuItems(
        context: OrchidContext,
        page: OrchidPage
    ): List<MenuItem> {
        return try {
            val module = SourceDocModel.getModule(context, moduleType, moduleName)
            if (module == null) return emptyList()

            if (node.isNotBlank()) {
                val pages: List<SourceDocPage<*>> = module
                    .nodes
                    .entries
                    .firstOrNull { it.key.name == node }
                    ?.value
                    ?: emptyList()

                pages
                    .sortedBy { it.title }
                    .map { menuItemPage ->
                        MenuItem.Builder(context)
                            .page(menuItemPage)
                            .also { applyTitle(context, menuItemPage, it) }
                            .build()
                    }
            } else {
                module
                    .nodes
                    .map { node ->
                        val nodeTitle =
                            "All ${node.key.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
                        val nodePages = node.value.sortedBy { it.title }

                        MenuItem.Builder(context)
                            .title(nodeTitle)
                            .pages(nodePages) { (page, it) -> applyTitle(context, page as SourceDocPage<*>, it) }
                            .build()
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun applyTitle(context: OrchidContext, page: SourceDocPage<*>, it: MenuItem.Builder) {
        var itemTitle = when (itemTitleType) {
            ItemTitleType.NAME -> page.title
            ItemTitleType.ID -> page.element.id
        }
        if (transform.isNotBlank()) {
            itemTitle = context.compile(
                page.resource, transformAs, transform,
                mapOf(
                    "title" to itemTitle,
                    "page" to page,
                    "element" to page.element
                )
            )
        }
        it.title(itemTitle)
    }
}
