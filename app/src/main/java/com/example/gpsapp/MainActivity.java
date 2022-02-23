package com.example.gpsapp;

import static java.util.Locale.US;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView Longitude,Latitude,Distance,Addy, time;
    //displays above
    Button Perm, extra;
    Switch power, darklight;
    //special additions for the Bottom
    String address = null;

    double distmoved = 0;//used for distance
    int speedofsec = 2000;//default to 1 sec intervals used for power save
    Location pastloco;//used for checking past locations for distance

    int count;
    String lasttime = "Time Spent @ Last Locations:";
    long time1;
    long sec;
    long min;
    long hour;
    ArrayList<String> times = new ArrayList();
    ArrayList<String> pastlocations = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = findViewById(R.id.Time);
        Longitude = findViewById(R.id.Longitude);
        Latitude = findViewById(R.id.Latitude);
        Distance = findViewById(R.id.DistWalked);
        Addy = findViewById(R.id.Address);
        //above are teh text display for data
        Perm = findViewById(R.id.Permission);
        power = findViewById(R.id.power);
        darklight = findViewById(R.id.daynight);
        extra = findViewById(R.id.play);
        //above are the special additions placed on the bottom
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //introduces locationmanager for finding location
        Perm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        Toast.makeText(getApplicationContext(), "In order for the app to function please turn permission on!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Permission is Granted\nThank You!", Toast.LENGTH_LONG).show();
                    }
            }
        });
        darklight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(darklight.isChecked()){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (power.isChecked()) {
                    speedofsec = 8000;//8 sec intervals

                }else{
                    speedofsec = 2000;//2 sec intervals
                }
            }
        });
        extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //above are the special addition onclicks
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, speedofsec, 1, this);
        }
        //requests perms for teh first time

    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geo = new Geocoder (this, US);
        try {
            List<android.location.Address> list = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address addressrn = list.get(0);
            address = addressrn.getAddressLine(0);
            if(pastloco != null)
                distmoved+= location.distanceTo(pastloco);

            if(list.get(0).getLocality() ==  null){
                pastlocations.add("There is no Locality");// the rare case there is no name to the area
            }else{
                pastlocations.add(list.get(0).getLocality());//list for past times
            }
            long time2;
            if(count>0) {
                time2 = SystemClock.elapsedRealtime() - time1;
                time2 /= 1000;

                sec = time2 % 60;
                min = time2 / 60 % 60;
                hour = time2 / (60*60) % 24;
                lasttime = (hour+":"+min+":"+sec);
                times.add(lasttime);
                if(times.size()>3){//removes null items created by onrestoreinstances
                    if(times.get(2) == null){
                        times.remove(2);
                        pastlocations.remove(2);
                    }
                    if(times.get(1) == null){
                        times.remove(1);
                        pastlocations.remove(1);
                    }
                    if(times.get(0) == null){
                        times.remove(0);
                        pastlocations.remove(0);
                    }
                }
                if(times.size()==0)
                    time.setText("Time Spent @ Last Locations:");
                else if(times.size()==1)
                    time.setText("Time Spent @ Last Locations:\n"+times.get(times.size()-1)+" @"+pastlocations.get(pastlocations.size()-1)+"\n");
                else if(times.size()==2){
                    time.setText("Time Spent @ Last Locations:\n"+times.get(times.size()-1)+" @"+pastlocations.get(pastlocations.size()-1)+"\n"+times.get(times.size()-2)+" @"+pastlocations.get(pastlocations.size()-2));
                }else{
                    time.setText("Time Spent @ Last Locations:\n"+times.get(times.size()-1)+" @"+pastlocations.get(pastlocations.size()-1)+"\n"+times.get(times.size()-2)+" @"+pastlocations.get(pastlocations.size()-2)+"\n"+times.get(times.size()-3)+" @"+pastlocations.get(pastlocations.size()-3));
                }
            }
            else {
                time1 = SystemClock.elapsedRealtime();
                time.setText("Time Spent @ Last Locations:");
            }
            count++;
            time1 = SystemClock.elapsedRealtime();
            //resets time for current location

            Addy.setText(address);
            Longitude.setText(""+location.getLongitude());
            Latitude.setText(""+location.getLatitude());
            Distance.setText(""+((distmoved/100)*100)+" meters");

            pastloco = location;
        } catch (IOException e){ e.printStackTrace(); }
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(times.size()>2){
            outState.putString("savedtime0",times.get(0));
            outState.putString("savedtime1",times.get(1));
            outState.putString("savedtime2",times.get(2));
            outState.putString("pastloco0",pastlocations.get(0));
            outState.putString("pastloco1",pastlocations.get(1));
            outState.putString("pastloco2",pastlocations.get(2));
        }else if (times.size()>1) {
            outState.putString("savedtime0", times.get(0));
            outState.putString("savedtime1", times.get(1));
            outState.putString("pastloco0", pastlocations.get(0));
            outState.putString("pastloco1", pastlocations.get(1));
        }else if (times.size()>0){
            outState.putString("savedtime0", times.get(0));
            outState.putString("pastloco0", pastlocations.get(0));
        }

        outState.putInt("count",count);//saves count
        outState.putDouble("distmoved",distmoved);//saves distmoved
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pastlocations.add(savedInstanceState.getString("pastloco2"));
        pastlocations.add(savedInstanceState.getString("pastloco1"));
        pastlocations.add(savedInstanceState.getString("pastloco0"));
        times.add(savedInstanceState.getString("savedtime2"));
        times.add(savedInstanceState.getString("savedtime1"));
        times.add(savedInstanceState.getString("savedtime0"));

        count = savedInstanceState.getInt("count");
        distmoved = savedInstanceState.getDouble("distmoved");
    }
}