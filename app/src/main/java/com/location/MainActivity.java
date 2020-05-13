package com.location;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.location.location_manager.LocationCallBack;
import com.location.location_manager.LocationProvider;
import com.location.permission_manager.PermissionUtils;

public class MainActivity extends AppCompatActivity implements LocationCallBack {
    LocationProvider locationProvider;
    TextView text;

    /*
     * TODO
     *  1.GeoCoder to get address
     *  2.Distance between location in Km.
     *  3.Stop continuous location on current thread
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Entry point for location
        locationProvider = new LocationProvider(this);
        locationProvider.setLocationCallBack(this);
        locationProvider.getLocationContinues();
        // End point for location

        text = findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationProvider.stopLocationFromBackground();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (locationProvider != null)
            locationProvider.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(int reqType, Location location) {
        text.setText(""+location.getLatitude());
    }

    @Override
    public void onFailure(int reqType, String errorMessage) {
        text.setText(errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Mandatory to avoid memory leak
        if (locationProvider != null)
            locationProvider.removeCallback();
    }
}
