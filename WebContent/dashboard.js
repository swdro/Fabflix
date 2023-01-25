/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {

    let resultDataJson = JSON.parse(resultData);

    $(".error-message").remove();
    $(".success-message").remove();
    if (resultDataJson["status"] === "success") {
        console.log("added successfully");
        $(".movie-submit-form").before("<div class='success-message'>" + resultDataJson["message"] + "</div>");
    }
    else {
        console.log("actor not added");
        $(".movie-submit-form").before("<div class='error-message'>" + resultDataJson["message"] + "</div>");
    }
}

function addDatabaseMetadata(md) {
    console.log(md);

    let dashboardInfo = $(".meta-data-table-container");
    dashboardInfo.append("<table class='meta-data-table'>" +
        "<tr>" +
        "<th>TableName</th>" +
        "<th>Column Name/Type</th>" +
        "</tr>" +
        "</table>");

    let currTable = md[0]["table"];
    let currRow = "<tr>" +
        "<td>" + currTable + "</td>";

    for (let i of md) {
        if (currTable != i["table"]) {
            currTable = i["table"];
            currRow += "</tr>";
            $(".meta-data-table").append(currRow);
            currRow = "<tr>" +
                "<td><p>" + currTable + "</p></td>";
        }

        currRow += "<td><p>" + i["name"] + " (" + i["type"] + ") " + "</p></td>";
    }
}

jQuery.ajax({
    method: "Get",
    url: "api/get-db-metadata",
    success: (resultData) => {
        addDatabaseMetadata(resultData);
    }
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
$(".star-submit-form").submit(function(e) {
    e.preventDefault();
    console.log($("input[name=star-name]").val());
    console.log($("input[name=star-dob]").val());
    console.log("form submitted");
    jQuery.ajax({
        dataType: "text", // Setting return data type
        method: "Post", // Setting request method
        url: "api/add-star", // Setting request url, which is mapped by StarsServlet in Stars.java
        data: {
            "starName" : $("input[name=star-name]").val(),
            "starDOB" : $("input[name=star-dob]").val()
        },
        success: (resultData) => {
            console.log("success function called");
            handleResult(resultData); // Setting callback function to handle data returned successfully by the StarsServlet
        }
    });
});

$(".movie-submit-form").submit(function(e) {
    e.preventDefault();
    console.log($("input[name=movie-title]").val());
    console.log($("input[name=movie-year]").val());
    console.log($("input[name=director]").val());
    console.log($("input[name=genre]").val());
    console.log($("input[name=movie-star-name]").val());

    console.log("form submitted");
    jQuery.ajax({
        dataType: "text", // Setting return data type
        method: "Post", // Setting request method
        url: "api/add-movie", // Setting request url, which is mapped by StarsServlet in Stars.java
        data: {
            "movieTitle" : $("input[name=movie-title]").val(),
            "movieYear" : $("input[name=movie-year]").val(),
            "director" : $("input[name=director]").val(),
            "genre" : $("input[name=genre]").val(),
            "star" : $("input[name=movie-star-name]").val()
        },
        success: (resultData) => {
            console.log("success function called");
            handleResult(resultData); // Setting callback function to handle data returned successfully by the StarsServlet
        }
    });
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
