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


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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

        request.getServletContext().log("movies servlet");

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        //Update queryString attribute of session for jump functionality
        HttpSession session = request.getSession();
        session.setAttribute("queryString", request.getQueryString());

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            //Update totalResults attribute of session for pagination
            PreparedStatement countStatement = buildQuery(request, true, conn);
            ResultSet resultSet = countStatement.executeQuery();
            resultSet.next();
            session.setAttribute("totalResults", resultSet.getString("totalResults"));
            resultSet.close();

            PreparedStatement statement = buildQuery(request, false, conn);//"SELECT id, title, year, director, rating FROM movies join ratings r on movies.id = r.movieId ORDER BY rating DESC LIMIT 20;";
            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");
                JsonArray firstThreeGenres = new JsonArray();
                JsonArray firstThreeStars = new JsonArray();

                String firstThreeGenreQuery = "SELECT name, id from genres as g, genres_in_movies as gim " +
                        "where g.id = gim.genreId and gim.movieId = ? ORDER BY g.name ASC LIMIT 3;";
                PreparedStatement genreStatement = conn.prepareStatement(firstThreeGenreQuery);
                genreStatement.setString(1, movie_id);
                ResultSet genreRs = genreStatement.executeQuery();

                while (genreRs.next()) {
                    JsonObject genreObject = new JsonObject();
                    String genreId = genreRs.getString("id");
                    String genreName = genreRs.getString("name");
                    genreObject.addProperty("genre_id", genreId);
                    genreObject.addProperty("genre_name", genreName);
                    firstThreeGenres.add(genreObject);
                }
                String firstThreeStarsQuery = "SELECT s.name, s.id, count(sim2.starId) as frequency FROM stars as s, stars_in_movies as sim1, stars_in_movies as sim2 WHERE s.id = sim1.starId and s.id = sim1.starId and sim1.movieId = ? and sim2.starId = sim1.starId GROUP BY s.name, s.id ORDER BY frequency DESC, s.name ASC LIMIT 3;";
                //String firstThreeStarsQuery = "SELECT s.name, s.id FROM stars as s, stars_in_movies as sim WHERE s.id = sim.starId and sim.movieId = ? LIMIT 3;"
//                String firstThreeStarsQuery = "SELECT s.name, s.id, star_frequency.movieAppearances " +
//                        "FROM stars as s, stars_in_movies as sim, " +
//                        "(SELECT starId, Count(starId) as movieAppearances FROM stars_in_movies as sim " +
//                        "GROUP BY starId) as star_frequency " +
//                        "WHERE s.id = sim.starId and s.id = star_frequency.starId and sim.movieId = ? " +
//                        "ORDER BY star_frequency.movieAppearances DESC, s.name ASC " +
//                        "LIMIT 3;";
                PreparedStatement starStatement = conn.prepareStatement(firstThreeStarsQuery);
                starStatement.setString(1, movie_id);
                ResultSet starRs = starStatement.executeQuery();

                while (starRs.next()) {
                    JsonObject starObject = new JsonObject();
                    String starId = starRs.getString("id");
                    String starName = starRs.getString("name");
                    starObject.addProperty("star_id", starId);
                    starObject.addProperty("star_name", starName);
                    firstThreeStars.add(starObject);
                }


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                JsonObject dataObject = new JsonObject();
                dataObject.addProperty("movie_id", movie_id);
                dataObject.addProperty("movie_title", movie_title);
                dataObject.addProperty("movie_year", movie_year);
                dataObject.addProperty("movie_director", movie_director);
                dataObject.addProperty("movie_rating", movie_rating);
                dataObject.add("first_three_genres", firstThreeGenres);
                dataObject.add("first_three_stars", firstThreeStars);
                jsonObject.addProperty("value", movie_title);
                jsonObject.add("data", dataObject);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

    private PreparedStatement buildQuery(HttpServletRequest request, Boolean paginate, Connection conn) throws Exception{
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String genre = request.getParameter("genre");
        String starName = request.getParameter("starName");
        String character = request.getParameter("character");
        String sortByFirst = request.getParameter("sortByFirst");
        String ratingSort = request.getParameter("ratingSort");
        String titleSort = request.getParameter("titleSort");
        String itemsPerPage = request.getParameter("itemsPerPage");
        String page = request.getParameter("page");
        StringBuilder query = new StringBuilder();
        if (paginate) {
            query.append("SELECT count(distinct(m.id)) as totalResults FROM movies as m LEFT JOIN ratings as r on m.id = r.movieID");
        } else {
            query.append("SELECT distinct(m.id), title, year, director, rating FROM movies as m LEFT JOIN ratings as r on m.id = r.movieID");
        }
        // Add all necessary tables
        if (genre != null) {
            query.append(", genres as g, genres_in_movies as gim");
        }
        if (starName != null) {
            query.append(", stars as s, stars_in_movies as sim");
        }

        // Add WHERE conditions
        query.append(" WHERE TRUE");
        if (character != null) {
            // Use regex to match non alpha-numeric if character.equals("*")
            if (character.equals("*")) {
                // TODO: Double check regex '^[^0-9A-Za-z]' or '^[^[:alnum:]]'
                // First matches stuff like ä but second does not
                query.append(" and m.title REGEXP '^[^[:alnum:]]'");
            } else {
                query.append(" and m.title LIKE ?");
            }
        }
        if (title != null) {
            // If the query contains all non-alphanumeric characters the results will be empty for
            // full-text search so just use LIKE in that case
            String alphanumericTitleString = title.replaceAll("[\\W]+", " ").trim();
            if (alphanumericTitleString.equals("")) {
               query.append(" and m.title LIKE ?");
            } else {
                query.append(" and MATCH (m.title) AGAINST (? IN BOOLEAN MODE)");
            }
        }
        if (year != null) {
            query.append(" and m.year=?");
        }
        if (director != null) {
            // Edit title to escape out any %, _, ', or \ in the user's search and match substrings using %x% pattern
            query.append(" and m.director LIKE ?");
        }
        if (genre != null) {
            query.append(" and gim.movieId = m.id and gim.genreId = g.id and g.name=?");
        }
        if (starName != null) {
            // Edit starName to escape out any %, _, ', or \ in the user's search and match substrings using %x% pattern
            query.append(" and m.id=sim.movieId and s.id=sim.starId and s.name LIKE ?");
        }
        if (!paginate) {
            // Add ORDER BY conditions and verify that ratingSort and titleSort are only ASC or DESC since we can't
            // pass them as parameters to the prepared statement
            if ((ratingSort.equalsIgnoreCase("ASC") || ratingSort.equalsIgnoreCase("DESC")) && (titleSort.equalsIgnoreCase("ASC") || titleSort.equalsIgnoreCase("DESC"))) {
                if (sortByFirst.equals("rating")) {
                    query.append(" ORDER BY r.rating " + ratingSort + ", m.title " + titleSort);
                } else {
                    query.append(" ORDER BY m.title " + titleSort + ", r.rating " + ratingSort);
                }
            }
            // Add LIMIT and OFFSET
            query.append(" LIMIT ? OFFSET ?;");
        } else {
            query.append(";");
        }

        // LOG QUERY
        System.out.println("QUERY: " + query);
        request.getServletContext().log("QUERY = \n" + query);

        PreparedStatement statement = conn.prepareStatement(query.toString());

        int index = 1;

        if (character != null && !character.equals("*")) {
            statement.setString(index, character+"%");
            index++;
        }
        if (title != null) {
            String alphanumericTitleString = title.replaceAll("[\\W]+", " ").trim();
            if (alphanumericTitleString.equals("")) {
                statement.setString(index, buildMatchString(title));
            } else {
                String[] words = alphanumericTitleString.split(" ");
                for (int i = 0; i < words.length; i++) {
                    words[i] = "+" + words[i] + "*";
                }
                System.out.println(index + " " + String.join(" ", words));
                statement.setString(index, String.join(" ", words));
            }
            index++;
        }
        if (year != null) {
            statement.setInt(index, Integer.parseInt(year));
            index++;
        }
        if (director != null) {
            statement.setString(index, buildMatchString(director));
            index++;
        }
        if (genre != null) {
            statement.setString(index, genre);
            index++;
        }
        if (starName != null) {
            statement.setString(index, buildMatchString(starName));
            index++;
        }
        if (!paginate) {
            // Add LIMIT and OFFSET
            statement.setInt(index, Integer.parseInt(itemsPerPage));
            index++;
            statement.setInt(index, ((Integer.parseInt(page) - 1) * Integer.parseInt(itemsPerPage)));
        }

        return statement;
    }

    private String buildMatchString(String string) {
        StringBuilder stringBuilder = new StringBuilder("%");

        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (character == '%') {
                stringBuilder.append("\\%");
            } else if (character == '_'){
                stringBuilder.append("\\_");
            } else if (character == '\'') {
                stringBuilder.append("''");
            } else if (character == '\\') {
                stringBuilder.append("\\");
            } else {
                stringBuilder.append(character);
            }
        }
        stringBuilder.append("%");

        return stringBuilder.toString();
    }
}
