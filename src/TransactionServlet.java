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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "TransactionServlet", urlPatterns = "/api/processTransaction")
public class TransactionServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbmaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNum = request.getParameter("creditCardNum");
        String creditCardExp = request.getParameter("creditCardExp");
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(creditCardNum);
        System.out.println(creditCardExp);

        HttpSession session = request.getSession();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String creditCardQuery =
                    "SELECT * " +
                    "FROM creditcards as c " +
                    "WHERE c.id = ? " +
                    "and c.firstName = ? " +
                    "and c.lastName = ? " +
                    "and c.expiration = ?;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(creditCardQuery);

            // Set Parameters
            statement.setString(1, creditCardNum);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, creditCardExp);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            rs.next();

            if (rs.getString("firstName").equals(firstName)) {

                ArrayList<JsonObject> previousItems = (ArrayList<JsonObject>) session.getAttribute("previousItems");

                synchronized (previousItems) {
                    //previousItems.add(item);
                    for (int i = 0; i < previousItems.size(); i++) {
                        JsonObject current = previousItems.get(i);

                        for (int j = 0; j < Integer.parseInt(current.get("quantity").toString()); j++) {
                            String movie = current.get("item").toString().replaceAll("^\"+|\"+$", "");
                            String email = session.getAttribute("user").toString();

                            System.out.println(movie);
                            System.out.println(email);


                            // ---- customer id query
                            String getCustomerIdQuery = "SELECT * " +
                                            "FROM customers as c " +
                                            "WHERE c.email = ?;";

                            // Declare our statement
                            PreparedStatement getCustomerIdStatement = conn.prepareStatement(getCustomerIdQuery);

                            // Set Parameters
                            getCustomerIdStatement.setString(1, email);

                            // Perform the query
                            ResultSet getCustomerIdRS = getCustomerIdStatement.executeQuery();

                            // get customer id and store it in variable
                            getCustomerIdRS.next();
                            String customerId = getCustomerIdRS.getString("id");

                            System.out.println("Finished customer query");

                            // ---- movie id query
                            String getMovieIdQuery = "SELECT * " +
                                    "FROM movies as m " +
                                    "WHERE m.title = ?;";

                            // Declare our statement
                            PreparedStatement getMovieIdStatement = conn.prepareStatement(getMovieIdQuery);

                            // Set Parameters
                            getMovieIdStatement.setString(1, movie);

                            // Perform the query
                            ResultSet getMovieIdRS = getMovieIdStatement.executeQuery();

                            // get customer id and store it in variable
                            getMovieIdRS.next();
                            String movieId = getMovieIdRS.getString("id");

                            System.out.println("Finished movie query");


                            // ---- add to sales table
                            String addSale = "INSERT INTO sales (customerId, movieId, saleDate)" +
                                    "VALUES (? , ?, CURRENT_DATE());";

                            // Declare our statement
                            PreparedStatement addSaleStatement = conn.prepareStatement(addSale);

                            // Set Parameters
                            addSaleStatement.setInt(1, Integer.parseInt(customerId));
                            addSaleStatement.setString(2, movieId);

                            // Perform the query
                            int saleExecution = addSaleStatement.executeUpdate();

                            System.out.println("Finished sales insertion");

                            getCustomerIdStatement.close();
                            getMovieIdStatement.close();
                            addSaleStatement.close();

                            getCustomerIdRS.close();
                            getMovieIdRS.close();
                        }
                    }

                    session.setAttribute("previousItems", new ArrayList<>());

                    out.write("Success");


                }
            }
            else {
                out.write("Failed");
            }

            rs.close();
            statement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            System.out.println(e);
            out.write("Failure");

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(200);
        } finally {
            out.close();
        }
    }
}
