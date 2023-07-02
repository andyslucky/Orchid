package com.eden.orchid.api.generators

import com.eden.orchid.api.options.Descriptive
import com.eden.orchid.utilities.*
import java.util.stream.Collectors
import java.util.stream.Stream

abstract class OrchidCollection<T : Collectible<*>>(
    generator: OrchidGenerator<*>,
    val collectionId: String?,
    private val items: () -> Stream<T>
) : Descriptive {

    val collectionType: String = generator.key

    val title: String
        get() {
            val collectionTypeTitle = collectionType from { camelCase() } with { capitalize() } to { titleCase() }
            val collectionIdTitle: String? = collectionId
                ?.from { camelCase() }
                ?.with { capitalize() }
                ?.to { titleCase() }

            return if (collectionIdTitle?.isNotBlank() == true && collectionTypeTitle != collectionIdTitle) {
                "$collectionTypeTitle > $collectionIdTitle"
            } else {
                collectionTypeTitle
            }
        }

    fun stream(): Stream<T> {
        return this.items()
    }

    fun getItems(): List<T> {
        return this.items().collect(Collectors.toList<T>())
    }
}
