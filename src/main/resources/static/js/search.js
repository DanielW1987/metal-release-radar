/**
 * Send ajax request to search for an artist
 * @returns {boolean}
 */
function search(){
    toggleLoader("searchResultsContainer");

    const url = new URL(window.location.href);
    const query = url.searchParams.get("query");
    const page = url.searchParams.get("page");
    const size = url.searchParams.get("size");

    if (!validateSearch(query, page, size)) {
        const message = "No results could be found for the given query: " + query;
        validationOrAjaxFailed(message, 'searchResultsContainer');
        return false;
    }

    const parameter =
        {
            "query" : query,
            "page" : page,
            "size" : size
        };

    $.ajax({
        method: "GET",
        url: "/rest/v1/artists/search",
        data: parameter,
        dataType: "json",
        success: function(searchResponse){
            buildResults(query, searchResponse);
            toggleLoader("searchResultsContainer");
        },
        error: function(){
            const message = "No results could be found for the given query: " + query;
            validationOrAjaxFailed(message, 'searchResultsContainer');
        }
    });

    return false;
}

/**
 * Basic validation for search parameters
 * @returns {boolean}
 * @param query The search query
 * @param page  The wanted page
 * @param size  Elements per page
 */
function validateSearch(query, page, size) {
    return query && !Number.isNaN(page) && !Number.isNaN(size) && page > 0 && size > 0;
}

/**
 * Builds HTML with results and pagination
 * @param query           The user's query
 * @param searchResponse  JSON response
 */
function buildResults(query, searchResponse) {
    createResultCards(searchResponse);
    createNavigationElement(query, searchResponse);
}

/**
 * Builds HTML for the result cards
 * @param searchResponse
 */
function createResultCards(searchResponse){
    jQuery.each(searchResponse.searchResults, function (i, searchResult) {
        const card = document.createElement('div');
        card.className = "card";

        const cardBody = document.createElement('div');
        cardBody.className = "card-body";

        const headingElement = document.createElement('h3');
        headingElement.innerText = searchResult.artistName;
        cardBody.append(headingElement);
        card.append(cardBody);

        if (searchResult.thumb !== ""){
            const thumbElement = document.createElement('img');
            thumbElement.alt = 'Thumb for ' + searchResult.artistName;
            thumbElement.src = searchResult.thumb;
            cardBody.append(thumbElement);
        }

        const artistIdElement = document.createElement('p');
        artistIdElement.innerText = searchResult.id;
        cardBody.append(artistIdElement);

        const artistDetailsElement = document.createElement('a');
        artistDetailsElement.href = "/artists/" + searchResult.id;
        artistDetailsElement.text = "Details for " + searchResult.artistName;
        cardBody.append(artistDetailsElement);

        const breakElement = document.createElement('br');
        cardBody.append(breakElement);

        const followArtistButtonElement = createFollowArtistButton(searchResult.artistName,
          searchResult.id, searchResult.isFollowed);
        cardBody.append(followArtistButtonElement);

        document.getElementById('searchResultsContainer').append(card);
    });
}

/**
 * Builds HTML for pagination
 * @param query           The user's query
 * @param searchResponse  The json response
 */
function createNavigationElement(query, searchResponse) {
    const navElement = document.createElement("nav");
    const listElement = document.createElement("ul");
    listElement.className = "pagination pagination-sm justify-content-end";
    navElement.append(listElement);

    // Previous link
    createPreviousOrNextItem(query, searchResponse, listElement, true);
    // Page links
    for (let index = 1; index <= searchResponse.pagination.totalPages; index++) {
        createPageLinks(query, searchResponse, index, listElement);
    }
    // Next link
    createPreviousOrNextItem(query, searchResponse, listElement, false);

    document.getElementById('searchResultsContainer').append(navElement);
}

/**
 * Creates pagination page links and appends them to the given element
 * @param query             The user's query
 * @param searchResponse    Search result object
 * @param index             Index number of the link
 * @param element           Element to add page links to
 */
function createPageLinks(query, searchResponse, index, element) {
    const listItem = document.createElement("li");
    listItem.className = "page-item";

    if (index === searchResponse.pagination.currentPage)
        listItem.classList.add("active");

    const link = document.createElement("a");
    link.className = "page-link";
    link.href = "/artists/search?query=" + query + "&page=" + index
                + "&size=" + searchResponse.pagination.itemsPerPage;
    link.text = String(index);

    listItem.append(link);
    element.append(listItem);
}

/**
 * Create the previos or next pagination butteon
 * @param query             The user's query
 * @param searchResponse    Search result object
 * @param element           Element to append to
 * @param previous          True if previous shall be created, false for next
 */
function createPreviousOrNextItem(query, searchResponse, element, previous) {
    const item = document.createElement("li");
    item.className = "page-item";

    let text;
    let targetPage;
    let symbol;

    if (previous){
        text = "Previous";
        targetPage = searchResponse.pagination.currentPage - 1;
        symbol = "\u00AB";
        item.classList.add("prev");
        if (searchResponse.pagination.currentPage === 1)
            item.classList.add("disabled");

    } else {
        text = "Next";
        targetPage = searchResponse.pagination.currentPage + 1;
        symbol = "\u00BB";
        item.classList.add("next");
        if (searchResponse.pagination.currentPage === searchResponse.pagination.totalPages)
            item.classList.add("disabled");
    }

    const link = document.createElement("a");
    link.setAttribute('aria-label', text);
    link.className = "page-link";
    link.href = "/artists/search?query=" + query + "&page=" + targetPage
                + "&size=" + searchResponse.pagination.itemsPerPage;

    let span = document.createElement("span");
    span.setAttribute('aria-hidden', true);
    span.textContent = symbol;
    link.append(span);

    span = document.createElement("span");
    span.className = "sr-only";
    span.textContent = text;
    link.append(span);

    item.append(link);
    element.append(item);
}