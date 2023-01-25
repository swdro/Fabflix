function handleCartResult(resultData) {
    console.log(resultData);

    let shoppingCartContainer = $(".shopping-cart-container");

    if (resultData.length === 0) {
        shoppingCartContainer.append("<h1 class='empty-cart-text'>Nothing Currently in Shopping Cart</h1>")
    }
    else {
        let total = 0;
        for (let i = 0; i < resultData.length; ++i) {
            shoppingCartContainer.append("<div class='list-card cart-item' id='item" + i + "'></div>");
            let cartItem = jQuery("#item" + i);

            cartItem.append("<div class='cart-item-name'>" +
                "<h1>" + resultData[i]['item']+ "</h1>" +
                "<span>$3.99</span>" +
                "</div>");
            cartItem.append("<div class='cart-quantity'>" +
                "<button class='quantity-inc-dec' onclick='decrementQuantity(this)'>-</button>" +
                "<div id='quantity-" + i + "'>" + resultData[i]['quantity'] + "</div>" +
                "<button class='quantity-inc-dec' onclick='incrementQuantity(this)'>+</button>" +
                "<div id='delete-item' onclick='deleteItem(this)'>" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-x-circle\" viewBox=\"0 0 16 16\">" +
                "<path d=\"M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z\"/>" +
                "<path d=\"M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z\"/>" +
                "</svg>" +
                "</div>" +
                "</div>");

            shoppingCartContainer.append("<hr>");

            total += resultData[i]["quantity"];
            console.log("total: " + total);
        }
        shoppingCartContainer.append("<h2 id='total-container'>Total: <span id='total'>" + (parseFloat(total * 3.99)).toFixed(2) + "</span></h2>");
        shoppingCartContainer.append("<button class='btn btn-primary submit-button' onclick='paymentPageRedirect()'>Checkout</button>");
    }
}

function deleteItem(element) {
    let count = $(element).parent().children()[1].innerHTML;
    let title = $(element).parent()[0].parentElement.children[0].children[0].innerHTML;

    adjustQuantity(title, parseInt(count) * -1);  // make post request to server

    window.location.reload();
}

function incrementQuantity(element) {
    // increment quantity
    let count = $(element).parent().children()[1].innerHTML;
    $(element).parent().children()[1].innerHTML = parseInt(count) + 1;

    let title = $(element).parent()[0].parentElement.children[0].children[0].innerHTML;
    adjustQuantity(title, 1);  // make post request to server

    // adjust total
    $("#total")[0].innerHTML = (parseFloat(parseFloat($("#total")[0].innerHTML) + 3.99)).toFixed(2);
}

function decrementQuantity(element) {
    // decrement quantity
    let count = $(element).parent().children()[1].innerHTML;
    $(element).parent().children()[1].innerHTML = parseInt(count) - 1;

    let title = $(element).parent()[0].parentElement.children[0].children[0].innerHTML;

    adjustQuantity(title, -1);  // make post request to server

    if (parseInt($(element).parent().children()[1].innerHTML) <= 0) {
        window.location.reload();
    }

    // adjust total
    $("#total")[0].innerHTML = (parseFloat(parseFloat($("#total")[0].innerHTML) - 3.99)).toFixed(2);
}


function adjustQuantity(title, count) {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data: {
            item: title,
            quantity: count
        },
        method: "POST", // Setting request method
        url: 'api/shoppingCart', // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => {
            console.log(resultData);
        } // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

function paymentPageRedirect() {
    window.location.replace('payment.html');
}


//Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: 'api/shoppingCart', // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleCartResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
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