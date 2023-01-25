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
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "DashboardLogin", urlPatterns = "/api/dashboard-login")
public class DashboardLogin extends HttpServlet {

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        System.out.println("Dashboard Login API accessed");

        // get values from post request
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String employeesQuery = "SELECT * " +
                    "FROM employees as e " +
                    "WHERE e.email = ?";

            PreparedStatement employeesStatement = conn.prepareStatement(employeesQuery);

            employeesStatement.setString(1, email);

            System.out.println("executing login query");
            // Perform the query
            ResultSet rs = employeesStatement.executeQuery();

            // Iterate through each row of rs
            rs.next();

            String encryptedPassword = rs.getString("password"); // compare password to encrypted password
            boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

            JsonObject responseJsonObject = new JsonObject();

            if (success) {
                request.getSession().setAttribute("user", email);

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
            else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect username and password");

                // Log to localhost log
                request.getServletContext().log("Login failed");
            }

            rs.close();
            statement.close();

            response.getWriter().write(responseJsonObject.toString());

            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            System.out.println(e);
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
