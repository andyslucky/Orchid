package com.eden.orchid.sourcedoc

import com.eden.orchid.strikt.pageWasRendered
import com.eden.orchid.testhelpers.TestResults
import strikt.api.Assertion
import java.util.*

const val separator = "{\"type\": \"separator\"}"

fun Assertion.Builder<TestResults>.withDefaultSourcedocPages(): Assertion.Builder<TestResults> {
    return this
        .pageWasRendered("/assets/css/orchidSourceDoc.css") { }
        .pageWasRendered("/favicon.ico") { }
}

fun themeMenuModulesSetup(type: String): String {
    return """
        |{
        |    "type": "sourcedocModules",
        |    "moduleType": "${type}doc",
        |    "asSubmenu": true,
        |    "submenuTitle": "${type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Modules"
        |}
        """.trimMargin()
}

fun themeMenuKindSetup(type: String, nodeKind: String, name: String? = null): String {
    return if (name != null) {
        """
        |{
        |    "type": "sourcedocPages",
        |    "moduleType": "${type}doc",
        |    "moduleName": "$name",
        |    "node": "$nodeKind",
        |    "asSubmenu": true,
        |    "submenuTitle": "Module ${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} ${
            type.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }doc ${nodeKind.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
        |}
        """.trimMargin()
    } else {
        """
        |{
        |    "type": "sourcedocPages",
        |    "moduleType": "${type}doc",
        |    "node": "$nodeKind",
        |    "asSubmenu": true,
        |    "submenuTitle": "${type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}doc ${
            nodeKind.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }"
        |}
        """.trimMargin()
    }
}

fun themeMenuKindSetup(type: String, nodeKinds: List<String>, name: String? = null): String {
    return nodeKinds.joinToString(",") { themeMenuKindSetup(type, it, name) }
}

fun themeMenuAllKindsSetup(type: String, nodeKind: String, name: String? = null): String {
    return if (name != null) {
        """
        |{
        |    "type": "sourcedocPages",
        |    "moduleType": "${type}doc",
        |    "moduleName": "$name",
        |    "asSubmenu": true,
        |    "submenuTitle": "Module ${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} ${
            type.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }doc ${nodeKind.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
        |}
        """.trimMargin()
    } else {
        """
        |{
        |    "type": "sourcedocPages",
        |    "moduleType": "${type}doc",
        |    "asSubmenu": true,
        |    "submenuTitle": "${type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}doc ${
            nodeKind.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }"
        |}
        """.trimMargin()
    }
}

fun themeMenuAllKindsSetup(type: String, nodeKinds: List<String>, name: String? = null): String {
    return nodeKinds.joinToString(",") { themeMenuAllKindsSetup(type, it, name) }
}

fun sourceDocPagesSetup(): String {
    return """
        |{
        |   "pages": {
        |       "summaryComponents": [
        |           {
        |               "type": "sourceDocSummary"
        |           }
        |       ],
        |       "menu": [
        |           {
        |               "type": "sourcedocPageLinks",
        |               "includeItems": false
        |           },
        |           {
        |               "type": "separator"
        |           },
        |           {
        |               "type": "sourcedocPageLinks",
        |               "includeItems": true
        |           },
        |           {
        |               "type": "separator"
        |           },
        |           {
        |               "type": "sourcedocPageLinks",
        |               "includeItems": true,
        |               "itemTitleType": "SIGNATURE"
        |           }
        |       ]
        |   }
        |}
        |""".trimMargin()
}
