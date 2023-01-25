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
@WebServlet(name = "AddStar", urlPatterns = "/api/add-star")
public class AddStar extends HttpServlet {

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
            String starName = request.getParameter("starName");

            Integer starDOB;
            if (request.getParameter("starDOB").isEmpty()) {
                starDOB = null;
            }
            else {
                starDOB = Integer.parseInt(request.getParameter("starDOB"));
            }

            if (starName.isEmpty()) {
                System.out.println("star name not provided");
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Star name must be provided");

                response.getWriter().write(responseJsonObject.toString());
                response.setStatus(200);

                return;
            }

            // Declare our statement
            Statement statement = conn.createStatement();

            ResultSet idMax = statement.executeQuery("select max(id) from stars"); // get max id from stars
            idMax.next();
            String myMaxId = idMax.getString(1);
            myMaxId = "nm" + String.valueOf(Integer.parseInt(myMaxId.substring(2)) + 1);

            idMax.close();

            String insert = "INSERT INTO stars VALUES (?, ?, ?)";

            PreparedStatement insertStatement = conn.prepareStatement(insert);

            insertStatement.setString(1, String.valueOf(myMaxId));
            insertStatement.setString(2, starName);
            if (starDOB == null) {
                insertStatement.setNull(3, Types.INTEGER);
            } else {
                insertStatement.setInt(3, starDOB);
            }

            System.out.println("inserting star into DB");
            // Perform the update
            int rs = insertStatement.executeUpdate();

            statement.close();

            JsonObject responseJsonObject = new JsonObject();

            if (rs != 0) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Actor successfully added to database");
            }
            else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect username and password");

                // Log to localhost log
                request.getServletContext().log("Update not executed");
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
