package com.eden.orchid.utilities

import clog.Clog
import clog.dsl.tag
import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.OptionsHolder
import com.eden.orchid.api.registration.OrchidModule
import com.eden.orchid.api.resources.resource.OrchidResource
import com.eden.orchid.api.theme.pages.OrchidPage
import com.google.inject.binder.LinkedBindingBuilder
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Stream
import kotlin.reflect.KClass


fun String?.wrap(width: Int = 80): List<String> {
    val matchList = ArrayList<String>()

    if (this?.isNotBlank() == true) {
        val regex = Pattern.compile("(.{1,$width}(?:\\s|$))|(.{0,$width})", Pattern.DOTALL)
        val regexMatcher = regex.matcher(this)
        while (regexMatcher.find()) {
            val line = regexMatcher.group().trim { it <= ' ' }
            if (line.isNotBlank()) {
                matchList.add(line)
            }
        }
    }


    return matchList
}

@Suppress(SuppressedWarnings.UNUSED_PARAMETER)
fun String.logSyntaxError(
    extension: String,
    lineNumberNullable: Int?,
    lineColumn: Int?,
    errorMessage: String? = "",
    cause: Throwable? = null
) {
    val lineNumber = lineNumberNullable ?: 0
    val lines = this.lines()
    val linesBeforeStart = (lineNumber - 3).coerceAtLeast(0)
    val linesBeforeEnd = (lineNumber - 1).coerceAtLeast(0)
    val linesAfterEnd = (lineNumber + 5).coerceAtMost(lines.size)

    val linesBefore = "   |" + lines.subList(linesBeforeStart, linesBeforeEnd).joinToString("\n   |")
    val errorLine = "#{\$0|fg('RED')}-->|#{\$0|reset}" + lines[linesBeforeEnd]
    val linesAfter = "   |" + lines.subList(lineNumber, linesAfterEnd).joinToString("\n   |")

    val formattedMessage = """
        |_|   |.$extension error
        |_|   |    Reason: $errorMessage
        |_|   |    Cause: ${cause?.toString() ?: "Unknown cause"}
        |_|   |
        |_|$linesBefore
        |_|$errorLine
        |_|$linesAfter
        """.trimMargin("|_|")

    Clog.tag("Template error").e("\n$formattedMessage")
}

// string conversions
infix fun String.from(mapper: String.() -> Array<String>): Array<String> {
    return mapper(this)
}

infix fun Array<String>.to(mapper: Array<String>.() -> String): String {
    return mapper(this)
}

infix fun Array<String>.with(mapper: String.() -> String): Array<String> {
    return this.map { mapper(it) }.toTypedArray()
}

// "from" mappers
fun String.camelCase(): Array<String> {
    return StringUtils.splitByCharacterTypeCamelCase(this)
}

fun String.camelCase(mapper: String.() -> String): Array<String> {
    return camelCase().with(mapper)
}

fun String.words(): Array<String> {
    return StringUtils.splitByWholeSeparator(this, null)
}

fun String.words(mapper: String.() -> String): Array<String> {
    return words().with(mapper)
}

fun String.snakeCase(): Array<String> {
    return StringUtils.splitByWholeSeparator(this, "_")
}

fun String.snakeCase(mapper: String.() -> String): Array<String> {
    return snakeCase().with(mapper)
}

fun String.dashCase(): Array<String> {
    return StringUtils.splitByWholeSeparator(this, "-")
}

fun String.dashCase(mapper: String.() -> String): Array<String> {
    return dashCase().with(mapper)
}

fun String.filename(): Array<String> {
    return this
        .words()
        .flatMap {
            it.dashCase().toList()
        }
        .flatMap {
            it.snakeCase().toList()
        }
        .flatMap {
            it.camelCase().toList()
        }
        .toTypedArray()
}

fun String.filename(mapper: String.() -> String): Array<String> {
    return filename().with(mapper)
}

// "to" mappers
fun Array<String>.pascalCase(): String {
    return map {
        it.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }.joinToString(separator = "")
}

fun List<String>.pascalCase(): String {
    return toTypedArray().pascalCase()
}

fun Array<String>.pascalCase(mapper: String.() -> String): String {
    return map(mapper).pascalCase()
}

fun Array<String>.camelCase(): String {
    return pascalCase().replaceFirstChar { it.lowercase(Locale.getDefault()) }
}

fun List<String>.camelCase(): String {
    return toTypedArray().camelCase()
}

fun Array<String>.camelCase(mapper: String.() -> String): String {
    return map(mapper).camelCase()
}

fun Array<String>.words(): String {
    return joinToString(separator = " ")
}

fun List<String>.words(): String {
    return toTypedArray().words()
}

fun Array<String>.words(mapper: String.() -> String): String {
    return map(mapper).words()
}

fun Array<String>.snakeCase(): String {
    return joinToString(separator = "_") { it.uppercase(Locale.getDefault()) }
}

fun List<String>.snakeCase(): String {
    return toTypedArray().snakeCase()
}

fun Array<String>.snakeCase(mapper: String.() -> String): String {
    return map(mapper).snakeCase()
}

fun Array<String>.dashCase(): String {
    return joinToString(separator = "-")
}

fun List<String>.dashCase(): String {
    return toTypedArray().dashCase()
}

fun Array<String>.dashCase(mapper: String.() -> String): String {
    return map(mapper).dashCase()
}

fun Array<String>.slug(): String {
    return dashCase().lowercase(Locale.getDefault())
}

fun List<String>.slug(): String {
    return toTypedArray().slug()
}

fun Array<String>.slug(mapper: String.() -> String): String {
    return map(mapper).slug()
}

fun Array<String>.titleCase(): String {
    return joinToString(separator = " ", transform = { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } })
}

fun List<String>.titleCase(): String {
    return toTypedArray().titleCase()
}

fun Array<String>.titleCase(mapper: String.() -> String): String {
    return map(mapper).titleCase()
}

// "with" mappers
fun String.urlSafe(): String {
    return replace("\\s+".toRegex(), "-").replace("[^\\w-_]".toRegex(), "")
}

fun String.urlSafe(mapper: String.() -> String): String {
    return urlSafe().mapper()
}

// Better Kotlin Module registration
// ---------------------------------------------------------------------------------------------------------------------

// bind
inline fun <reified T : Any> OrchidModule.bind(): LinkedBindingBuilder<T> {
    return this._bind(T::class.java)
}

// addToSet
inline fun <reified T : Any> OrchidModule.addToSet(vararg objectClasses: KClass<out T>) {
    this.addToSet(T::class.java, *(objectClasses.map { it.java }.toTypedArray()))
}

inline fun <reified T : Any, reified IMPL : T> OrchidModule.addToSet() {
    this.addToSet(T::class.java, IMPL::class.java)
}

inline fun <reified T : Any> OrchidModule.addToSet(vararg objects: T) {
    this.addToSet(T::class.java, *objects)
}

// Better dynamic object resolution
// ---------------------------------------------------------------------------------------------------------------------

inline fun <reified T : Any> OrchidContext.resolve(): T {
    return this.resolve(T::class.java)
}

inline fun <reified T : Any> OrchidContext.resolve(named: String): T {
    return this.resolve(T::class.java, named)
}

inline fun <reified T : Any> OrchidContext.resolveSet(): Set<T> {
    return this.resolveSet(T::class.java)
}

fun findPageByServerPath(pages: Stream<OrchidPage>, path: String): OrchidPage? {
    val requestedPath = OrchidUtils.normalizePath(path)

    return pages
        .filter { page -> page.reference.pathOnDisk == requestedPath }
        .findFirst()
        .orElse(null)
}

fun InputStream?.readToString(): String? = this?.bufferedReader()?.use { it.readText() }
fun String?.asInputStream(): InputStream = ByteArrayInputStream((this ?: "").toByteArray(Charsets.UTF_8))

fun OptionsHolder.extractOptionsFromResource(context: OrchidContext, resource: OrchidResource): Map<String, Any?> {
    val data = resource.embeddedData
    extractOptions(context, data)
    return data
}

inline fun <reified U> Any?.takeIf(): U? {
    return if (this is U) this else null
}

inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) block()
    return this
}

fun <T> T.debugger(): T {
    return this
}
