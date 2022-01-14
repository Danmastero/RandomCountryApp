package com.example.randomcountryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener{

    TextView randomCountry;
    TextView randomCapital;
    TextView shakeInformation;
    Button button;
    Button searchButton;
    ImageView imageView;
    String mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setTheme(R.style.splashCreenTheme);


        setContentView(R.layout.activity_main);

        randomCountry = findViewById(R.id.randomCountry);
        randomCapital = findViewById(R.id.capital);
        shakeInformation = findViewById(R.id.txtShakeStatus);
        button = findViewById(R.id.button);
        searchButton = findViewById(R.id.search);
        imageView = findViewById(R.id.imgFlag);



        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                callApi();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                searchInGoogle();
            }
        });

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);








    }

    private void searchInGoogle() {
        String urlString = "https://www.google.pl/maps/place/Politechnika+Białostocka/@53.1171019,23.1444932,17z/data=!3m1!4b1!4m5!3m4!1s0x471ffbfb9415aee5:0x1f6449ccc6c966f7!8m2!3d53.1170987!4d23.1466872?hl=pl";
        //String urlString = "https://www.google.com/search?q=" + randomCapital.getText().toString();
        if(mapView != null) {
            urlString = mapView;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.andro/id.chrome");
        try {
            this.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            this.startActivity(intent);
        }
    }

    public static final String TAG = "MyTag";
    StringRequest stringRequest; // Assume this exists.
    RequestQueue requestQueue;  // Assume this exists.
    private void callApi() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

// Start the queue
        requestQueue.start();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        Random r = new Random();
        int random = r.nextInt(250);




        String url ="https://restcountries.com/v3.1/all";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       //Log.d("Response", response.substring(0,500));
                        String capital = "Brak stolicy";
                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            JSONObject object= jsonarray.getJSONObject(random);
                            String country = getCountryNameFromApi(object, random);

                            if(country != "Antarctica") {
                                 capital = getCapitalName(object);
                            }
                            mapView = getMapUrl(object);
                            String flagImageUrl = getFlag(object);
                            Picasso.get().load(flagImageUrl).resize(1200,550).into(imageView);

                            randomCountry.setText(country);
                            if(capital != null)
                                randomCapital.setText(capital);
                            else randomCapital.setText("Ten kraj nie ma stolicy");

                            shakeInformation.setText("Potrząśnij telefonem, żeby wylosować kraj");


                        }catch (JSONException err){
                            Log.d("Error", err.toString());
                        }


                    }

                    private String getFlag(JSONObject object) throws JSONException {
                        Log.d("flags", object.toString());
                        JSONObject flagArray = object.getJSONObject("flags");
                        String png = flagArray.getString("png");
                        Log.d("linkToFlag", png);
                        return png;
                    }

                    private String getCapitalName(JSONObject object) throws JSONException {
                        Log.d("object", object.toString());
                        JSONArray capitalArray = object.getJSONArray("capital");
                        String capital = (String) capitalArray.get(0);
                        Log.d("capital", capital.toString());
                        return  capital;
                    }
                    private String getMapUrl(JSONObject object) throws JSONException {
                        Log.d("googleMaps", object.toString());
                        JSONObject flagArray = object.getJSONObject("maps");
                        String mapLink = flagArray.getString("googleMaps");
                        Log.d("linkToMap", mapLink);
                        return mapLink;
                    }

                    @NonNull
                    private String getCountryNameFromApi(JSONObject object, int random) throws JSONException {
                        Log.d("object", object.toString());
                        JSONObject jsonArray = object.getJSONObject("name");
                        String name = jsonArray.getString("common");
                        Log.d("lalala", name);
                        return name;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "ERROR");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
// Set the tag on the request.
        stringRequest.setTag(TAG);

// Add the request to the RequestQueue.
        requestQueue.add(stringRequest);


    }


    @Override
    protected void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    @Override
    public void hearShake() {
    callApi();

        shakeInformation.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake));
        shakeInformation.setText("Losuję kraj!");

    }


}