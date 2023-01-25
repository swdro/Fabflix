/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

function handleLookup(query, doneCallback) {
    console.log("Autocomplete search is initiated");
    if (localStorage.getItem(query)) {
        console.log("Using cached results");
        let data = JSON.parse(localStorage.getItem(query));
        console.log(data.map((data) => data['value']));
        doneCallback( { suggestions: data } );
    }
    else {
        console.log("Sending ajax request to server for results");
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/movies?title=" + `${$("#title").val()}` + "&sortByFirst=rating&ratingSort=DESC&titleSort=ASC&itemsPerPage=10&page=1",
            success: function (data) {
                console.log(data.map((data) => data['value']));
                if (localStorage.getItem('CQ') == null) {
                    localStorage.setItem('CQ', JSON.stringify([]));
                }
                let cached_queries = JSON.parse(localStorage.getItem('CQ'));
                if (cached_queries.length >= 10) {
                    localStorage.removeItem(cached_queries.shift());
                }
                cached_queries.push(query)
                localStorage.setItem('CQ', JSON.stringify(cached_queries));
                localStorage.setItem(query, JSON.stringify(data));
                doneCallback({suggestions: data});
            },
            error: function (errorData) {
                console.log("ERROR");
                console.log(errorData);
            }
        });
    }
}

function handleSelectSuggestion(suggestion) {
    window.location.replace("/fabflix/single-movie.html?id=" + suggestion["data"]["movie_id"]);
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function generateGenres(resultData) {
    for (let genre of resultData) {
        $(".genres").append("<a href='movies.html?genre=" + genre + "&sortByFirst=rating&ratingSort=DESC" +
            "&titleSort=ASC&itemsPerPage=25&page=1'>" + genre + "</a>");
    }
}

/**
 * generates links for each letter and numerical character
 */
function generateTitles() {
    for (let char of "123456789*ABCDEFGHIJKLMNOPQRSTUVWXYZ") {
        $(".titles").append("<a href='movies.html?character=" + char + "&sortByFirst=rating&ratingSort=DESC" +
            "&titleSort=ASC&itemsPerPage=25&page=1'>" + char + "</a>");
    }
}

function buildSearchUrl() {
    let URL = "/fabflix/movies.html?";
    if (`${$("#title").val()}` != '') {
        URL += 'title=' + `${$("#title").val()}` + '&';
    }
    if (`${$("#year").val()}` != '') {
        URL += 'year=' + `${$("#year").val()}` + '&';
    }
    if (`${$("#director").val()}` != '') {
        URL += 'director=' + `${$("#director").val()}` + '&';
    }
    if (`${$("#starName").val()}` != '') {
        URL += 'starName=' + `${$("#starName").val()}` + '&';
    }
    URL += "sortByFirst=rating&ratingSort=DESC&titleSort=ASC&itemsPerPage=25&page=1";
    return URL;
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => {
        generateGenres(resultData);
        generateTitles();
    } // Setting callback function to handle data returned successfully by the StarsServlet
});

$('#title').autocomplete({
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback);
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion);
    },
    deferRequestBy: 300,
    minChars: 3,
});

$('#title').keypress(function(event) {
    if (event.keyCode == 13) {
        window.location.replace(buildSearchUrl());
    }
});

$("form").submit(function(e) {
    e.preventDefault();
    window.location.replace(buildSearchUrl());
});

$("#logout-button").click(function() {
    jQuery.ajax({
        method: "Get",
        url: "api/logout",
        success: (resultData) => {
            location.reload();
        }
    });
});