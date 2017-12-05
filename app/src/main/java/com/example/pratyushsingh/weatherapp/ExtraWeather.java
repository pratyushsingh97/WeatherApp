package com.example.pratyushsingh.weatherapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtraWeather extends AppCompatActivity {
    private TextView firstDay, secondDay, thirdDay, fourthDay, fifthDay, sixthDay, seventhDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_weather);

        firstDay = findViewById(R.id.firstDayLabel);
        secondDay = findViewById(R.id.secondDayLabel);
        thirdDay = findViewById(R.id.thirdDayLabel);
        fourthDay = findViewById(R.id.fourthDayLabel);
        fifthDay = findViewById(R.id.fifthDayLabel);
        sixthDay = findViewById(R.id.sixthDayLabel);
        seventhDay = findViewById(R.id.seventhDayLabel);

        Intent intent = getIntent();
        String dailyJson = intent.getStringExtra("DAILY_DATA");

        long timestamp;
        String time;
        try {
            JSONObject dailyObject = new JSONObject(dailyJson);
            JSONArray dailyArray = dailyObject.getJSONArray("data");

            for(int i = 1; i < 8; i++) {
                String low_temperature = dailyArray.getJSONObject(i)
                        .get("temperatureHigh").toString();
                String high_temperature = dailyArray.getJSONObject(i).get("temperatureLow").toString();
                time = dailyArray.getJSONObject(i).get("time").toString();
                timestamp = Long.parseLong(time);
                String day = formattedDate(timestamp * 1000);

                switch (i) {
                    case 1:
                        firstDay.setText(setLabel(low_temperature, high_temperature, day));
                        firstDay.setTextSize(25);
                        break;
                    case 2:
                        secondDay.setText(setLabel(low_temperature, high_temperature, day));
                        secondDay.setTextSize(25);
                        break;
                    case 3:
                        thirdDay.setText(setLabel(low_temperature, high_temperature, day));
                        thirdDay.setTextSize(25);
                        break;
                    case 4:
                        fourthDay.setText(setLabel(low_temperature, high_temperature, day));
                        fourthDay.setTextSize(25);
                        break;
                    case 5:
                        fifthDay.setText(setLabel(low_temperature, high_temperature, day));
                        fifthDay.setTextSize(25);
                        break;
                    case 6:
                        sixthDay.setText(setLabel(low_temperature, high_temperature, day));
                        sixthDay.setTextSize(25);
                        break;
                    case 7:
                        seventhDay.setText(setLabel(low_temperature, high_temperature, day));
                        seventhDay.setTextSize(25);
                        break;
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String formattedDate(Long d) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String date = sdf.format(new Date(d));
        return date;
    }

    private String setLabel(String lowTemp, String highTemp, String day) {
        return day + ": Low: " + lowTemp + " High: " + highTemp;
    }
}
