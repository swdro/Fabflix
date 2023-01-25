package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import java.lang.reflect.Array;
import java.util.ArrayList;

import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.MainPageBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainPageActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "18.220.143.255";
    private final String port = "8443";
    private final String domain = "fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        // TODO: this should be retrieved from the backend server

        Bundle extras = getIntent().getExtras();
        String value;
        Integer page;
        String searchWord;
        if (extras != null) {
            value = extras.getString("key");
            page = extras.getInt("page");
            searchWord = extras.getString("searchWord");
            //The key argument here must match that used in the other activity
        } else {
            value = "";
            page = 1;
            searchWord = "";
        }

        final ArrayList<Movie> movies = new ArrayList<>();

        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        final Button backButton = binding.backButton;
        //assign a listener to call a function to handle the user request when clicking a button
        backButton.setOnClickListener(view -> back());

        final Button next = binding.next;
        final Button previous = binding.prev;

        //assign a listener to call a function to handle the user request when clicking a button
        next.setOnClickListener(view -> navigate("next", searchWord, page));
        previous.setOnClickListener(view -> navigate("prev", searchWord, page));

        try {
            JSONArray movieArray = new JSONArray(value);
            for (int i = 0; i < movieArray.length(); ++i) {
                JSONObject movie = movieArray.getJSONObject(i);
                JSONObject movieData = movie.getJSONObject("data");
                //Log.d("movieData", movieData.toString());
                String title = movieData.get("movie_title").toString();
                String year = movieData.get("movie_year").toString();
                String director = movieData.get("movie_director").toString();
                String rating = movieData.get("movie_rating").toString();
                JSONArray genres = movieData.getJSONArray("first_three_genres");
                JSONArray stars = movieData.getJSONArray("first_three_stars");

                ArrayList<String> genresArr = new ArrayList<String>();
                for (int j = 0; j < genres.length(); ++j) {
                    genresArr.add(genres.getJSONObject(j).get("genre_name").toString());
                    //Log.d("genre", genres.getJSONObject(j).get("genre_name").toString());
                }

                ArrayList<String> starsArr = new ArrayList<String>();
                for (int j = 0; j < stars.length(); ++j) {
                    starsArr.add(stars.getJSONObject(j).get("star_name").toString());
                    //Log.d("star", stars.getJSONObject(j).get("star_name").toString());
                }

                movies.add(new Movie(title, year, director, rating, genresArr, starsArr));
            }
        } catch (Exception e) {
            Log.d("Json Object Exception", e.toString());
        }

        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            finish();
            Intent singleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            singleMoviePage.putExtra("key", value);
            singleMoviePage.putExtra("page", page);
            singleMoviePage.putExtra("searchWord", searchWord);
            singleMoviePage.putExtra("movieTitle", movie.getName());
            singleMoviePage.putExtra("movieDirector", movie.getDirector());
            singleMoviePage.putExtra("movieYear", movie.getYear());
            singleMoviePage.putExtra("movieGenres", movie.getGenres());
            singleMoviePage.putExtra("movieStars", movie.getStars());
            startActivity(singleMoviePage);
            //@SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %s", position, movie.getName(), movie.getYear());
            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("SetTextI18n")
    public void navigate(String pageSelection, String searchWord, Integer page) {
        Log.d("Searching", "query being sent to server");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        if (pageSelection.equals("next")) {
            page += 1;
        }
        else if (pageSelection.equals("prev")) {
            page -= 1;
            if (page <= 0) {
                return;
            }
        }

        // request type is POST
        String finalPage = page.toString();
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?title=" + searchWord + "&sortByFirst=rating&ratingSort=DESC&titleSort=ASC&itemsPerPage=25&page=" + page.toString(),
                (response) -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.                    Log.d("login.success", response);

                    try {
                        //Complete and destroy login activity once successful
                        String value = response;
                        Integer p = Integer.parseInt(finalPage);
                        Log.d("next/prev clicked", p.toString());
                        // initialize the activity(page)/destination
                        if (response.equals("[]")) {
                            Log.d("NextPageError", "Cannot navigate to next page because the result is empty" + p.toString());
                            navigate("prev", searchWord, p);
                            return;
                        }
                        finish();
                        Intent movieListActivity = new Intent(MovieListActivity.this, MovieListActivity.class);
                        movieListActivity.putExtra("key",response);
                        movieListActivity.putExtra("page", p);
                        movieListActivity.putExtra("searchWord", searchWord);
                        // activate the list page.
                        startActivity(movieListActivity);
                    } catch (Exception e) {
                        Log.d("next/prev exception: ", e.toString());
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }

    @SuppressLint("SetTextI18n")
    public void back() {
        finish();
        Intent movieListPage = new Intent(MovieListActivity.this, MainPageActivity.class);
        startActivity(movieListPage);
    }
}