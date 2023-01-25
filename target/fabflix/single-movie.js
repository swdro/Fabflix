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
    console.log("handleResult: populating movie info from resultData");
    console.log(resultData);
    // populate the movie info h1
    let movieTitle = jQuery("#movie_title");
    movieTitle.html(resultData[0]["movie_title"]);
    let movieInfo = jQuery("#movie_info");
    // append two html <p> created to the h1 body, which will refresh the page
    movieInfo.append("<p><b>Year</b>: " + resultData[0]["movie_year"] + "</p>");
    movieInfo.append("<p><b>Director</b>: " + resultData[0]["movie_director"] + "</p>");
    movieInfo.append("<p><b>rating</b>: " + (resultData[0]["movie_rating"] ? resultData[0]["movie_rating"] : "N/A") + "</p>");

    // display genres
    let genres = [];
    for (let i = 0; i < resultData[1]["genres"].length; ++i) {
        genres.push("<a href='/fabflix/movies.html?genre=" + resultData[1]["genres"][i]["genre_name"] + "&sortByFirst=rating&ratingSort=ASC" +
            "&titleSort=ASC&itemsPerPage=25&page=1' class='main-theme'>" + resultData[1]["genres"][i]["genre_name"] + "</a>");
    }
    console.log(genres);
    movieInfo.append("<p><b>Genres</b>: " + genres.join(", ") + "</p>");

    // display stars
    let stars = [];
    for (let i = 0; i < resultData[2]["stars"].length; ++i) {
        stars.push('<a href="single-star.html?id=' + resultData[2]['stars'][i]['star_id'] +
            '" class="main-theme">' + resultData[2]['stars'][i]['star_name'] + '</a>');
    }
    console.log(stars);
    movieInfo.append("<p><b>Stars</b>: " + stars.join(", ") + "</p>");
    movieInfo.append("<button type='button' class='btn btn-light' id='add-to-cart' onclick='addToCart()'>Add to Cart</button>");
    movieInfo.append("<span> $3.99</span>");
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
// Get id from URL
let movieId = getParameterByName('id');
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by SingleMovieServlet in SingleMovieServlet.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});

function addToCart() {
    console.log($("#movie_title").html());
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data: {
            item: $("#movie_title").html(),
            quantity: 1
        },
        method: "POST", // Setting request method
        url: 'api/shoppingCart', // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => {
            console.log(resultData);
        } // Setting callback function to handle data returned successfully by the StarsServlet
    });
    if (!$("#added-to-cart-text").length) {
        $("#movie_info").append($("<div id='added-to-cart-text' class='shopping-cart-notification'>Added to Cart!</div>"));
    }
};


$("#logout-button").click(function() {
    jQuery.ajax({
        method: "Get",
        url: "api/logout",
        success: (resultData) => {
            location.reload();
        }
    });
});