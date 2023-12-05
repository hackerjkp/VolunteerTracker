package com.example.volunteertracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.content.DialogInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    // creating variables for tracking hours
    // each volunteer has their own unique name
    HashMap<String, Long> volunteerHours;
    long loginTime;
    int checkoutLimit = 18000000; //5 hours
    StringBuilder savedDataSB = new StringBuilder();

    //creating a notification group
    private static final String CHANNEL_ID = "VolunteerApp";

    //creating location permissions
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView locationText;

    // adding shared preferences to save & retrieve volunteer hours
    private static final String preferencesName = "VolunteerTrackerPrefs";
    private static final String hoursKey = "volunteerHours";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationText = findViewById(R.id.locationText);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    // Logging the location data
                    String coordinatesText = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
                    locationText.setText(coordinatesText);
                }
            }
        };

        // check if the user has on location permissions
        locationPermissions();


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
        // button for showing data
        Button showHours = findViewById(R.id.showHours);

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

        // showing saved data
        showHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSavedData();
            }
        });
    }

    // method to check in
    private void checkIn() {
        String volunteerName = getVolunteerName();
        // checking if user already has hours
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
    // adding vest notification here for every time user checks out
    private void checkOut() {
        String volunteerName = getVolunteerName();
        // creating variable for user having vest
        boolean hasVest = true;

        stop_count_timer();
        long logoutTime = System.currentTimeMillis();

        // checking if user already has hours
        if (volunteerHours.containsKey(volunteerName)) {
            volunteerHours.put(volunteerName, volunteerHours.get(volunteerName) + calculate_hours(logoutTime - loginTime));
        }
        // saving data to shared preferences
        savingHoursToPrefs();

        // checking if user still has vest
        if (hasVest) {
            showVestNotification();
        }
        else {
            stop_count_timer();
        }
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
        if(timeDifference >= checkoutLimit){
            checkout_missed();
            return 0;
        }
        return timeDifference / (60 * 60 * 1000);
    }

    //notify user they missed the time limit while clocking in hours.
    private void checkout_missed() {
        AlertDialog.Builder checkoutNotification = new AlertDialog.Builder(this);
        checkoutNotification.setTitle("Checkout Missed Notification");
        checkoutNotification.setMessage("You did not submit your hours on time. Please contact your admin.");
        checkoutNotification.setPositiveButton("Dismiss", null);
        checkoutNotification.show();
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

    // method to show saved hours
    private void showSavedData() {
        // getting data from previous method
        retrievingHoursFromPrefs();
        // using similar string builder as HashMap2String's
        for (String key : volunteerHours.keySet()) {
            savedDataSB.append(key).append(" : \t").append(volunteerHours.get(key)).append((" Hours\n"));
        }
        // displaying saved hours as an alert
        AlertDialog.Builder savedHours = new AlertDialog.Builder(this);
        savedHours.setTitle("Saved Volunteer Hours");
        savedHours.setMessage(savedDataSB.toString());
        savedHours.setPositiveButton("Convert to PDF", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pdfPermission();
            }
        });

        savedHours.setNegativeButton("Confirm", null);
        savedHours.show();
    }

    // method to show vest notification
    private void showVestNotification() {
        AlertDialog.Builder vestNotification = new AlertDialog.Builder(this);
        vestNotification.setTitle("Vest Notification");
        vestNotification.setMessage("You have not returned your vest. Please return it when you finished.");
        vestNotification.setPositiveButton("Dismiss", null);
        vestNotification.show();
    }

    private void convertDataToPDF() {
        // Generate a PDF file with the saved data
        String pdfFileName = "VolunteerHours.pdf";
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), pdfFileName);

        try {
            // Create a FileOutputStream for the PDF file
            FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
            fileOutputStream.write(savedDataSB.toString().getBytes());
            fileOutputStream.close();

            // Display a success message or open the PDF file using an Intent
            Log.d("PDF", "PDF file created: " + pdfFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., display an error message)
            Log.e("PDF", "Error creating PDF file: " + e.getMessage());
        }
    }
    //LOCATION PERMISSION SECTION
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000); //this means 5 seconds
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop location
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    private void locationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            startLocationUpdates();
        }
        else{
            startLocationUpdates();
        }
    }
    //ask phone for permission to generate a pdf
    private void pdfPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // if access hasn't been granted yet, ask for it.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            );
        } else {
            convertDataToPDF();
        }
    }

}

