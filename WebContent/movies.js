/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

let params = {};

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
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    // Update filters to reflect selected
    let sortByFirst = getParameterByName("sortByFirst");
    let ratingSort = getParameterByName("ratingSort");
    let titleSort = getParameterByName("titleSort");
    let itemsPerPage = getParameterByName('itemsPerPage');

    $("#sort-by-first option[value=" + sortByFirst + "]").attr('selected', 'selected');
    $("#rating-sort option[value=" + ratingSort + "]").attr('selected', 'selected');
    $("#title-sort option[value=" + titleSort + "]").attr('selected', 'selected');
    $("#items-per-page option[value=" + itemsPerPage + "]").attr('selected', 'selected');

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieListContainer = jQuery(".list-container");
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(parseInt(itemsPerPage), resultData.length); i++) {

        movieListContainer.append("<hr>");
        movieListContainer.append("<div class='list-card' id='movie" + i + "'></div>");
        let movieCard = jQuery("#movie" + i);

        let movieTitle = resultData[i]["data"]["movie_title"];
        let movieYear = resultData[i]["data"]["movie_year"];
        let movieRating = resultData[i]["data"]["movie_rating"] ? resultData[i]["data"]["movie_rating"] : "N/A";
        let movieDirector = resultData[i]["data"]["movie_director"];
        let movieGenres = resultData[i]["data"]["first_three_genres"];
        let movieStars = resultData[i]["data"]["first_three_stars"];

        movieCard.append("<h3><a class='main-theme' href='single-movie.html?id=" +
            resultData[i]["data"]["movie_id"] + "' \>" + movieTitle + "</h3>");
        movieCard.append("<p><b>Year</b>: " + movieYear + "</p>");
        movieCard.append("<p><b>Rating</b>: " + movieRating + "</p>");
        movieCard.append("<p><b>Director</b>: " + movieDirector + "</p>");

        let firstThreeGenres = [];
        for (let j = 0; j < movieGenres.length; j++) {
            firstThreeGenres.push("<a href='/fabflix/movies.html?genre=" + movieGenres[j]["genre_name"] + "&sortByFirst=rating&ratingSort=ASC" +
                "&titleSort=ASC&itemsPerPage=25&page=1' class='main-theme'>" + movieGenres[j]["genre_name"] + "</a>");
        }
        firstThreeGenres = firstThreeGenres.join(", ");
        movieCard.append("<p><b>Genres</b>: " + firstThreeGenres + "</p>");


        let firstThreeStars = [];
        for (let j = 0; j < movieStars.length; j++) {
            firstThreeStars.push('<a href="single-star.html?id=' + movieStars[j]['star_id'] +
                '" class="main-theme">'
                + movieStars[j]['star_name'] +
                '</a>');
        }
        firstThreeStars = firstThreeStars.join(", ");
        movieCard.append("<p><b>Stars</b>: " + firstThreeStars + "</p>");
        movieCard.append("<button type='button' class='btn btn-light' id='movie" + i +
            "' onclick='addToCart(this.parentElement.childNodes[0].innerText, this)'>Add to Cart</button>");
        movieCard.append("<span> $3.99</span>");
    }

    // Makes the HTTP GET request and registers on success updates the prev and next links
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/moviesSession", // Setting request url, which is mapped by SingleMovieServlet in SingleMovieServlet.java
        success: (data) => {
            if (parseInt(getParameterByName('page')) * parseInt(getParameterByName('itemsPerPage')) < parseInt(data['totalResults'])) {
                $("#next").attr("href", buildPaginationURL(parseInt(getParameterByName('page'))+1));
            }
            if (parseInt(getParameterByName('page')) > 1) {
                $("#prev").attr("href", buildPaginationURL(parseInt(getParameterByName('page'))-1));
            }
            $("#movie-list-link").attr("href", '/fabflix/movies.html?' + data['queryString']);
        } // Setting callback function to handle data returned successfully by the SingleMovieServlet
    });
    $("#page-number").text("page " + getParameterByName('page'));
}

function addToCart(title, element) {
    let id = element.getAttribute("id");
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data: {
            item: title,
            quantity: 1
        },
        method: "POST", // Setting request method
        url: 'api/shoppingCart', // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => {
            console.log(resultData);
        } // Setting callback function to handle data returned successfully by the StarsServlet
    });
    if (!$("#" + id + "added").length) {
        $(element).parent().append($("<div id='" + id + "added' class='shopping-cart-notification'>Added to Cart!</div>"));
    }
};

function constructParamsObj() {
    console.log(getParameterByName('genre'));
     if (getParameterByName('genre')) {
         params['genre'] = getParameterByName('genre');
     }
     if (getParameterByName('character')) {
         params['character'] = getParameterByName('character');
     }
     if (getParameterByName('title')) {
         params['title'] = getParameterByName('title');
     }
     if (getParameterByName('year')) {
         params['year'] = getParameterByName('year');
     }
     if (getParameterByName('director')) {
         params['director'] = getParameterByName('director');
     }
     if (getParameterByName('starName')) {
         params['starName'] = getParameterByName('starName');
     }
}

function buildNewFilterURL() {
    constructParamsObj();
    let URL = "/fabflix/movies.html?";
    for ([key, value] of Object.entries(params)) {
        URL += key + "=" + value + "&";
    }
    URL += `sortByFirst=${$("#sort-by-first").val()}&ratingSort=${$("#rating-sort").val()}&titleSort=${$("#title-sort").val()}&itemsPerPage=${$("#items-per-page").val()}&page=1`;
    return URL;
}

function buildPaginationURL(page) {
    constructParamsObj();
    let URL = "/fabflix/movies.html?";
    for ([key, value] of Object.entries(params)) {
        URL += key + "=" + value + "&";
    }
    URL += `sortByFirst=${getParameterByName('sortByFirst')}&ratingSort=${getParameterByName('ratingSort')}&titleSort=${getParameterByName('titleSort')}&itemsPerPage=${getParameterByName('itemsPerPage')}&page=${page}`;
    return URL;
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

//Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/movies' + location.search, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

// Call constructParamsObj then loop through and build url and then update filters
$("form").submit(function(e) {
    e.preventDefault();
    window.location.replace(buildNewFilterURL());
});

$("#logout-button").click(function(e) {
    e.preventDefault();
    jQuery.ajax({
        method: "Get",
        url: "api/logout",
        success: (resultData) => {
            location.reload();
        }
    });
});