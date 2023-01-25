package edu.uci.ics.fabflixmobile.ui.mainpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.MainPageBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

import java.util.HashMap;
import java.util.Map;


public class MainPageActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private EditText searchvalue;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "18.220.143.255";
    private final String port = "8443";
    private final String domain = "fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainPageBinding binding = MainPageBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        searchvalue = binding.searchvalue;
        final Button searchButton = binding.search;

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> search());
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        Log.d("Searching", "query being sent to server");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?title=" + searchvalue.getText().toString() + "&sortByFirst=rating&ratingSort=DESC&titleSort=ASC&itemsPerPage=25&page=1",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.                    Log.d("login.success", response);

                    try {
                        Log.d("login.success", response);
                        //Complete and destroy login activity once successful
                        finish();
                        String value = response;
                        Integer page = 1;
                        String searchWord = searchvalue.getText().toString();
                        Log.d("res", response);
                        // initialize the activity(page)/destination
                        Intent MovieListPage = new Intent(MainPageActivity.this, MovieListActivity.class);
                        MovieListPage.putExtra("key",value);
                        MovieListPage.putExtra("page", page);
                        MovieListPage.putExtra("searchWord", searchWord);
                        // activate the list page.
                        startActivity(MovieListPage);
                    } catch (Exception e) {
                        Log.d("login exception: ", e.toString());
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

}
