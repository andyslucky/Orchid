package com.eden.orchid.taxonomies.utils

import com.eden.orchid.api.theme.pages.OrchidPage
import com.eden.orchid.utilities.SuppressedWarnings
import java.util.*

fun OrchidPage.getSingleTermValue(taxonomy: String): String? {
    try {
        val method = this.javaClass.getMethod(
            "get${
                taxonomy.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }")
        return method.invoke(this)?.toString()
    } catch (e: Exception) {
        if (this.has(taxonomy)) {
            return this.get(taxonomy)?.toString()
        }
    }

    return null
}

@Suppress(SuppressedWarnings.UNCHECKED_KOTLIN)
fun OrchidPage.getTermValues(taxonomy: String): List<String> {
    try {
        val method = this.javaClass.getMethod(
            "get${
                taxonomy.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }")
        val result = method.invoke(this)
        if (result is List<*>) {
            return result as List<String>
        } else if (result is Array<*>) {
            return (result as Array<String>).toList()
        }
    } catch (e: Exception) {
        if (this.has(taxonomy) && this.get(taxonomy) is Iterable<*>) {
            val terms = ArrayList<String>()

            (this.get(taxonomy) as? Iterable<*>)?.forEach {
                terms.add(it.toString())
            }

            return terms
        }
    }

    return emptyList()
}
