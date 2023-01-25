/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    // Makes the HTTP GET request and registers on success updates the Movie List link
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/moviesSession", // Setting request url, which is mapped by SingleMovieServlet in SingleMovieServlet.java
        success: (data) => {
            $("#movie-list-link").attr("href", '/fabflix/movies.html?' + data['queryString']);
        } // Setting callback function to handle data returned successfully by the SingleMovieServlet
    });

    console.log("handleResult: populating star info from resultData");
    console.log(resultData);

    // populate the star info h3
    // find the empty h1 body by id "star_info" and fill in name
    let starName = jQuery("#star_name");
    console.log("starName: " + resultData[0]["star_name"])
    starName.html(resultData[0]["star_name"]);

    let starInfo = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfo.append("<p><b>Date Of Birth</b>: " +
        (resultData[0]["star_dob"] ? resultData[0]["star_dob"] : "N/A") + "</p>");
    // create list of movies that the star is in
    let movies = [];
    for (let i = 0; i < resultData.length; ++i) {
        movies.push('<a class="main-theme" href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]['movie_title'] + '</a>');
    }

    console.log(movies);

    starInfo.append("<p><b>Movies</b>: " + movies.join(", ") + "</p>");
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
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