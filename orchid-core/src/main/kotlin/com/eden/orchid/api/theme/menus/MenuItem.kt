package com.eden.orchid.api.theme.menus

import com.eden.common.util.EdenUtils
import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.indexing.OrchidIndex
import com.eden.orchid.api.theme.pages.OrchidPage

class MenuItem private constructor(
    val context: OrchidContext,
    val title: String,
    val page: OrchidPage?,
    private val _children: List<MenuItem>,
    val anchor: String,
    val isSeparator: Boolean,
    var allData: Map<String, Any>,
    var indexComparator: Comparator<MenuItem>?
) {

    val children: List<MenuItem>
        get() {
            if (indexComparator != null) {
                for (child in _children) {
                    child.indexComparator = indexComparator
                }

                return _children.sortedWith(indexComparator!!)
            }
            return _children
        }

    val link: String
        get() = if (page != null) {
            if (anchor.isNotBlank()) {
                "${page.link}#$anchor"
            } else {
                page.link
            }
        } else {
            if (anchor.isNotBlank()) {
                "#$anchor"
            } else {
                ""
            }
        }

    val isHasChildren: Boolean get() = hasChildren()

    fun isActive(page: OrchidPage): Boolean {
        return (!isHasChildren && isActivePage(page)) || (isHasChildren && hasActivePage(page))
    }

    fun isActivePage(page: OrchidPage): Boolean {
        return this.page === page
    }

    fun hasActivePage(page: OrchidPage): Boolean {
        for (child in children) {
            if (child.isActivePage(page) || child.hasActivePage(page)) {
                return true
            }
        }

        return false
    }

    @JvmOverloads
    fun activeClass(page: OrchidPage, className: String = "active"): String {
        return if (isActive(page)) className else ""
    }

    fun hasChildren(): Boolean {
        return !EdenUtils.isEmpty(children)
    }

    fun hasOwnLink(): Boolean {
        return page != null || anchor.isNotBlank()
    }

    fun isExternal(): Boolean {
        return !link.startsWith(context.baseUrl)
    }

    operator fun get(key: String): Any? {
        return allData[key]
    }

    fun toBuilder(): Builder {
        return Builder(context)
            .title(title)
            .page(page)
            .children(children)
            .anchor(anchor)
            .separator(isSeparator)
            .data(allData)
            .indexComparator(indexComparator)
    }

// Builder
// ---------------------------------------------------------------------------------------------------------------------

    class Builder(val context: OrchidContext) {
        var children: MutableList<Builder>? = null
        var page: OrchidPage? = null
        var anchor: String? = null
        var title: String? = null
        var indexComparator: Comparator<MenuItem>? = null
        var data: Map<String, Any>? = null

        var separator: Boolean = false

        constructor(context: OrchidContext, builder: Builder.() -> Unit) : this(context) {
            this.builder()
        }

        fun fromIndex(index: OrchidIndex): Builder {
            if (children == null) {
                children = ArrayList()
            }

            if (index.children.isNotEmpty()) {
                for ((key, value) in index.children) {
                    this.children!!.add(Builder(context).title(key).fromIndex(value))
                }
            }

            if (!EdenUtils.isEmpty(index.getOwnPages())) {
                for (page in index.getOwnPages()) {
                    if (page.reference.fileName == "index") {
                        this.title(page.title).page(page)
                    } else {
                        this.children!!.add(Builder(context).page(page))
                    }
                }
            }

            return this
        }

        fun childrenBuilders(children: MutableList<Builder>): Builder {
            this.children = children
            return this
        }

        fun children(children: List<MenuItem>): Builder {
            this.children = children.map { it.toBuilder() }.toMutableList()
            return this
        }

        fun child(child: Builder): Builder {
            if (children == null) {
                children = ArrayList()
            }
            children!!.add(child)
            return this
        }

        fun pages(pages: List<OrchidPage>, transform: (Pair<OrchidPage, Builder>) -> Unit = {}): Builder {
            this.children = pages.map { page ->
                Builder(context)
                    .page(page)
                    .also { builder -> transform(page to builder) }
            }.toMutableList()
            return this
        }

        fun page(page: OrchidPage?): Builder {
            this.page = page
            return this
        }

        fun anchor(anchor: String): Builder {
            this.anchor = anchor
            return this
        }

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun separator(separator: Boolean): Builder {
            this.separator = separator
            return this
        }

        fun indexComparator(indexComparator: Comparator<MenuItem>?): Builder {
            this.indexComparator = indexComparator
            return this
        }

        fun data(data: Map<String, Any>): Builder {
            this.data = data
            return this
        }

        fun build(): MenuItem {
            if (title.isNullOrBlank() && page != null) {
                title = page!!.title
            }

            if (title.isNullOrBlank() && !anchor.isNullOrBlank()) {
                title = anchor
            }

            return MenuItem(
                context,
                title ?: "",
                page,
                children?.map { it.build() } ?: emptyList(),
                anchor ?: "",
                separator,
                data ?: emptyMap(),
                indexComparator
            )
        }
    }
}
