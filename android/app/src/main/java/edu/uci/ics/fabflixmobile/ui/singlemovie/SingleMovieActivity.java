package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.databinding.SingleMovieBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class SingleMovieActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_movie);

        Bundle extras = getIntent().getExtras();

        SingleMovieBinding binding = SingleMovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        TextView title = (TextView) findViewById(R.id.title);
        TextView year = (TextView) findViewById(R.id.year);
        TextView director = (TextView) findViewById(R.id.director);
        TextView genres = (TextView) findViewById(R.id.genres);
        TextView genre1 = (TextView) findViewById(R.id.genre1);
        TextView genre2 = (TextView) findViewById(R.id.genre2);
        TextView genre3 = (TextView) findViewById(R.id.genre3);
        TextView stars = (TextView) findViewById(R.id.stars);
        TextView star1 = (TextView) findViewById(R.id.star1);
        TextView star2 = (TextView) findViewById(R.id.star2);
        TextView star3 = (TextView) findViewById(R.id.star3);

        title.setText(extras.getString("movieTitle"));
        Log.d("movie title", extras.getString("movieTitle"));
        year.setText(extras.getString("movieYear"));
        director.setText(extras.getString("movieDirector"));

        genres.setText("Genres");

        ArrayList<String> movieGenres = extras.getStringArrayList("movieGenres");
        ArrayList<String> movieStars = extras.getStringArrayList("movieStars");

        //genres = binding.genres;

        for (int j = 0; j < movieGenres.size(); ++j) {
            if (j == 0) {
                genre1.setText(movieGenres.get(j));
            }
            else if (j == 1) {
                genre2.setText(movieGenres.get(j));
            }
            else if (j == 2) {
                genre3.setText(movieGenres.get(j));
            }
        }

        //stars = binding.stars;
        stars.setText("Stars");

        for (int j = 0; j < movieStars.size(); ++j) {
            if (j == 0) {
                star1.setText(movieStars.get(j));
            }
            else if (j == 1) {
                star2.setText(movieStars.get(j));
            }
            else if (j == 2) {
                star3.setText(movieStars.get(j));
            }
        }

        String value = extras.getString("key");
        Integer page = extras.getInt("page");
        String searchWord = extras.getString("searchWord");

        final Button backButton = binding.backButton;

        //assign a listener to call a function to handle the user request when clicking a button
        backButton.setOnClickListener(view -> back(value, page, searchWord));
    }

    @SuppressLint("SetTextI18n")
    public void back(String value, Integer page, String searchWord) {
        finish();
        Intent movieListPage = new Intent(SingleMovieActivity.this, MovieListActivity.class);
        movieListPage.putExtra("key", value);
        movieListPage.putExtra("page", page);
        movieListPage.putExtra("searchWord", searchWord);
        startActivity(movieListPage);
    }
}
