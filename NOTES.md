# NOTES

I started by running the task search and checking the same flow in the React
code, Spring controller, and SQL query. The first bug I fixed was in the search
condition. The query was missing brackets around the title/description search.
Because of that, archived tasks could show up and the status filter did not
always work. I fixed the runtime repository query and updated the H2 and Oracle
reference queries to use the same condition.

While checking the API, I also found a `Thread.sleep` in every search request.
It makes an empty search feel slow for no useful reason and holds up a server
thread. I removed it and added proper 400 responses for bad status, page, and
page-size values instead of letting them become 500 errors.

Another issue I found is pagination. Right now the API loads all matching tasks
and then picks one page in Java. That is okay for the sample data, but it will
not be okay when the table grows. I changed it so the database returns only the
requested page using `Pageable` and a count query.

I also fixed a few frontend issues: loading now ends after an error, an older
search response cannot replace a newer one, and changing a filter resets the
list to page 1.

I checked the changes with focused API requests: status filtering, archive
exclusion, page results, invalid input, and an empty search. I kept bigger work,
such as authentication, replacing H2, and redesigning the pagination UX, out of
this small exercise. I used Codex to inspect the flow and reproduce issues, but
I reviewed the SQL and code paths myself.
