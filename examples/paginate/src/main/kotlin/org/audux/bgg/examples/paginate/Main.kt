package org.audux.bgg.examples.paginate

import java.util.Scanner
import kotlin.math.min
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.response.Comment
import org.audux.bgg.response.Thing

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        BggClient.setLoggerSeverity(BggClient.Severity.Info)

        runBlocking {
            println("Retrieving Scythe...")
            val pageSize = 100 // defaults is 100.
            val lastPage = 25 // Limit the number of comments / pages that is paginated

            // Retrieve Scythe and it's comments (12_000+ as of this writing) but limit to only 25
            // pages.
            val thingsResponse =
                BggClient.things(arrayOf(169786), comments = true, pageSize = pageSize)
                    .paginate(toPage = lastPage)
                    .call()

            // Throw and log error when data is not returned, can also use #isError() or
            // #isSuccess()
            if (thingsResponse.data == null) {
                throw Exception("Something went wrong: ${thingsResponse.error}.")
            }

            // Assume all is well, production code should instead iterate over things.
            val scythe = thingsResponse.data?.things!![0]

            // Print the game name and description.
            println(summary(scythe))

            // Set up the `CLI`
            val scanner = Scanner(System.`in`)
            val comments = scythe.comments!!
            var currentPage = 0

            // Keep going.
            while (true) {
                print("(n)ext page[${currentPage + 1}/$lastPage] or (q)uit: ")
                val command = scanner.nextLine()
                if (arrayOf("q", "quit").contains(command.lowercase())) break

                // If we're not yet at the last page keep printing comments.
                if (currentPage < lastPage) {
                    val nextPage = currentPage + 1
                    comments.comments
                        .subList(
                            currentPage * pageSize,
                            min(nextPage * pageSize, comments.comments.size)
                        )
                        .forEach { println(commentLine(it)) }
                    currentPage = nextPage
                } else {
                    // Fin.
                    println("THE END")
                    break
                }
            }
        }
    }

    private fun commentLine(comment: Comment) =
        """
        ------------------------
        ${comment.username}             ${comment.rating?.let { "($it/10)" }}
        ${comment.value}
        
    """
            .trimIndent()

    private fun summary(thing: Thing) =
        """
        ${thing.name}[${thing.id}]      ${thing.releaseDate}
        ${thing.description}
        
        Number of comments: ${thing.comments?.totalItems}
        
    """
            .trimIndent()
}
