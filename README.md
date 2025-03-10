# Module Board Game Geek Client/BGGClient

[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Build](https://github.com/Bram--/bggclient/actions/workflows/ci.yml/badge.svg)](https://github.com/Bram--/bggclient/actions/workflows/ci.yml) [![codecov](https://codecov.io/gh/Bram--/bggclient/graph/badge.svg?token=FJDN8I5FR1)](https://codecov.io/gh/Bram--/bggclient) [![Maven Central](https://img.shields.io/maven-central/v/org.audux.bgg/bggclient.svg)](https://central.sonatype.com/artifact/org.audux.bgg/bggclient)

BggClient is a API client for the Board Game Geek XML APIs. These APIs work for all geek domains,
meaning Board games, video games and RPGs can be retrieved. It works on both the JVM and Android (
24+). Before using the BGG APIs please refer to
the [Terms of Use](https://boardgamegeek.com/wiki/page/XML_API_Terms_of_Use#) for the APIs. Finally

If you're looking for all Board game IDs and some basic information please refer to
[this page](https://boardgamegeek.com/data_dumps/bg_ranks) that contains CSV with all boardgames
instead.

## APIs overview

A short summary of all available APIs/Endpoints is available
in [the documentation](https://bram--.github.io/bggclient/).

## Usage

The library is published
on [MavenCentral as bggclient](https://central.sonatype.com/artifact/org.audux.bgg/bggclient) so
using it is as simple as adding a single line to Gradle.

##### Gradle

```kotlin
implementation("org.audux.bgg:bggclient:1.1.0")
```

##### Maven

```xml

<dependency>
    <groupId>org.audux.bgg</groupId>
    <artifactId>bggclient</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Examples

Below are few short example more complicated examples can be found in
the [examples folder](/Bram--/bggclient/tree/main/examples).

### Simple board game request in Kotlin

Below is a code snippet that calls the `things` XML2 API retrieving info about "Nucleum" including
comments left on the game.

```kotlin
runBlocking {
    val response = BggClient.things(ids = arrayOf(396790), comments = true).call()

    print(response.data?.things!![0].name) // Prints "Nucleum"
}
```

This returns all information about a `Thing` - in this case a board game including any comments
users have left on the thing. Comments are paginated and would only return the last 100.
See [BggClient#things](/Bram--/bggclient/blob/main/src/main/kotlin/org/audux/bgg/BggClient.kt#:~:text=fun%20things)
for the full API. The actual response is `Response<Thing>` which is a wrapper that holds the
response data or holds an
error instead. Depending on what is requested (e.g. are comments included in the request?) the
object is (partially)
filled: [Thing object](/Bram--/bggclient/blob/main/src/main/kotlin/org/audux/bgg/response/Things.kt)

_Note that `call()` needs to be called in a suspense function._

### Simple board game request in Java

Using the library in Java is the same as in Kotlin, however it uses a `CompletableFuture` so the
request and response can be used in Java.

```java
class Example() {
    public static void main(String... arguments) {
        CompletableFuture<Response<Things>> future =
                BggClient.things(
                                /* ids= */ new Integer[]{396790},
                                /* types= */ new ThingType[]{},
                                /* stats= */ false,
                                /* versions= */ false,
                                /* videos= */ false,
                                /* marketplace= */ false,
                                /* comments= */ true,
                                /* ratingComments= */ false)
                        .callAsync();

        // Blocking get - for example purposes only.
        Response<Things> response = future.get(2_000, MILLISECONDS);
    }
}
```

The same as the Kotlin example above; this returns all information about a `Thing` - in this case a
board game including any comments
users have left on the thing. Comments are paginated and would only return the last 100. See
[BggClient#things](/Bram--/bggclient/blob/main/src/main/kotlin/org/audux/bgg/BggClient.kt#:~:text=fun%20things)
for the full API. The actual response is `Response<Thing>` which is a wrapper that holds the
response data or holds an
error instead. Depending on what is requested (e.g. are comments included in the request?) the
object is (partially) filled:
[Thing object](/Bram--/bggclient/blob/main/src/main/kotlin/org/audux/bgg/response/Things.kt)

### Async request

Instead of using a suspend function you can also do a request inline by giving a callback. The
request and response parsing will then be handled using `Dispatchers.IO` and `Dispatchers.DEFAULT`.
The same as the `things` request above but using callAsync() it looks as follows:

```kotlin
BggClient.things(ids = arrayOf(396790), comments = true).callAsync { response ->
    print(response.data?.things!![0].name) // Prints "Nucleum"
}
```

### Pagination

Pagination is completely taken care off by the library, a more in-depth example can be found in the
examples folder. Pagination starts with the initial request i.e. when a `page` parameter is given
that's the start page and the end page can be configured using the `toPage` param. If no `page` and
no `toPage` params are given the library will attempt to paginate all pages.

```kotlin
var response = BggClient.things(ids = arrayOf(396790), comments = true).paginate().call()

// Prints "Loads of comments 524"
println("Loads of comments! ${response.data?.things!![0].comments?.comments}") 
```

The above codes results in 6 requests to BGG, first, the initial request and then 5 parallel
requests to retrieve the resulting pages/comments.

##### Paginated requests:

Not all requests can be paginates (as most of them are not actually paginated in the API nor
need they be). However the following requests can be paginated:

* `forum` - Aggregate/paginates over the `threads` in a forum. Only 50 per request can be
  retrieved which means that large forums could result in a lot of requests.
* `guilds` - Aggregate/paginates over the `guildMembers`. Only 25 members can be returned
  per request.
* `plays` - Aggregates/paginates over the list of `plays` for the given user (id). Only 100 plays
  per request are retrieved.
* `things` - Aggregates/paginates over the list of `comments` or `ratingcomments`. The pageSize can
  be set but has a default and maximum size of 100 comments per request.
* `user` - Aggregates/paginates over the list of `buddies` and `guilds`. The default page size is
  1000 so it's unlikely pagination actually happens when called.

### Sitemaps

Sitemaps are a quick to get IDs of Board games, RPGs, etc. The `sitemapIndex` endpoint contains all
sitemaps for the given domain e.g. `boardgamegeek.com/sitemapindex` has a list of several sitemaps (
UrlSets) that contains links to ALL board game pages.
You can request the index by calling `BggClient#sitemapIndex`, but using the `diffuse` function will
actually for off requests to the contained sitemaps and collect the URLs by 'type'.

```kotlin
val response =
    BggClient.sitemapIndex(Domain.BOARD_GAME_GEEK)
        .diffuse(SitemapLocationType.BOARD_GAMES, SitemapLocationType.BOARD_GAME_EXPANSIONS)
        .call()

println(response.data) 
```

Data contains a (multi) map of types and URLs (`Map<SitemapLocationType, List<SiteMapUrl>>`):

* [BOARD_GAMES] => ["https://boardgamegeek.com/boardgame/2/dragonmaster", ...]
* [BOARD_GAME_EXPANSIONS] => ["https://boardgamegeek.com/boardgameexpansion/1573/banzai", ...)

### Custom configuration

`BggClientConfiguration` allows the client to be configured differently. This allows the user to
increase the maximum number of retries, the maximum number of concurrent requests and how the
exponential delay is calculated. The below example also configured (The already default) request 
limit to 60 request per minute - this appears to be the BGG limit.

```kotlin
BggClient.configure {
    maxConcurrentRequests = 5
    maxRetries = 100
    requestsPerWindowLimit = 60
    requestWindowSize = 60.seconds
}
```

### Logging

Running into errors/faulty responses? Turn on the internal logging for the library to see the
internal workings.

```kotlin
BggClient.setLoggerSeverity(Severity.Verbose)
```
