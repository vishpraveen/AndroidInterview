fun main() {
    //7 unique Genres [Dystopian,Political Fiction,Science Fiction,Fiction,Classic,Historical,Coming-of-Age]
    //5 Unique Titles [1984,To Kill a Mockingbird,The Great Gatsby,Brave New World,The Catcher in the Rye]
    val booksList = listOf(
        Book("1984", listOf("Dystopian", "Political Fiction", "Science Fiction")),
        Book("To Kill a Mockingbird", listOf("Fiction", "Classic", "Historical")),
        Book("The Great Gatsby", listOf("Fiction", "Classic")),
        Book("Brave New World", listOf("Dystopian", "Science Fiction")),
        Book("The Catcher in the Rye", listOf("Fiction", "Classic", "Coming-of-Age"))
    )

    // Create a map from genre to sorted list of titles
    val genreToTitlesMap = booksList
        .flatMap { book -> book.genres.map { genre -> genre to book.title } }  // Create pairs of (genre, title)
//        ["Dystopian: 1984",
//        "Political Fiction: 1984",
//        .....
//         "Fiction: To Kill a Mockingbird"
//        ...
//         "Fiction: The Great Gatsby"
//        ] // @flatMap returns -> List<Pair<String,String>>
        .groupBy({ it.first }, { it.second }) // Group by genre, collecting titles
//          Dystopian: [1984, Brave New World]
//          Political Fiction: [1984]
//          Science Fiction: [1984, Brave New World]
//          Fiction: [To Kill a Mockingbird, The Great Gatsby, The Catcher in the Rye]
//          Classic: [To Kill a Mockingbird, The Great Gatsby, The Catcher in the Rye]
//          Historical: [To Kill a Mockingbird]
//          Coming-of-Age: [The Catcher in the Rye] //@groupBy returns ->Map<String,List<String>>

        .mapValues { (_, titles) -> titles.sorted() }  // Sort titles for each genre
//          Dystopian: [1984, Brave New World]
//          Political Fiction: [1984]
//          Science Fiction: [1984, Brave New World]
//          Fiction: [The Catcher in the Rye, The Great Gatsby, To Kill a Mockingbird]
//          Classic: [The Catcher in the Rye, The Great Gatsby, To Kill a Mockingbird]
//          Historical: [To Kill a Mockingbird]
//          Coming-of-Age: [The Catcher in the Rye]

    // Print the map
    genreToTitlesMap.forEach { (genre, titles) ->
        println("$genre: ${titles.joinToString()}")
    }
    println("-------------------------------------------------------------------------")

    //2. Print uniques genres sorted & respective book titles in sorted manner
    val sortedGenres = booksList.flatMap { it.genres }.toSet().sorted() // Extract unique genres and sort them

    val genreToTitlesMap2 = sortedGenres.associateWith { genre ->
        booksList.filter { book -> genre in book.genres }.map { it.title }.sorted()
    }
    genreToTitlesMap2.forEach { (genre, titles) ->
        println("$genre: ${titles.joinToString()}")
    }

}
data class Book(
    val title: String,
    val genres: List<String>
)
