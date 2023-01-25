import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysql.cj.jdbc.DatabaseMetaData;

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
import java.sql.*;
import java.util.*;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "DatabaseMetaData", urlPatterns = "/api/get-db-metadata")
public class DatabaseMetadata extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("Getting Metadata");

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try {

            String tablesObject = this.getMetadata();

            out.write(tablesObject);

        } catch (Exception e) {

            System.out.println(e);

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    public String getMetadata() throws Exception {

        DriverManager.registerDriver(new com.mysql.jdbc.Driver());

        String loginUser = "dev";
        String loginPassword = "Fabflix!@122B";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword);

        DatabaseMetaData databaseMetaData = (DatabaseMetaData) connection.getMetaData();

        Set<String> tableSet = new HashSet<String>();

        ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
        while(resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            //System.out.println(resultSet.getString("TABLE_NAME"));
            if (!tableName.equals("sys_config")) {
                tableSet.add(tableName);
            }
        }

        resultSet.close();

        JsonArray tablesArray= new JsonArray();

        for (String table: tableSet) {

            ResultSet columns = databaseMetaData.getColumns(null,null, table, null);

            while (columns.next()) {
                JsonObject tableColumns = new JsonObject();

                String columnName = columns.getString("COLUMN_NAME");
                String datatype = columns.getString("TYPE_NAME");

                tableColumns.addProperty("table", table);
                tableColumns.addProperty("name", columnName);
                tableColumns.addProperty("type", datatype);

                tablesArray.add(tableColumns);
            }
            columns.close();
        }

        //String tablesObjectStr = tablesObject.toString().replaceAll("\\\\", "");

        System.out.println(tablesArray);
        return tablesArray.toString();

    }
}
