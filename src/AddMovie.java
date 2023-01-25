import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.cj.xdevapi.JsonString;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import org.jasypt.util.password.StrongPasswordEncryptor;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "AddMovie", urlPatterns = "/api/add-movie")
public class AddMovie extends HttpServlet {

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbmaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            System.out.println("adding Star");

            // get values from post request
            String movieTitle = request.getParameter("movieTitle");
            String director = request.getParameter("director");
            String genre = request.getParameter("genre");
            String star = request.getParameter("star");

            Integer movieYear;
            if (request.getParameter("movieYear").isEmpty()) {
                movieYear = null;
            }
            else {
                movieYear = Integer.parseInt(request.getParameter("movieYear"));
            }

            if (movieTitle.isEmpty() || director.isEmpty() || genre.isEmpty() || star.isEmpty()) {
                System.out.println("all necessary parameters not provided");
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "All input is necessary except movie year");

                response.getWriter().write(responseJsonObject.toString());
                response.setStatus(200);

                return;
            }

            // Declare our statement
            Statement statement = conn.createStatement();

            String callProcedureStr = "call add_movie(?,?,?,?,?,?)";

            CallableStatement callProcedure = conn.prepareCall(callProcedureStr);

            callProcedure.setString(1, movieTitle);
            callProcedure.setString(3, director);
            callProcedure.setString(4, genre);
            callProcedure.setString(5, star);
            callProcedure.registerOutParameter(6, Types.VARCHAR);

            if (movieYear == null) {
                callProcedure.setNull(2, Types.INTEGER);
            } else {
                callProcedure.setInt(2, movieYear);
            }

            System.out.println("calling procedure");
            // Perform the update
            callProcedure.execute();

            String message = callProcedure.getString(6);

            statement.close();

            JsonObject responseJsonObject = new JsonObject();

            if (message.equals("exists")) {
                responseJsonObject.addProperty("status", "failure");
                responseJsonObject.addProperty("message", "Movie already exists in database");
            }
            else {
                // successfully added movie
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Movie successfully added to database");
            }

            response.getWriter().write(responseJsonObject.toString());

            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            System.out.println(e);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
