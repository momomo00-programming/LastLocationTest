package com.example.momomo00.user.lastlocationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private AddressResultReceiver   mResultReceiver = null;
    private String mAddressOutput;

    private Location    mLocation = null;

    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 34;

    private TextView mLocationLatitudeTextView;
    private TextView mLocationLongitudeTextView;
    private TextView mLocationAddressTextView;
    private Button mGetAddressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mResultReceiver = new AddressResultReceiver(new Handler());

        mLocationLatitudeTextView = findViewById(R.id.location_latitude_text_view);
        mLocationLongitudeTextView = findViewById(R.id.location_longitude_text_view);
        mLocationAddressTextView = findViewById(R.id.location_address_text_view);

        mGetAddressButton = findViewById(R.id.get_address_button);
        mGetAddressButton.setEnabled(false);

        mAddressOutput = "";

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(checkPermissions()) {
            mGetAddressButton.setEnabled(true);
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProviderRational =
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        if(shouldProviderRational) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
                checkRequestPermissionResult(permissions, grantResults);
                break;
            default:
                break;
        }
    }

    private void checkRequestPermissionResult(String[] permissions, int[] grantResults) {
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGetAddressButton.setEnabled(true);
    }

    public void onClickButton(View view) {
        switch(view.getId()) {
            case R.id.get_address_button:
                showAddress();
                break;
            default:
                break;
        }
    }

    private void showAddress() {
        mGetAddressButton.setEnabled(false);
        getAddress();
    }

    private void getAddress() {
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                this, mOnSuccessListener);
    }

    private OnSuccessListener<Location> mOnSuccessListener = new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
            if(location == null) {
                return;
            }

            mLocation = location;
            mLocationLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            mLocationLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));

            if(!Geocoder.isPresent()) {
                mGetAddressButton.setEnabled(true);
                return;
            }

            startIntentService();
        }
    };

    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLocation);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            mLocationAddressTextView.setText(mAddressOutput);

            mGetAddressButton.setEnabled(true);
        }
    }
}
