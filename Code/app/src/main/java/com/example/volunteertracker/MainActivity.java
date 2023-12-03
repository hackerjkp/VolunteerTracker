package com.example.volunteertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // creating variables for tracking hours
    // each volunteer has their own unique name
    HashMap<String, Long> volunteerHours;
    long loginTime;

    // adding shared preferences to save & retrieve volunteer hours
    private static final String preferencesName = "VolunteerTrackerPrefs";
    private static final String hoursKey = "volunteerHours";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Variables for check in and check out messages
        Button checkIn = (Button)findViewById(R.id.checkIn);
        TextView checkInMessage = (TextView)findViewById(R.id.checkInMessage);
        Button checkOut = (Button)findViewById(R.id.checkOut);
        TextView checkOutMessage = (TextView)findViewById(R.id.checkOutMessage);

        // creating hashmap variable to track hours
        volunteerHours = new HashMap<>();
        // initializing shared preferences
        preferences = getSharedPreferences(preferencesName, MODE_PRIVATE);
        retrievingHoursFromPrefs();

        //display check in message
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // calling method to check in
                checkIn();
                checkInMessage.setVisibility(View.VISIBLE);
            }
        });

        //display check out message
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // calling method to check out
                checkOut();
                checkOutMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    // method to check in
    private void checkIn() {
        String volunteerName = getVolunteerName();
        // checking if
        if (!volunteerHours.containsKey(volunteerName)) {
            volunteerHours.put(volunteerName, 0L);
            start_timer_count();
        }
        else {
            start_timer_count();;
        }
        // saving data to shared preferences
        savingHoursToPrefs();
    }

    // method to check out
    private void checkOut() {
        String volunteerName = getVolunteerName();
        stop_count_timer();
        long logoutTime = System.currentTimeMillis();

        if (volunteerHours.containsKey(volunteerName)) {
            volunteerHours.put(volunteerName, volunteerHours.get(volunteerName) + calculate_hours(logoutTime - loginTime));
        }
        // saving data to shared preferences
        savingHoursToPrefs();
    }


    // method to start the timer
    private void start_timer_count() {
        loginTime = System.currentTimeMillis();
    }
    // method to stop the timer
    private void stop_count_timer() {
    }

    // method to calculate difference in time
    private long calculate_hours(long timeDifference) {
        return timeDifference / (60 * 60 * 1000);
    }

    // method to get names of volunteers
    private  String getVolunteerName() {
        EditText volName = findViewById(R.id.VolName);
        return volName.getText().toString();
    }

    // method to save hours into shared preferences
    private void savingHoursToPrefs() {
        preferences.edit().putString(hoursKey, HashMap2String(volunteerHours)).apply();
    }

    // method to get hours into shared preferences
    private void retrievingHoursFromPrefs() {
        String savedData = preferences.getString(hoursKey, "");
        volunteerHours = String2HashMap(savedData);
    }


    // helper method to convert hash maps to string
    private String HashMap2String(HashMap<String, Long> hm) {
        // using delimiter to separate string
        StringBuilder sb = new StringBuilder();
        for (String key : hm.keySet()) {
            sb.append(key).append(":").append(hm.get(key)).append(",");
        }
        return sb.toString();
    }

    // helper method to convert string to hash map
    private HashMap<String, Long> String2HashMap(String s) {
        // using delimiter here as well to separate string
        HashMap<String, Long> hm = new HashMap<>();
        String[] array = s.split(",");
        for (String pair : array) {
            String[] key = pair.split(":");
            if (key.length == 2) {
                hm.put(key[0], Long.parseLong(key[1]));
            }
        }
        return hm;
    }
}
