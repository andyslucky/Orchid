rootProject.name = "orchid"

include(":orchid-core")
include(":orchid-test")
include(":docs")

include(":bundles:orchid-all-bundle")
include(":bundles:orchid-blog-bundle")
include(":bundles:orchid-docs-bundle")
include(":bundles:orchid-languages-bundle")

include(":features:orchid-archives-feature")
include(":features:orchid-asciidoc-feature")
include(":features:orchid-azure-feature")
include(":features:orchid-bible-feature")
include(":features:orchid-bitbucket-feature")
include(":features:orchid-changelog-feature")
include(":features:orchid-diagrams-feature")
include(":features:orchid-forms-feature")
include(":features:orchid-github-feature")
include(":features:orchid-gitlab-feature")
include(":features:orchid-groovydoc-feature")
include(":features:orchid-javadoc-feature")
include(":features:orchid-kotlindoc-feature")
include(":features:orchid-kss-feature")
include(":features:orchid-netlify-feature")
include(":features:orchid-netlify-cms-feature")
include(":features:orchid-pages-feature")
include(":features:orchid-plugin-docs-feature")
include(":features:orchid-posts-feature")
include(":features:orchid-presentations-feature")
include(":features:orchid-search-feature")
include(":features:orchid-snippets-feature")
include(":features:orchid-sourcedoc-feature")
include(":features:orchid-swagger-feature")
include(":features:orchid-swiftdoc-feature")
include(":features:orchid-syntax-highlighter-feature")
include(":features:orchid-wiki-feature")
include(":features:orchid-writers-blocks-feature")

include(":themes:orchid-bsdoc-theme")
include(":themes:orchid-copper-theme")
include(":themes:orchid-editorial-theme")
include(":themes:orchid-future-imperfect-theme")

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
        create("testLibs") {
            from(files("./test-libs.versions.toml"))
        }
    }
}
