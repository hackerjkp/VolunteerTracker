package com.example.volunteertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Variables for check in and check out messages
        Button checkIn = (Button)findViewById(R.id.checkIn);
        TextView checkInMessage = (TextView)findViewById(R.id.checkInMessage);
        Button checkOut = (Button)findViewById(R.id.checkOut);
        TextView checkOutMessage = (TextView)findViewById(R.id.checkOutMessage);

        //display check in message
        checkIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                checkInMessage.setVisibility(View.VISIBLE);
            }
        });

        //display check out message
        checkOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                checkOutMessage.setVisibility(View.VISIBLE);
            }
        });

    }
}
