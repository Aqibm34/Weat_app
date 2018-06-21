package com.example.aquib.weath_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;



public class WeatherController extends AppCompatActivity {

    //TODO: Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    //TODO: App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";
    //TODO: Time between location updates (in milliseconds)
    final long MIN_TIME = 5000;
    // TODO: Distance between location updates(in meters )
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION PROVIDER
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    //TODO: Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // LocationManager and a LocationListener here:
    LocationManager mLocationManager;               //starts or stops location updates
    LocationListener mLocationListener;             //notifies that location changed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO:OnClickListener to the changeCityButton :
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent my = new Intent(WeatherController.this,ChangeCity.class);
                startActivity(my);
            }
        });
    }


    // TODO: onResume() method:
    @Override
    public void onResume() {
        super.onResume();
        Log.d("Clima", "OnResume called");

        Intent Intent = getIntent();
        String city = Intent.getStringExtra("city");
        if (city != null){
            getWeatherForNewCity(city);
        }
        else {
            Log.d("Clima", "Getting Weather for current location");
            getWeatherForCurrentLocation();
        }
    }


    // TODO: getWeatherForNewCity(String city) method:
    private void getWeatherForNewCity(String location){
        RequestParams params = new RequestParams();
        params.put("q",location);
        params.put("AppId",APP_ID);
        DoSomeNetworking(params);
    }

    // TODO: getWeatherForCurrentLocation() method:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {                    // It has 4 methods below
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "OnLocationChanged() callback recieved");

                String longitude = String.valueOf(location.getLongitude());
                String latitide = String.valueOf(location.getLatitude());
                Log.d("Clima","Longitude is " +longitude);
                Log.d("Clima","Latitude is"+latitide);

                RequestParams params = new RequestParams();
                params.put("lat",latitide);
                params.put("lon",longitude);
                params.put("appid",APP_ID);
                DoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Clima", "OnProviderDisabled() callback recieved");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.tr

            //Requesting Permissions from the User:
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    //Act on Users Permissions:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Clima","OnRequestPermission() Called");
            }
        }
    }
    // TODO: DoSomeNetworking(RequestParams params) method:
    public void DoSomeNetworking(RequestParams params){
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL,params,new JsonHttpResponseHandler(){    // It has two methods onSucess and on
            @Override
            public void onSuccess(int StatusCode, Header[] headers, JSONObject response){
                Log.d("Clima","Jason Success!!"+response.toString());
                WeatherDataModel weatherdata = WeatherDataModel.fromJSon(response);
                UpdateUI(weatherdata);
            }
            @Override
            public void onFailure(int StatusCode, Header[] headers, Throwable e,JSONObject response){
                Log.e("Clima","fail"+e.toString());
                Log.d("Clima","Statuscode" + StatusCode);
                Toast.makeText(WeatherController.this,"Request Fail",Toast.LENGTH_SHORT).show();
            }

        });
    }


    // TODO: updateUI() method :
    public void UpdateUI(WeatherDataModel weather){
        mCityLabel.setText(weather.getCity());
        mTemperatureLabel.setText(weather.getTemperature());

        int ResId = getResources().getIdentifier(weather.getIconName(),"drawable", getPackageName());

        mWeatherImage.setImageResource(ResId);
    }

    // TODO: onPause() method for stop the location services when not used by the app :
    @Override
    protected void onPause() {
        super.onPause();

        if(mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
