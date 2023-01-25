

function handleCartResult(resultData) {
    console.log(resultData);

    let paymentContainer = $(".payment-container");

    let total = 0;
    for (let i = 0; i < resultData.length; ++i) {
        total += resultData[i]["quantity"];
        console.log("total: " + total);
    }
    paymentContainer.append("<div id='total-container'>Total: <h2 id='total'>" + (total * 3.99).toFixed(2) + "</h2></div>");
    paymentContainer.append(
        "<form id='payment-form'>" +
            "<br>" +
            "<div>" +
                "<label for='first-name-input'>First Name</label>" +
                "<input name='first-name' class='form-control' id='first-name-input' placeholder='John'>" +
            "</div>" +
            "<br>" +
                "<div>" +
                    "<label for='last-name-input'>Last Name</label>" +
                    "<input name='last-name' class='form-control' id='last-name-input' placeholder='Smith'>" +
                "</div>" +
            "<br>" +
                "<div>" +
                    "<label for='credit-card-num'>Credit Card</label>" +
                    "<input name='credit-card-num' class='form-control' id='credit-card-num'>" +
                "</div>" +
            "<br>" +
                 "<div>" +
                    "<label for='credit-card-exp'>Expiration Date</label>" +
                    "<input name='credit-card-exp' class='form-control' id='credit-card-exp' placeholder='2000-01-01'>" +
                 "</div>" +
            "<br><br>" +
        "</form>"
    );
    paymentContainer.append("<button class='btn btn-primary submit-button' onclick='checkPaymentInfo()'>Purchase</button>");
}

function checkPaymentInfo() {
    jQuery.ajax({
        dataType: "text", // Setting return data type
        method: "Post", // Setting request method
        url: "api/processTransaction", // Setting request url, which is mapped by StarsServlet in Stars.java
        data: {
            "firstName" : $("input[name=first-name]").val(),
            "lastName" : $("input[name=last-name]").val(),
            "creditCardNum": $("input[name=credit-card-num]").val(),
            "creditCardExp": $("input[name=credit-card-exp]").val()
        },
        success: (resultData) => {
            console.log("success function called");
            handleResult(resultData); // Setting callback function to handle data returned successfully by the StarsServlet
        }
    });
}

function handleResult(resultData) {
    console.log(resultData);

    if ((resultData !== "Success") && ($('.credit-card-error').length === 0)) {
        $(".submit-button").before("<div class='credit-card-error'>Incorrect Credit/Debit Card Information</div><br>");
    }
    else {
        window.location.replace('confirmation.html');
    }
}


jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/shoppingCart', // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleCartResult(resultData), // Setting callback function to handle data returned successfully by the StarsServlet
    error: () => {
        console.log("Failed to check credit card info");
        handleCartResult("Failure");
    }
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