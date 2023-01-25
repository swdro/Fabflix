import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import static java.util.Collections.max;
// Files:
// 0. XML files to parse: main.xml, actors.xml, casts.xml
// 1. Log File for errors/inconsistencies
// 2. movie insert file
// 3. genres insert file
// 4. gim insert file
// 5. stars insert file
// 6. sim insert file

public class Parser {
    HashMap<String, String> moviesMap = new HashMap<>(); // used to map fid in the XML to a movieId in the db table
    HashMap<String, HashSet<String>> starsMap = new HashMap<>(); // used to map the actor name in the XML to an actor id in the db table and have multiple if the actor name occurs multiple times
    String loginUser = "dev";
    String loginPasswd = "Fabflix!@122B";
    String loginUrl = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";
    Connection connection = null;

    private void parseMains() throws Exception {
        //Create Files
        FileWriter f1 = new FileWriter("inconsistencyReport.txt");
        PrintWriter inconsistencyLog = new PrintWriter(f1);
        FileWriter f2 = new FileWriter("movieData.txt");
        PrintWriter movieData = new PrintWriter(f2);
        FileWriter f3 = new FileWriter("genreData.txt");
        PrintWriter genreData = new PrintWriter(f3);
        FileWriter f4 = new FileWriter("gimData.txt");
        PrintWriter gimData = new PrintWriter(f4);

        HashMap<Movie, String> movies = loadMovies(); // Load entries from movies table and map the title,director,genre : id
        HashMap<String, Integer> genres = loadGenres(); // load entries from genres table {genreName: genreId}
        HashMap<String, String> ctcodeToGenre = new HashMap<>(); // map the ctcodes to genres to use when parsing genres
        ctcodeToGenre.put("ctxx", "Uncategorized");
        ctcodeToGenre.put("actn", "Violence");
        ctcodeToGenre.put("advt", "Adventure");
        ctcodeToGenre.put("avga", "Avant Garde");
        ctcodeToGenre.put("biop", "Biography");
        ctcodeToGenre.put("camp", "Camp");
        ctcodeToGenre.put("cart", "Animation");
        ctcodeToGenre.put("cnr", "Cops and Robbers");
        ctcodeToGenre.put("cnrb", "Cops and Robbers");
        ctcodeToGenre.put("comd", "Comedy");
        ctcodeToGenre.put("disa", "Disaster");
        ctcodeToGenre.put("docu", "Documentary");
        ctcodeToGenre.put("dram", "Drama");
        ctcodeToGenre.put("epic", "Epic");
        ctcodeToGenre.put("faml", "Family");
        ctcodeToGenre.put("hist", "History");
        ctcodeToGenre.put("horr", "Horror");
        ctcodeToGenre.put("musc", "Musical");
        ctcodeToGenre.put("myst", "Mystery");
        ctcodeToGenre.put("noir", "Black");
        ctcodeToGenre.put("porn", "Pornography");
        ctcodeToGenre.put("romt", "Romance");
        ctcodeToGenre.put("scfi", "Sci-Fi");
        ctcodeToGenre.put("s.f.", "Sci-Fi");
        ctcodeToGenre.put("sprt", "Sport");
        ctcodeToGenre.put("surl", "Surreal");
        ctcodeToGenre.put("susp", "Thriller");
        ctcodeToGenre.put("tv", "TV Show");
        ctcodeToGenre.put("tvser", "TV Series");
        ctcodeToGenre.put("tvmini", "TV Miniseries");
        ctcodeToGenre.put("west", "Western");
        ctcodeToGenre.put("fant", "Fantasy");
        String nextMovieId = max(movies.values());
        Document dom;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse("stanford-movies/mains243.xml");
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
            return;
        }

        Element documentElement = dom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                String director = null;
                Element element = (Element) nodeList.item(i);
                NodeList subList = element.getElementsByTagName("director");
                if (subList != null) {
                    for (int j = 0; j < subList.getLength(); j++) {
                        Element subElement = (Element) subList.item(j);
                        director = getTextValue(subElement, "dirname");
                        if (director == null) {
                            director = getTextValue(subElement, "dirn");
                        }
                    }
                }
                NodeList filmList = element.getElementsByTagName("films");
                if (filmList != null) {
                    for (int j = 0; j < filmList.getLength(); j++) {
                        Element filmElement = (Element) filmList.item(j);
                        NodeList filmsList = filmElement.getElementsByTagName("film");
                        if (filmsList != null) {
                            for (int k = 0; k < filmsList.getLength(); k++) {
                                Element subElement = (Element) filmsList.item(k);
                                String fid = (getTextValue(subElement, "fid") != null) ? getTextValue(subElement, "fid") : getTextValue(subElement, "filmed") ;
                                String title = getTextValue(subElement, "t");
                                String year = getTextValue(subElement, "year");
                                if (fid == null || fid.trim().equals("") || director == null || director.trim().equals("") || title == null || title.trim().equals("") || year == null || year.trim().equals("")) {
                                    inconsistencyLog.printf("Movie with fid=%s, title=%s, year=%s, director=%s cannot be added because one of the fields is null or empty\n", fid, title, year, director);
                                    continue;
                                }
                                try {
                                    Integer.parseInt(year.trim());
                                } catch(NumberFormatException e) {
                                    inconsistencyLog.printf("Movie with fid=%s, title=%s, year=%s, director=%s cannot be added because year is not a valid integer\n", fid, title, year, director);
                                    continue;
                                }
                                Movie movie = new Movie(title.trim(), Integer.parseInt(year.trim()), director.trim());
                                String movieId = null;
                                if (moviesMap.containsKey(fid.trim().toLowerCase())) { // We already have this fid so no more movies should map to this one and if they do we'll leave them out
                                    inconsistencyLog.printf("We have already added a movie with fid=%s, so we will not add %s\n", fid.trim().toLowerCase(), movie);
                                    continue;
                                } else if (movies.containsKey(movie)) { // If this movie is already in our database just add it to the moviesMap in case there are additional genres we find
                                    inconsistencyLog.printf("%s with fid=%s is already in our database\n", movie, fid.trim().toLowerCase());
                                    movieId = movies.get(movie);
                                    moviesMap.put(fid.trim().toLowerCase(), movieId);
                                } else {
                                    nextMovieId = nextMovieId.substring(0, 2) + String.format("%07d", (Integer.parseInt(nextMovieId.substring(2)) + 1));
                                    movieId = nextMovieId;
                                    moviesMap.put(fid.trim().toLowerCase(), movieId);
                                    movieData.printf("%s|%s|%s|%s\n", movieId, movie.getTitle(), movie.getYear(), movie.getDirector());
                                }
                                NodeList genreList = subElement.getElementsByTagName("cats");
                                HashSet<String> movieGenres = new HashSet<>();
                                if (genreList != null) {
                                    for (int l = 0; l < genreList.getLength(); l++) {
                                        Element genresElement = (Element) genreList.item(l);
                                        NodeList genresList = genresElement.getElementsByTagName("cat");
                                        if (genresList != null) {
                                            for (int m = 0; m < genresList.getLength(); m++) {
                                                String genre = (genresList.item(m).getFirstChild() != null) ? genresList.item(m).getFirstChild().getNodeValue() : null;
                                                if (genre != null) {
                                                    String fullGenreName = ctcodeToGenre.get(genre.toLowerCase().trim());
                                                    fullGenreName = (fullGenreName == null && genres.containsKey(genre.trim())) ? genre.trim() : fullGenreName;
                                                    if (fullGenreName == null) {
                                                        inconsistencyLog.printf("genre=%s for %s with fid=%s is inconsistent with the mapping provided so it will not be added as a genre\n", genre, movie, fid.trim().toLowerCase());
                                                        continue;
                                                    }
                                                    if (!genres.containsKey(fullGenreName)) {
                                                        // INSERT GENRE INTO GENRES TABLE
                                                        genres.put(fullGenreName, max(genres.values()) + 1);
                                                        genreData.printf("%s|%s\n", genres.get(fullGenreName), fullGenreName);
                                                    }
                                                    if (!movieGenres.contains(fullGenreName)) {
                                                        gimData.printf("%s|%s\n", genres.get(fullGenreName), movieId);
                                                        movieGenres.add(fullGenreName);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // DO LOAD DATA on the generated files for movies, genres, genres_in_movies;
        inconsistencyLog.close();
        movieData.close();
        genreData.close();
        gimData.close();
        insertData("movieData.txt", "movies");
        insertData("genreData.txt", "genres");
        insertData("gimData.txt", "genres_in_movies");
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            textVal = nodeList.item(0).getFirstChild().getNodeValue();
        }
        return textVal;
    }

    private void insertData(String file, String table) throws Exception{
        if (connection == null) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        }
        Statement statement = connection.createStatement();

        String query = String.format("LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '|' LINES TERMINATED BY '\n'", file, table);
        int count = statement.executeUpdate(query);
        System.out.println("INSERTED " + count + " ROWS TO " + table);
        statement.close();
    }

    private HashMap<String, Integer> loadGenres() throws Exception {
        HashMap<String, Integer> genres = new HashMap<>();
        if (connection == null) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        }
        Statement statement = connection.createStatement();

        String query = "SELECT * FROM genres";
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            genres.put(rs.getString(2), rs.getInt(1));
        }
        rs.close();
        statement.close();
        System.out.println("LOADED " + genres.size() + " genres from the db");
        return genres;
    }

    private HashMap<Movie, String> loadMovies() throws Exception {
        HashMap<Movie, String> movies = new HashMap<>();
        if (connection == null) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        }
        Statement statement = connection.createStatement();

        String query = "SELECT * FROM movies";
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            Movie movie = new Movie(rs.getString(2), rs.getInt(3), rs.getString(4));
            movies.put(movie, rs.getString(1));
        }
        rs.close();
        statement.close();
        System.out.println("LOADED " + movies.size() + " movies from the db");
        return movies;
    }

    private void parseActors() throws Exception{
        //Create Files
        FileWriter f1 = new FileWriter("inconsistencyReport.txt", true);
        PrintWriter inconsistencyLog = new PrintWriter(f1);
        FileWriter f2 = new FileWriter("starData.txt");
        PrintWriter starData = new PrintWriter(f2);
        HashMap<Star, String> stars = loadStars();

        String nextStarId = max(stars.values());
        Document dom;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse("stanford-movies/actors63.xml");
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
            return;
        }

        Element documentElement = dom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String starName = getTextValue(element, "stagename");
                String dob = getTextValue(element, "dob");
                if (starName == null || starName.trim().equals("")) {
                    inconsistencyLog.printf("Star name is null or empty and has birthYear=%s so we are not adding it\n", dob);
                    continue;
                }
                Integer birthYear = null;
                try {
                    if (dob != null) {
                        birthYear = Integer.parseInt(dob.trim());
                    }
                } catch(NumberFormatException e) { birthYear=null;}
                Star star = new Star(starName.trim(), birthYear);
                if (stars.containsKey(star)) {
                    inconsistencyLog.printf("%s is a duplicate entry\n", star);
                    if (starsMap.get(star.getName()) == null) {
                        starsMap.put(star.getName(), new HashSet<>());
                    }
                    starsMap.get(star.getName()).add(stars.get(star));
                    continue;
                } else {
                    nextStarId = nextStarId.substring(0, 2) + String.format("%07d", (Integer.parseInt(nextStarId.substring(2)) + 1));
                    if (starsMap.get(star.getName()) == null) {
                        starsMap.put(star.getName(), new HashSet<>());
                    }
                    starsMap.get(star.getName()).add(nextStarId);
                    stars.put(star, nextStarId);
                    starData.printf("%s|%s|%s\n", nextStarId, star.getName(), (star.getYear() == null) ? "\\N" : star.getYear());
                }
            }
        }
        inconsistencyLog.close();
        starData.close();
        insertData("starData.txt", "stars");
    }

    private HashMap<Star, String> loadStars() throws Exception{
        HashMap<Star, String> stars = new HashMap<>();
        if (connection == null) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        }
        Statement statement = connection.createStatement();

        String query = "SELECT * FROM stars";
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            Star star = new Star(rs.getString(2), rs.getInt(3));
            stars.put(star, rs.getString(1));
        }
        rs.close();
        statement.close();
        System.out.println("LOADED " + stars.size() + " stars from the db");
        return stars;
    }

    private void parseCasts() throws Exception{
        FileWriter f1 = new FileWriter("inconsistencyReport.txt", true);
        PrintWriter inconsistencyLog = new PrintWriter(f1);
        FileWriter f2 = new FileWriter("simData.txt");
        PrintWriter simData = new PrintWriter(f2);
        FileWriter f3 = new FileWriter("starData2.txt");
        PrintWriter starData = new PrintWriter(f3);
        HashMap<String, HashSet<String>> sim = loadSIM();
        HashMap<Star, String> stars = loadStars();
        String nextStarId = max(stars.values());

        Document dom;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse("stanford-movies/casts124.xml");
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
            return;
        }

        Element documentElement = dom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("m");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String fid = getTextValue(element, "f");
                String starName = getTextValue(element, "a");
                if (starName == null || starName.trim().equals("s a") || fid == null || !moviesMap.containsKey(fid.trim().toLowerCase())) {
                    inconsistencyLog.printf("Star name is null/s a starName=%s or Movie with fid=%s does not exist so we are not inserting\n", starName, fid);
                    continue;
                }
                if (!starsMap.containsKey(starName.trim())) { // Create the star if they don't exist
                    Star star = new Star(starName.trim(), null); // the year is null because we don't know it
                    if (stars.containsKey(star)) {
                        if (starsMap.get(star.getName()) == null) {
                            starsMap.put(star.getName(), new HashSet<>());
                        }
                        starsMap.get(star.getName()).add(stars.get(star));
                    } else {
                        nextStarId = nextStarId.substring(0, 2) + String.format("%07d", (Integer.parseInt(nextStarId.substring(2)) + 1));
                        if (starsMap.get(star.getName()) == null) {
                            starsMap.put(star.getName(), new HashSet<>());
                        }
                        starsMap.get(star.getName()).add(nextStarId);
                        stars.put(star, nextStarId);
                        starData.printf("%s|%s|%s\n", nextStarId, star.getName(), "\\N");
                    }
                }
                if (starsMap.get(starName.trim()) != null && starsMap.get(starName.trim()).size() > 1) {
                    inconsistencyLog.printf("Star with starName=%s maps to multiple starIds(%s) so we cannot insert a stars_in_movie entry for the Movie with movieId=%s\n", starName, starsMap.get(starName.trim()), moviesMap.get(fid.trim()));
                    continue;
                }
                String movieId = moviesMap.get(fid.trim().toLowerCase());
                String starId = null;
                for (String s: starsMap.get(starName.trim())) {
                    starId = s;
                }
                if (sim.get(movieId) != null && sim.get(movieId).contains(starId)) {
                    inconsistencyLog.printf("There is already a stars_in_movies entry with movieId=%s and starId=%s\n", movieId, starId);
                    continue;
                }
                if (sim.get(movieId) == null) {
                    sim.put(movieId, new HashSet<>());
                }
                sim.get(movieId).add(starId);
                simData.printf("%s|%s\n", starId, movieId);
            }
        }
        inconsistencyLog.close();
        simData.close();
        starData.close();
        insertData("starData2.txt", "stars");
        insertData("simData.txt", "stars_in_movies");
        if (connection != null) {
            connection.close();
        }
    }

    private HashMap<String, HashSet<String>> loadSIM() throws Exception{
        HashMap<String, HashSet<String>> sims = new HashMap<>();
        if (connection == null) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        }
        Statement statement = connection.createStatement();

        String query = "SELECT * FROM stars_in_movies";
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            String starId = rs.getString(1);
            String movieId = rs.getString(2);
            if (!sims.containsKey(movieId)) {
                sims.put(movieId, new HashSet<>());
            }
            sims.get(movieId).add(starId);
        }
        rs.close();
        statement.close();
        System.out.println("LOADED " + sims.size() + " stars_in_movies rows from the db");
        return sims;
    }

    public static void main(String[] args) throws Exception{
        Parser p = new Parser();
        System.out.println("BEGINNING TO PARSE");
        p.parseMains();
        p.parseActors();
        p.parseCasts();
        System.out.println("DONE PARSING, inconsistency report can be found in inconsistencyReport.txt");
    }
}
