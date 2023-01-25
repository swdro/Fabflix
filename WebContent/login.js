/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {

    let resultDataJson = JSON.parse(resultData);

    console.log("login page");
    console.log("email", $("input[name=email]").val());
    console.log("password", $("input[name=password]").val());
    console.log(typeof(resultDataJson));
    console.log(resultDataJson);

    if (resultDataJson["status"] === "success") {
        console.log("successfully logged in");
        window.location.replace('index.html');
    }
    else {
        console.log("cannot log in");
        $(".login-error").remove();
        $(".login-info").append("<div class='login-error'>" + resultDataJson["message"] + "</div>");
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
$("form").submit(function(e) {
    e.preventDefault();
    console.log("form submitted");

    jQuery.ajax({
        dataType: "text", // Setting return data type
        method: "Post", // Setting request method
        url: "form-recaptcha", // Setting request url, which is mapped by StarsServlet in Stars.java
        data: {
            "g-recaptcha-response": grecaptcha.getResponse()
        },
        success: (resultData) => {
            let resultDataJson = JSON.parse(resultData);

            console.log("recaptcha api called");
            if (resultDataJson["status"] === "success") {
                jQuery.ajax({
                    dataType: "text", // Setting return data type
                    method: "Post", // Setting request method
                    url: "api/login", // Setting request url, which is mapped by StarsServlet in Stars.java
                    data: {
                        "email" : $("input[name=email]").val(),
                        "password" : $("input[name=password]").val()
                    },
                    success: (resultData) => {
                        console.log("success function called");
                        handleResult(resultData); // Setting callback function to handle data returned successfully by the StarsServlet
                    }
                });
            }
            else {
                $(".login-error").remove();
                $(".login-info").append("<div class='login-error'>" + resultDataJson["message"] + "</div>");
            }
        }
    });

    console.log("done");
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
