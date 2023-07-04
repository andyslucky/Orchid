package com.eden.orchid.impl.publication

import clog.Clog
import com.eden.common.util.IOStreamUtils
import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.ValidationError
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.options.validateNotEmpty
import com.eden.orchid.api.publication.OrchidPublisher
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Description(value = "Run arbitrary shell scripts.", name = "Script")
class ScriptPublisher : OrchidPublisher("script") {

    @Option
    @Description("The executable name")
    lateinit var command: List<String>

    @Option
    @Description("The working directory of the script to run")
    lateinit var cwd: String

    override fun validate(context: OrchidContext): List<ValidationError> {
        return super.validate(context) + listOfNotNull(
            validateNotEmpty("command", command)
        )
    }

    override fun publish(context: OrchidContext) {
        val builder = ProcessBuilder()
        builder.command(*command.toTypedArray())
        var directory: String
        if (cwd.isNotBlank()) {
            directory = cwd
            if (directory.startsWith("~")) {
                directory = System.getProperty("user.home") + directory.substring(1)
            }
        } else {
            directory = context.sourceDir
        }
        builder.directory(File(directory))
        Clog.i("[{}]> {}", directory, command.joinToString(" "))

        val process = builder.start()
        val future = Executors
            .newSingleThreadExecutor()
            .submit(IOStreamUtils.InputStreamPrinter(process.inputStream, "Script Publisher"))

        // pause for the process to finish and input to finish printing
        process.waitFor()
        future.get(3, TimeUnit.SECONDS)
    }
}
