# Package org.audux.bgg
Core package containing the [BggClient] used for all interactions with this library.

# Package org.audux.bgg.common
Enums and classes shared between several requests and responses.

# Package org.audux.bgg.request
Contains all code to build requests to BGG. [Request] is the main request object returned when 
calling most APIs, however requests that support pagination might return a specific 
[PaginatedRequest] instead.

# Package org.audux.bgg.response
Mostly data classes that wrap the XML response from the BGG APIs. [Response] wraps all data classes 
in order to catch erroneous responses. The [Response] object has [Response.data] which contains the 
parsed (successful) response and [Response.error] is set when the response could not be parsed. The
latter may happen, for example, when the API returns HTML or an errors like `Guild not found` etc.

See the [org.audux.bgg.BggClient] for all the requests and associated response data classes.

