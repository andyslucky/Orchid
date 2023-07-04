package com.eden.orchid.posts

import clog.Clog
import com.eden.common.util.EdenUtils
import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.generators.FolderCollection
import com.eden.orchid.api.generators.OrchidCollection
import com.eden.orchid.api.generators.OrchidGenerator
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.ImpliedKey
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.options.annotations.StringDefault
import com.eden.orchid.api.resources.resource.StringResource
import com.eden.orchid.api.resources.resourcesource.LocalResourceSource
import com.eden.orchid.api.theme.pages.OrchidPage
import com.eden.orchid.api.theme.pages.OrchidReference
import com.eden.orchid.api.theme.permalinks.PermalinkStrategy
import com.eden.orchid.posts.model.Author
import com.eden.orchid.posts.model.CategoryModel
import com.eden.orchid.posts.model.PostsModel
import com.eden.orchid.posts.pages.AuthorPage
import com.eden.orchid.posts.pages.PostPage
import com.eden.orchid.posts.utils.PostsUtils
import com.eden.orchid.posts.utils.isToday
import com.eden.orchid.utilities.*
import java.time.LocalDate
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@Description("Share your thoughts and interests with blog posts.", name = "Blog Posts")
class PostsGenerator
@Inject
constructor(
    private val permalinkStrategy: PermalinkStrategy
) : OrchidGenerator<PostsModel>(GENERATOR_KEY, Stage.CONTENT) {

    companion object {
        const val GENERATOR_KEY = "posts"
        // Accept both   YEAR - MONTH - DAY - TITLE   or  YEAR - DAY_OF_YEAR - TITLE  
        val pageTitleRegex = Pattern.compile("(\\d{4})-(?:(\\d{1,2})-(\\d{1,2})|(\\d{3}))-([\\w-]+)")
        enum class PageTitleGrp { ALL, YEAR, MONTH, DAY, DAY_OF_YEAR, TITLE }
    }

    @Option
    @ImpliedKey(typeKey = "name")
    @Description(
        "A list of Author objects denoting the 'regular' or known authors of the blog. Authors can also be " +
            "set up from a resource in the `authorsBaseDir`. All known authors will have a page generated for them " +
            "and will be linked to the pages they author. Guest authors may be set up directly in the post " +
            "configuration, but they will not have their own pages."
    )
    lateinit var authors: List<Author>

    @Option
    @ImpliedKey(typeKey = "key")
    @Description(
        "An array of Category configurations, which may be just the path of the category or a full " +
            "configuration object. Categories are strictly hierarchical, which is denoted by the category path. If a " +
            "category does not have an entry for its parent category, an error is thrown and Posts generation " +
            "will not continue."
    )
    lateinit var categories: MutableList<CategoryModel>

    @Option
    @StringDefault("posts")
    @Description("The base directory in local resources to look for blog post entries in.")
    lateinit var baseDir: String

    @Option
    @StringDefault("posts/authors")
    @Description("The base directory in local resources to look for author configs/bios in.")
    lateinit var authorsBaseDir: String

    @Option
    @Description("The configuration for the default category, when no other categories are set up.")
    lateinit var defaultConfig: CategoryModel

    override fun startIndexing(context: OrchidContext): PostsModel {
        val authorPages = getAuthorPages(context)

        if (EdenUtils.isEmpty(categories)) {
            categories.add(defaultConfig)
        }

        val model = PostsModel(context, categories, authorPages)

        val postPages = mutableListOf<PostPage>()

        if (model.validateCategories()) {
            model.categories.values.forEach { categoryModel ->
                categoryModel.first = getPostsPages(context, categoryModel)
                postPages.addAll(categoryModel.first)
            }
        } else {
            Clog.e("Categories are not hierarchical, cannot continue generating posts.")
        }

        model.postPages = postPages
        model.collections = getCollections(model.categories, authorPages)

        return model
    }

    private fun getAuthorPages(context: OrchidContext): List<AuthorPage> {
        val authorPages = ArrayList<AuthorPage>()

        // add Author pages from content pages in the authorsBaseDir
        val resourcesList = context.getDefaultResourceSource(LocalResourceSource, null).getResourceEntries(context, authorsBaseDir, null, false)
        for (entry in resourcesList) {
            val newAuthor = Author()
            val authorName = entry.reference.originalFileName from { dashCase() } to { titleCase() }
            val options = entry.embeddedData.takeIf { it.isNotEmpty() }?.toMutableMap() ?: mutableMapOf<String, Any?>("name" to authorName)

            if (!options.containsKey("name")) {
                options["name"] = authorName
            }

            newAuthor.extractOptions(context, options)

            val authorPage = AuthorPage(entry, newAuthor)
            authorPage.author.authorPage = authorPage
            permalinkStrategy.applyPermalink(authorPage, authorPage.permalink)
            authorPages.add(authorPage)
        }

        // add Author pages from those specified in config.yml
        for (author in this.authors) {
            val authorPage = AuthorPage(
                StringResource(
                    OrchidReference(context, "index.md"),
                    ""
                ),
                author
            )
            authorPage.author.authorPage = authorPage
            permalinkStrategy.applyPermalink(authorPage, authorPage.permalink)
            authorPages.add(authorPage)
        }

        return authorPages
    }

    private fun getPostsPages(context: OrchidContext, categoryModel: CategoryModel): List<PostPage> {
        val baseCategoryPath = OrchidUtils.normalizePath(baseDir + "/" + categoryModel.path)
        val resourcesList = context.getDefaultResourceSource(LocalResourceSource, null).getResourceEntries(context, baseCategoryPath, null, true)

        val posts = ArrayList<PostPage>()

        for (entry in resourcesList) {
            val formattedFilename = PostsUtils.getPostFilename(entry, baseCategoryPath)
            val matcher = pageTitleRegex.matcher(formattedFilename)

            if (matcher.matches()) {
                val title = matcher.group(PageTitleGrp.TITLE.ordinal).from { dashCase() }.to { words {
                    replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                } }
                val post = PostPage(entry, categoryModel, title)

                if (post.publishDate.toLocalDate().isToday()) {
                    if (matcher.group(PageTitleGrp.DAY_OF_YEAR.ordinal).isNullOrEmpty())
                        post.publishDate = LocalDate.of(
                            Integer.parseInt(matcher.group(PageTitleGrp.YEAR.ordinal)),
                            Integer.parseInt(matcher.group(PageTitleGrp.MONTH.ordinal)),
                            Integer.parseInt(matcher.group(PageTitleGrp.DAY.ordinal))
                        ).atStartOfDay()
                    else
                        post.publishDate = LocalDate.ofYearDay(
                            Integer.parseInt(matcher.group(PageTitleGrp.YEAR.ordinal)),
                            Integer.parseInt(matcher.group(PageTitleGrp.DAY_OF_YEAR.ordinal))
                        ).atStartOfDay()
                }

                // TODO: when setting the permalink, check all categories in the hierarchy until you find one
                val permalink = if (!EdenUtils.isEmpty(post.permalink)) post.permalink else categoryModel.permalink
                permalinkStrategy.applyPermalink(post, permalink)
                posts.add(post)
            }
        }

        posts.sortWith(PostsModel.postPageComparator)
        posts.mapIndexed { i, post ->
            if (next(posts, i) != null) {
                post.previous = next(posts, i)
            }
            if (previous(posts, i) != null) {
                post.next = previous(posts, i)
            }
        }

        return posts.filter { !it.isDraft }
    }

    private fun getCollections(
        categories: MutableMap<String?, CategoryModel>,
        authorPages: List<AuthorPage>
    ): List<OrchidCollection<*>> {
        val collectionsList = ArrayList<OrchidCollection<*>>()

        categories.values.forEach {
            if (EdenUtils.isEmpty(it.key)) {
                val collection = FolderCollection(
                    this@PostsGenerator,
                    "blog",
                    it.first as List<OrchidPage>,
                    PostPage::class.java,
                    baseDir
                )
                collection.slugFormat = "{year}-{month}-{day}-{slug}"
                collectionsList.add(collection)
            } else {
                val collection = FolderCollection(
                    this@PostsGenerator,
                    it.key ?: "",
                    it.first as List<OrchidPage>,
                    PostPage::class.java,
                    baseDir + "/" + it.key
                )
                collection.slugFormat = "{year}-{month}-{day}-{slug}"
                collectionsList.add(collection)
            }
        }

        val collection = FolderCollection(
            this@PostsGenerator,
            "authors",
            authorPages,
            AuthorPage::class.java,
            authorsBaseDir
        )
        collectionsList.add(collection)

        return collectionsList
    }

    private fun previous(posts: List<OrchidPage>, i: Int): OrchidPage? {
        if (posts.size > 1) {
            if (i != 0) {
                return posts[i - 1]
            }
        }

        return null
    }

    private fun next(posts: List<OrchidPage>, i: Int): OrchidPage? {
        if (posts.size > 1) {
            if (i < posts.size - 1) {
                return posts[i + 1]
            }
        }

        return null
    }
}
