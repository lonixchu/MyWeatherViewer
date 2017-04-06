// MainActivity.java
// Displays a 16-dayOfWeek weather forecast for the specified city
package com.example.lonixchu.myweatherview;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
   // List of Weather objects representing the forecast
   private List<Weather> weatherList = new ArrayList<>();

   // ArrayAdapter for binding Weather objects to a ListView
   private WeatherArrayAdapter weatherArrayAdapter;
   private ListView weatherListView; // displays weather info

   // configure Toolbar, ListView and FAB
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // autogenerated code to inflate layout and configure Toolbar
      setContentView(R.layout.activity_main);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      // create ArrayAdapter to bind weatherList to the weatherListView
      weatherListView = (ListView) findViewById(R.id.weatherListView);
      weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
      weatherListView.setAdapter(weatherArrayAdapter);

      // configure FAB to hide keyboard and initiate web service request
      FloatingActionButton fab =
         (FloatingActionButton) findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            // get text from locationEditText and create web service URL
            EditText locationEditText =
               (EditText) findViewById(R.id.locationEditText);
            URL url = createURL(locationEditText.getText().toString());

            // hide keyboard and initiate a GetWeatherTask to download
            // weather data from OpenWeatherMap.org in a separate thread
            if (url != null) {
               dismissKeyboard(locationEditText);
               GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
               getLocalWeatherTask.execute(url);
            }
            else {
               Snackbar.make(findViewById(R.id.coordinatorLayout),
                  R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            }
         }
      });
   }

   // programmatically dismiss keyboard when user touches FAB
   private void dismissKeyboard(View view) {
      InputMethodManager imm = (InputMethodManager) getSystemService(
         Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
   }

   // create openweathermap.org web service URL using city
   private URL createURL(String city) {
      String apiKey = getString(R.string.api_key);
      String baseUrl = getString(R.string.web_service_url);

      try {
         // create URL for specified city and imperial units (Fahrenheit)
         String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
            "&units=metric&cnt=16&APPID=" + apiKey;
         return new URL(urlString);
      }
      catch (Exception e) {
         e.printStackTrace();
      }

      return null; // URL was malformed
   }

   // makes the REST web service call to get weather data and
   // saves the data to a local HTML file
   private class GetWeatherTask
      extends AsyncTask<URL, Void, JSONObject> {

      @Override
      protected JSONObject doInBackground(URL... params) {
         HttpURLConnection connection = null;

         try {
            connection = (HttpURLConnection) params[0].openConnection();
            int response = connection.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
               StringBuilder builder = new StringBuilder();

               try (BufferedReader reader = new BufferedReader(
                  new InputStreamReader(connection.getInputStream()))) {

                  String line;

                  while ((line = reader.readLine()) != null) {
                     builder.append(line);
                  }
               }
               catch (IOException e) {
                  Snackbar.make(findViewById(R.id.coordinatorLayout),
                     R.string.read_error, Snackbar.LENGTH_LONG).show();
                  e.printStackTrace();
               }

               return new JSONObject(builder.toString());
            }
            else {
               Snackbar.make(findViewById(R.id.coordinatorLayout),
                  R.string.connect_error, Snackbar.LENGTH_LONG).show();
            }
         }
         catch (Exception e) {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
               R.string.connect_error, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
         }
         finally {
            connection.disconnect(); // close the HttpURLConnection
         }

         return null;
      }

      // process JSON response and update ListView
      @Override
      protected void onPostExecute(JSONObject weather) {
         convertJSONtoArrayList(weather); // repopulate weatherList
         weatherArrayAdapter.notifyDataSetChanged(); // rebind to ListView
         weatherListView.smoothScrollToPosition(0); // scroll to top
      }
   }

   // create Weather objects from JSONObject containing the forecast
   private void convertJSONtoArrayList(JSONObject forecast) {
      weatherList.clear(); // clear old weather data

      try {
         // get forecast's "list" JSONArray
         JSONArray list = forecast.getJSONArray("list");

         // convert each element of list to a Weather object
         for (int i = 0; i < list.length(); ++i) {
            JSONObject day = list.getJSONObject(i); // get one day's data

            // get the day's temperatures ("temp") JSONObject
            JSONObject temperatures = day.getJSONObject("temp");

            // get day's "weather" JSONObject for the description and icon
            JSONObject weather =
               day.getJSONArray("weather").getJSONObject(0);

            // add new Weather object to weatherList
            weatherList.add(new Weather(
               day.getLong("dt"), // date/time timestamp
               temperatures.getDouble("min"), // minimum temperature
               temperatures.getDouble("max"), // maximum temperature
               day.getDouble("humidity"), // percent humidity
               weather.getString("description"), // weather conditions
               weather.getString("icon"))); // icon name
         }
      }
      catch (JSONException e) {
         e.printStackTrace();
      }
   }
}

/**************************************************************************
 * (C) Copyright 1992-2016 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/
