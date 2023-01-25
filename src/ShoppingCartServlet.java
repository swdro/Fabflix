import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shoppingCart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        response.setContentType("application/json"); // Response mime type
        JsonObject responseJsonObject = new JsonObject();

        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new ArrayList<>();
        }

        // write all the data into the jsonObject
        response.getWriter().write(previousItems.toString());
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        String quantity = request.getParameter("quantity");
        System.out.println(item);
        System.out.println(quantity);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        ArrayList<JsonObject> previousItems = (ArrayList<JsonObject>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new ArrayList<>();

            JsonObject itemAndQuantity = new JsonObject();
            itemAndQuantity.addProperty("item", item);
            itemAndQuantity.addProperty("quantity", Integer.parseInt(quantity));

            previousItems.add(itemAndQuantity);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {

                boolean itemFound = false;

                for (int i = 0; i < previousItems.size(); i++) {
                    JsonObject current = previousItems.get(i);
                    String itemName = current.get("item").toString().replaceAll("^\"+|\"+$", "");
                    if (itemName.equals(item)) {
                        current.addProperty("quantity", (Integer.parseInt(quantity) + Integer.parseInt(current.get("quantity").toString())));
                        itemFound = true;
                        if (Integer.parseInt(current.get("quantity").toString()) <= 0) {
                            previousItems.remove(i);
                        }
                        break;
                    }
                }

                if (!itemFound) {
                    JsonObject itemAndQuantity = new JsonObject();
                    itemAndQuantity.addProperty("item", item);
                    itemAndQuantity.addProperty("quantity", Integer.parseInt(quantity));

                    previousItems.add(itemAndQuantity);
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}
