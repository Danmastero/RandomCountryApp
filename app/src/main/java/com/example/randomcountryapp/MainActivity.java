package com.example.randomcountryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener{

    TextView randomCountry;
    TextView capital;
    Button button;
    Button searchButton;
    String[] COUNTRIES = {"Poland", "Ukraine", "Germany"};

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomCountry = findViewById(R.id.randomCountry);
        capital = findViewById(R.id.capital);
        button = findViewById(R.id.button);
        searchButton = findViewById(R.id.search);


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
        String urlString = "https://www.google.com/search?q=" + capital.getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
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
        int random = r.nextInt(3);




        String url ="https://restcountries.com/v3.1/name/" + COUNTRIES[random];

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       Log.d("Response", response.substring(0,500));

                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            for(int i=0;i<jsonarray.length();i++)
                            {
                                JSONObject object=jsonarray.getJSONObject(i);
                                Log.d("test",object.getString("capital"));
                                String capitalText = object.getString("capital").replace("[","");
                                capitalText = capitalText.replace("]","");
                                capitalText = capitalText.replace("\"","");

                                capital.setText(capitalText);
                                randomCountry.setText(COUNTRIES[random]);


                            }
                        }catch (JSONException err){
                            Log.d("Error", err.toString());
                        }


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
        Toast.makeText(this, "Don't shake me, bro!", Toast.LENGTH_SHORT).show();
    }
}