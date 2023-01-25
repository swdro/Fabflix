package edu.uci.ics.fabflixmobile.ui.movielist;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private final ArrayList<Movie> movies;

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView year;
        TextView director;
        TextView rating;
        TextView genres;
        TextView genre1;
        TextView genre2;
        TextView genre3;
        TextView stars;
        TextView star1;
        TextView star2;
        TextView star3;

    }

    public MovieListViewAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.movielist_row, movies);
        this.movies = movies;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the movie item for this position
        Movie movie = movies.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.movielist_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.year = convertView.findViewById(R.id.year);
            viewHolder.director = convertView.findViewById(R.id.director);
            viewHolder.rating = convertView.findViewById(R.id.rating);
            viewHolder.genres = convertView.findViewById(R.id.genres);
            viewHolder.genre1 = convertView.findViewById(R.id.genre1);
            viewHolder.genre2 = convertView.findViewById(R.id.genre2);
            viewHolder.genre3 = convertView.findViewById(R.id.genre3);
            viewHolder.stars = convertView.findViewById(R.id.stars);
            viewHolder.star1 = convertView.findViewById(R.id.star1);
            viewHolder.star2 = convertView.findViewById(R.id.star2);
            viewHolder.star3 = convertView.findViewById(R.id.star3);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.title.setText(movie.getName());
        viewHolder.year.setText(movie.getYear() + "");
        viewHolder.director.setText(movie.getDirector());
        viewHolder.rating.setText(movie.getRating());
        viewHolder.genres.setText("Genres");
        for (int j = 0; j < movie.getGenres().size(); ++j) {
            if (j == 0) {
                viewHolder.genre1.setText(movie.getGenres().get(j));
            }
            else if (j == 1) {
                viewHolder.genre2.setText(movie.getGenres().get(j));
            }
            else if (j == 2) {
                viewHolder.genre3.setText(movie.getGenres().get(j));
            }
        }
        viewHolder.stars.setText("Stars");
        for (int j = 0; j < movie.getStars().size(); ++j) {
            if (j == 0) {
                viewHolder.star1.setText(movie.getStars().get(j));
            }
            else if (j == 1) {
                viewHolder.star2.setText(movie.getStars().get(j));
            }
            else if (j == 2) {
                viewHolder.star3.setText(movie.getStars().get(j));
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }
}