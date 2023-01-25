import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String movieQuery = "SELECT id, title, year, director, rating FROM movies as m LEFT JOIN ratings as r on m.id = r.movieID " +
                    "where m.id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(movieQuery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);

                jsonArray.add(jsonObject);
            }

            // Add movieGenres to JsonArray
            String movieGenresQuery = "SELECT id, name from genres as g, genres_in_movies as gim " +
                    "where gim.movieId = ? and g.id = gim.genreId ORDER BY g.name ASC;";
            PreparedStatement genreStatement = conn.prepareStatement(movieGenresQuery);
            genreStatement.setString(1, id);
            ResultSet genreRs = genreStatement.executeQuery(); // execute above query

            JsonObject movieGenresObject = new JsonObject(); // will contain array of objects
            JsonArray movieGenresObjectsArray = new JsonArray(); // will contain objects

            while (genreRs.next()) {
                JsonObject jsonObject = new JsonObject();
                String genreId = genreRs.getString("id");
                String genreName = genreRs.getString("name");
                jsonObject.addProperty("genre_id", genreId);
                jsonObject.addProperty("genre_name", genreName);
                movieGenresObjectsArray.add(jsonObject);
            }

            movieGenresObject.add("genres", movieGenresObjectsArray);
            jsonArray.add(movieGenresObject);

            // Add movieStars to JsonArray
            String movieStarsQuery = "SELECT s.name, s.id, count(sim2.starId) as frequency FROM stars as s, stars_in_movies as sim1, stars_in_movies as sim2 WHERE s.id = sim1.starId and s.id = sim1.starId and sim1.movieId = ? and sim2.starId = sim1.starId GROUP BY s.name, s.id ORDER BY frequency DESC, s.name ASC;";
//            String movieStarsQuery = "SELECT s.name, s.id, star_frequency.movieAppearances " +
//                    "FROM stars as s, stars_in_movies as sim, " +
//                    "(SELECT starId, Count(starId) as movieAppearances FROM stars_in_movies as sim " +
//                    "GROUP BY starId) as star_frequency " +
//                    "WHERE s.id = sim.starId and s.id = star_frequency.starId and sim.movieId = ? " +
//                    "ORDER BY star_frequency.movieAppearances DESC, s.name ASC;";
            PreparedStatement starStatement = conn.prepareStatement(movieStarsQuery);
            starStatement.setString(1, id);
            ResultSet starRs = starStatement.executeQuery();

            JsonObject movieStarsObject = new JsonObject();
            JsonArray movieStarsObjectArray = new JsonArray();

            while (starRs.next()) {
                JsonObject jsonObject = new JsonObject();
                String starId = starRs.getString("id");
                String starName = starRs.getString("name");
                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);
                movieStarsObjectArray.add(jsonObject);
            }

            movieStarsObject.add("stars", movieStarsObjectArray);
            jsonArray.add(movieStarsObject);

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
