package com.example.pratyushsingh.weatherapp;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.json.*;




public class MainActivity extends AppCompatActivity {
    private static final int TWO_DAYS  = 48;
    double [] fiveHourTemp = new double[5];
    double tempAverage = 0;
    boolean isSet = false;
    boolean isPast = false;
    JSONObject dailyData;

    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefresh;
    TextView city, lowerTempLabel, currentTempLabel, upperTempLabel, nextHourLabel, firstHourLabel,
            secondHourLabel, thirdHourLabel, fourthHourLabel, fifthHourLabel, avgTemp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize labels
        progressBar = findViewById(R.id.progressBar);
        city = findViewById(R.id.locationAddress);
        lowerTempLabel = findViewById(R.id.lowerTemperature);
        currentTempLabel = findViewById(R.id.currentTemperature);
        upperTempLabel = findViewById(R.id.higherTemperature);
        nextHourLabel = findViewById(R.id.nextHourLabel);
        firstHourLabel = findViewById(R.id.firstHour);
        secondHourLabel = findViewById(R.id.secondHour);
        thirdHourLabel = findViewById(R.id.thirdHour);
        fourthHourLabel = findViewById(R.id.fourthHour);
        fifthHourLabel = findViewById(R.id.fifthHour);
        avgTemp = findViewById(R.id.twoDaysLabel);

        //make the labels invisible
        city.setVisibility(View.INVISIBLE);
        lowerTempLabel.setVisibility(View.INVISIBLE);
        currentTempLabel.setVisibility(View.INVISIBLE);
        upperTempLabel.setVisibility(View.INVISIBLE);
        nextHourLabel.setVisibility(View.INVISIBLE);
        firstHourLabel.setVisibility(View.INVISIBLE);
        secondHourLabel.setVisibility(View.INVISIBLE);
        thirdHourLabel.setVisibility(View.INVISIBLE);
        fourthHourLabel.setVisibility(View.INVISIBLE);
        fifthHourLabel.setVisibility(View.INVISIBLE);
        avgTemp.setVisibility(View.INVISIBLE);

        //click on current temp to get more info
        currentTempLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExtraWeather.class);
                intent.putExtra("DAILY_DATA", dailyData.toString());
                intent.putExtra("AVG_DATA", tempAverage);
                startActivity(intent);
                //Log.d("click", "Click worked");
            }
        });

        //refresh
        swipeRefresh = findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                new RetrieveWeather().execute();

            }
        });


        //click on nextHourLabel to get the
        nextHourLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("H a");

                Double firstHour = fiveHourTemp[0];
                firstHourLabel.setText(sdf.format(cal.getTime()) + ": " + firstHour.toString());

                cal.add(Calendar.HOUR_OF_DAY, 1);
                Double second = fiveHourTemp[1];
                secondHourLabel.setText(sdf.format(cal.getTime()) + ": " + second.toString());

                cal.add(Calendar.HOUR_OF_DAY, 1);
                Double third = fiveHourTemp[2];
                thirdHourLabel.setText(sdf.format(cal.getTime()) + ": " + third.toString());

                cal.add(Calendar.HOUR_OF_DAY, 1);
                Double fourth = fiveHourTemp[3];
                fourthHourLabel.setText(sdf.format(cal.getTime()) + ": " + fourth.toString());

                cal.add(Calendar.HOUR_OF_DAY, 1);
                Double fifth = fiveHourTemp[4];
                fifthHourLabel.setText(sdf.format(cal.getTime()) + ": " + fifth.toString());

                isSet = !isSet;

                if(isSet) {
                    firstHourLabel.setVisibility(View.VISIBLE);
                    secondHourLabel.setVisibility(View.VISIBLE);
                    thirdHourLabel.setVisibility(View.VISIBLE);
                    fourthHourLabel.setVisibility(View.VISIBLE);
                    fifthHourLabel.setVisibility(View.VISIBLE);

                    firstHourLabel.setTextSize(20);
                    secondHourLabel.setTextSize(20);
                    thirdHourLabel.setTextSize(20);
                    fourthHourLabel.setTextSize(20);
                    fifthHourLabel.setTextSize(20);
                }
                else {
                    firstHourLabel.setVisibility(View.INVISIBLE);
                    secondHourLabel.setVisibility(View.INVISIBLE);
                    thirdHourLabel.setVisibility(View.INVISIBLE);
                    fourthHourLabel.setVisibility(View.INVISIBLE);
                    fifthHourLabel.setVisibility(View.INVISIBLE);
                }
            }
        });

      if(!isPast) {
          new RetrieveWeather().execute();
          currentTempLabel.setEnabled(true); // re-enable button
      }

        handleIntent(getIntent());

    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new RetrieveWeather(query, true).execute();

        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        return true;
    }




    class RetrieveWeather extends AsyncTask<Void, String, String> {

        private final String getURL = "https://api.darksky.net/forecast/";
        private final double LATITUDE = 30.2672;
        private final double LONGITUDE = -97.7431;
        private final String API_KEY = "5e8a9c173af6d9383467a83360b28eb4";
        String currentTemperature = "";
        String lowerTemperature = "";
        String upperTemperature = "";
        String url = getURL + API_KEY + '/' + LATITUDE + ',' + LONGITUDE;
        boolean disableFunctions = false;
        //isPast = false;

        String [] almostMessages = {"Getting weather related updates", "Just a couple more seconds",
        "Extracting weather information from the cloud (what is the cloud?)", "Getting info about clouds" +
                " from the cloud", "The weather can be good, the weather can be bad, but atleast it doesn't tweet :)"
        };

        String [] doneMessages = {"The weather is served!", "Weather, how abou' dah?", "Just about done!"};

        public RetrieveWeather() {
            //regular execution

        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        public RetrieveWeather(String query, boolean disableFx) {
            isPast = true;
            try {

                SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy");
                Date date = df.parse(query);
                long epoch = date.getTime()/1000;
                Log.e("EPOCH", "" + epoch);
                url = getURL + API_KEY + '/' + LATITUDE + ',' + LONGITUDE + ',' + epoch;
                disableFunctions = disableFx;


            }
            catch(Exception e) {
                Toast.makeText(MainActivity.this, "Please enter in the following format: MMM dd yyyy",
                        Toast.LENGTH_LONG).show();
            }


        }

        protected String doInBackground(Void... urls) {
            try {

                URL callURL = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) callURL.openConnection();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new
                            InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder jsonResponse = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {

                        int range = almostMessages.length;
                        int rIndex = (int) Math.random() * range;
                        publishProgress(almostMessages[rIndex]);

                        jsonResponse.append(line);
                    }

                    bufferedReader.close();

                    //let's get the data from the JSON
                    JSONObject jsonObject = new JSONObject(jsonResponse.toString());
                    currentTemperature = jsonObject.getJSONObject("currently").getString("temperature");

                    dailyData = jsonObject.getJSONObject("daily");//.getJSONArray("data");

                    lowerTemperature = dailyData.getJSONArray("data").getJSONObject(0).get("temperatureLow").toString();
                    upperTemperature = dailyData.getJSONArray("data").getJSONObject(0).get("temperatureHigh").toString();

                    JSONArray hourlyData = jsonObject.getJSONObject("hourly").getJSONArray("data");

                    //get the hourly temperature
                    for(int i = 0; i < hourlyData.length(); i++) {
                        if(hourlyData.getJSONObject(i).get("temperature") != null) {
                            tempAverage += Double.parseDouble(hourlyData.getJSONObject(i).
                                    get("temperature").toString());
                        }

                        if(i < 5) {
                            fiveHourTemp[i] = Double.parseDouble(hourlyData.getJSONObject(i).
                                    get("temperature").toString());
                        }
                    }

                    tempAverage = tempAverage/TWO_DAYS;


                    return jsonResponse.toString();

                }
                finally {
                    urlConnection.disconnect();
                }
            }
            catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                progressBar.setVisibility(View.INVISIBLE);
                response = "There was an error in retrieving weather information";
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
            }
            else {
                //show "done messages"
                int range = doneMessages.length;
                int rIndex = (int) (Math.random() * range);
                Toast.makeText(MainActivity.this, doneMessages[rIndex], Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);


                //update labels

                lowerTempLabel.setText(lowerTemperature);
                lowerTempLabel.setVisibility(View.VISIBLE);
                lowerTempLabel.setTextSize(30);

                upperTempLabel.setText(upperTemperature);
                upperTempLabel.setVisibility(View.VISIBLE);
                upperTempLabel.setTextSize(30);

                city.setVisibility(View.VISIBLE);
                city.setTextSize(60);

                //if(!disableFunctions) {
                    currentTempLabel.setText(currentTemperature);
                    currentTempLabel.setVisibility(View.VISIBLE);
                    currentTempLabel.setTextSize(50);
                    currentTempLabel.setEnabled(false);

                    nextHourLabel.setVisibility(View.VISIBLE);
                    nextHourLabel.setTextSize(22);

                    avgTemp.setVisibility(View.VISIBLE);
                    avgTemp.setText("The average temp for the next 48 hours: " + tempAverage);
               // }

                swipeRefresh.setRefreshing(false);


             }
        }

        protected void onProgressUpdate(String updateMessage) {
            Toast.makeText(MainActivity.this, updateMessage, Toast.LENGTH_LONG).setText(updateMessage);
        }

    }
}



