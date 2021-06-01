package com.example.nearbyplacesdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Initialize variable
    Spinner spType;
    Button btFind;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign variable
        spType = findViewById(R.id.sp_type);
        btFind = findViewById(R.id.bt_find);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        //Initaialize array of place type
        String[] placeTypeList = {"atm","hospital"};
        String[] placeNameList = {"ATM","Hospital"};

        //Set adapter as spinner
        spType.setAdapter(new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, placeNameList));

        //Intialize fused locator provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Check permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //call method
            getCurrentLocation();
        } else {
            //when permission denied
            //request permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get selected position of spinner
                int i = spType.getSelectedItemPosition();
                //Initialize url
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +//Url
                        "location=" + currentLat + "," + currentLong + //Location lat and long
                        "&radius=5000" +//Nearby radius
                        "&type=" + placeTypeList[i] + //Place type
                        "&sensor=true" + //Sensor
                        "&key=" + getResources().getString(R.string.google_map_key); // Google map Key

                //Execute place task method
                new PlaceTask().execute(url);
            }
        });

    }

    private void getCurrentLocation() {
        //initialize task location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //when success
                if(location!=null){
                    //when location ks not equal to null
                    //get current latitude
                    currentLat=location.getLatitude();
                    currentLong=location.getLongitude();

                    //Sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //when map is ready
                            map=googleMap;
                            //zoom current location on map
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLat,currentLong),10
                            ));
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       //Extra line
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);   //Extra line
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted
                //call method
                getCurrentLocation();

            }
        }
    }

    private class PlaceTask extends AsyncTask<String,Integer,String> {
        private String s;

        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
                //Initialize Data
                data=downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            this.s = s;
            //Execute parser task
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        //Initialize url
        URL url=new URL(string);
        //Initialize connection
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        //Connect connection
        connection.connect();
        //Initailze input stream
        InputStream stream=connection.getInputStream();
        //Initial Buffer reader
        BufferedReader reader=new BufferedReader(new InputStreamReader(stream));
        //Initailize string builder
        StringBuilder builder=new StringBuilder();
        //Init string var
        String line="";
        //Use while looop
        while ((line= reader.readLine())!=null){
            //Append Line
            builder.append(line);
        }
        //Get append data
        String data=builder.toString();
        reader.close();
        return data;

    }

    private class ParserTask extends  AsyncTask<String,Integer, List<HashMap<String,String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //create json parser class
            JsonParser jsonParser=new JsonParser();
            //Initailize hash map list
            List<HashMap<String,String>> mapList=null;
            JSONObject object=null;
            try {
                //Initailize Json Object
                object=new JSONObject(strings[0]);
                //Parse json object
                mapList=jsonParser.parseResult(object);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Return Maplist
            return mapList;

        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //Clear Map
            map.clear();
            //use for loop
            for(int i=0;i<hashMaps.size();i++){
                //initalize hash map
                HashMap<String,String> hashMapList=hashMaps.get(i);
                //Get latitude
                double lat=Double.parseDouble(hashMapList.get("lat"));
                //Get longitude
                double lng=Double.parseDouble(hashMapList.get("lng"));
                //Get name
                String name=hashMapList.get("name");
                //Concat latitude and longitutde
                LatLng latLng=new LatLng(lat,lng);
                //Initialize marker options
                MarkerOptions options=new MarkerOptions();
                //Set position
                options.position(latLng);
                //Set title
                options.title(name);
                //Add marker on map
                map.addMarker(options);

            }
        }
    }
}