

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