package de.phk.telescopenightchecker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadWeather()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun downloadWeather(){

        //TODO: Make this dynamic
        val lat = 49.853691
        val lon = 12.004130

        val key = BuildConfig.OWM_KEY
        var responseJSONObject: JSONObject? = null
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=current,minutely,daily&appid=$key"
        //TODO: Remove before release
        Log.d("URL", url)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                analyzeWeatherData(response)
                Log.d("Result", response.toString())
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
                Log.d("Result", error.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun analyzeWeatherData(weatherData : JSONObject){
        val weatherArray : JSONArray = weatherData.get("hourly") as JSONArray
        for (i in 0 until weatherArray.length()){
            Log.d("array $i", minimalCloudsForHour(weatherArray.getJSONObject(i)).toString())
        }
    }

    fun minimalCloudsForHour(hourData : JSONObject) : Boolean{
        val maxCloudValue = 10
        if (hourData.getInt("clouds") <= maxCloudValue){
            return true
        }
        return false
    }

}