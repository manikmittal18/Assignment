package com.manik.quiz.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    String url = "https://api.openweathermap.org/";
    TextView temp, feels, mi, max, rise,set,description;
    String latitude, longitude;
    double lat, longi;
    ImageView imageView;
    FusedLocationProviderClient mFusedLocationClient;
    List<Address> addresses = null;
    Geocoder geocoder;
    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        getLastLocation();
        geocoder = new Geocoder(this, Locale.getDefault());

        temp = (TextView) findViewById(R.id.temperature);
        feels = (TextView) findViewById(R.id.feels_like);
        mi = (TextView) findViewById(R.id.min_temp);
        max = (TextView) findViewById(R.id.max_temp);
        rise = (TextView) findViewById(R.id.rise);
        set = (TextView) findViewById(R.id.set);
        description = (TextView) findViewById(R.id.desc);
        imageView = findViewById(R.id.image);



    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {

                                    lat = location.getLatitude();
                                    longi = location.getLongitude();
                                    latitude = String.valueOf(lat);
                                    longitude = String.valueOf(longi);

                                    List<Address> addresses = null;
                                    try {
                                        addresses = geocoder.getFromLocation(lat, longi, 1);
                                        String cityName = addresses.get(0).getLocality();
                                        String stateName = addresses.get(0).getAdminArea();

                                        getSupportActionBar().setTitle(cityName + ", "+ stateName);


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(url)
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build();

                                    RetrofitObjectAPI service = retrofit.create(RetrofitObjectAPI.class);

                                    Call<Weather> call = service.getJson(lat,longi,"e75953e6b4c13ef04386a4e50904abaa");



                                    call.enqueue(new Callback<Weather>() {
                                        @Override
                                        public void onResponse(Call<Weather> call, Response<Weather> response) {
                                            try {

                                                long sunrise= response.body().getSys().getSunrise();
                                                long sunset= response.body().getSys().getSunset();

                                                float tempp = response.body().getMain().getTemp();
                                                float feels_like = response.body().getMain().getFeels_like();
                                                float temp_min = response.body().getMain().getTemp_min();
                                                float temp_max = response.body().getMain().getTemp_max();

                                                long s = sunrise % 60;
                                                long m = (sunrise / 60) % 60;
                                                long h = (sunrise / (60 * 60)) % 24;

                                                String sunrise_c = String.format("%d:%02d:%02d", h,m,s);

                                                long ss = sunset % 60;
                                                long mm = (sunset / 60) % 60;
                                                long hh = (sunset / (60 * 60)) % 24;

                                                String sunset_c = String.format("%d:%02d:%02d", hh,mm,ss);

                                                int temppp = (int) (tempp-273.15);
                                                int feels_likee = (int) (feels_like-273.15);
                                                int temp_minn = (int) (temp_min-273.15);
                                                int temp_maxx = (int) (temp_max-273.15);

                                                temp.setText("Current Temperature : "+temppp);
                                                feels.setText("Feels Like : "+feels_likee);
                                                mi.setText("Minimum Temperature : "+temp_minn);
                                                max.setText("Maximum Temperature : "+temp_maxx);
                                                rise.setText("Sunrise : "+sunrise_c);
                                                set.setText("Sunset : "+sunset_c);

                                                List<weatherr> arrayList =  response.body().getWeather();
                                                String descr= arrayList.get(0).getDescription();
                                                String ico= "https://openweathermap.org/img/w/" +arrayList.get(0).getIcon() + ".png";

                                                description.setText("Weather Description "+descr);
                                                Glide.with(getApplicationContext()).load(ico).into(imageView);



                                            } catch (Exception e) {
                                                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Weather> call, Throwable t) {

                                            Toast.makeText(MainActivity.this, ""+t.toString(), Toast.LENGTH_LONG).show();

                                        }


                                    });

                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            lat = mLastLocation.getLatitude();
            longi = mLastLocation.getLongitude();


            try {
                addresses = geocoder.getFromLocation(lat, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
            else {
                finish();
                Toast.makeText(this, "We need your location to show weather", Toast.LENGTH_SHORT).show();
            }
        }
    }
}