package com.eden.orchid.impl.themes.menus

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.IntDefault
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.theme.menus.MenuItem
import com.eden.orchid.api.theme.menus.OrchidMenuFactory
import com.eden.orchid.api.theme.pages.OrchidPage

@Description(
    "All pages in a collection",
    name = "Collection Pages"
)
class CollectionPagesMenuItem : OrchidMenuFactory("collectionPages") {

    @Option
    @Description("The Id of the items to link to.")
    lateinit var itemId: String

    @Option
    @Description("The type of collection the items are expected to come from.")
    lateinit var collectionType: String

    @Option
    @Description("The specific Id of the given collection type where the items are expected to come from.")
    lateinit var collectionId: String

    @Option
    @IntDefault(0)
    @Description("Paginate results starting at this page. Index starts at 1.")
    var pageIndex: Int = 0

    @Option
    @IntDefault(0)
    @Description("Paginate results using this as a page size")
    var pageSize: Int = 0

    override fun getMenuItems(
        context: OrchidContext,
        page: OrchidPage
    ): List<MenuItem> {
        val loadedPages: List<OrchidPage> = if (pageIndex > 0 && pageSize > 0) {
            context.findAll(collectionType, collectionId, itemId, pageIndex, pageSize).filterIsInstance<OrchidPage>()
        } else {
            context.findAll(collectionType, collectionId, itemId).filterIsInstance<OrchidPage>()
        }

        return loadedPages.map {
            MenuItem.Builder(context)
                .page(it)
                .build()
        }
    }
}
