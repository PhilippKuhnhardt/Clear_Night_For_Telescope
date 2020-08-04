package de.phk.telescopenightchecker

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*
import java.time.LocalTime as LocalTime

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadWeather()
        getRelevantHours()
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

    /**
     * Downloads the weatherdata from openweathermap
     */
    fun downloadWeather(){

        //TODO: Make this dynamic
        val lat = 49.853691
        val lon = 12.004130

        val key = BuildConfig.OWM_KEY
        var responseJSONObject: JSONObject? = null
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=current,minutely&appid=$key"
        //TODO: Remove before release
        Log.d("URL", url)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                Log.d("Result", response.toString())
                if(analyzeWeatherData(response)){
                    //Weather is fine
                    statusText.text = "Weather is good tonight"
                    backgroundImage.setImageResource(R.drawable.space)
                }else{
                    //Weather isn't fine
                    statusText.text = "Weather is bad tonight"
                    backgroundImage.setImageResource(R.drawable.clouds)
                }

            },
            Response.ErrorListener { error ->
                // TODO: Handle error
                Log.d("Result", error.toString())
                Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG).show()
            }
        )
        queue.add(jsonObjectRequest)
    }

    /**
     * Analyses the weather
     * @param weatherData Should contain the entirety of the downloaded weather Data
     * @return true = Weather is fine for tonight, false = Weather isn't good
     */
    fun analyzeWeatherData(weatherData : JSONObject) : Boolean{
        val weatherArray : JSONArray = weatherData.get("hourly") as JSONArray
        val relevantHours : IntArray = getRelevantHours()

        for (i in relevantHours[0]..relevantHours[1]){
            Log.d("array $i", minimalCloudsForHour(weatherArray.getJSONObject(i)).toString())
            if(!minimalCloudsForHour(weatherArray.getJSONObject(i))){
                return false
            }
        }
        return true
    }

    /**
     * Checks if the clouds don't exceed the limit for this hour
     * @param hourData Should contain the data of an entire hour, meaning an array of the hourly JSONArray
     * @return returns if the cloudValue is smaller than 10 for this hour
     */
    fun minimalCloudsForHour(hourData : JSONObject) : Boolean{
        val maxCloudValue = 10
        if (hourData.getInt("clouds") <= maxCloudValue){
            return true
        }
        return false
    }

    /**
     * @return  Returns which hours from the next 48 are relevant for the analysis. E.g. if it's 19pm and you want the data for 22pm-1am it will return an Array with 3 to 6
     */
    fun getRelevantHours() : IntArray{
        //TODO: Make this dynamic
        val startHour = 21
        val startMinute = 0
        val endHour = 2
        val endMinute = 0

        val current = LocalDateTime.now()
        val startDate = LocalDateTime.of(current.year, current.month, current.dayOfMonth, startHour, startMinute)
        val endDate = LocalDateTime.of(current.year, current.month, current.dayOfMonth.plus(1), endHour, endMinute)

        Log.d("LocalDateTime", current.toString())
        Log.d("LocalDateTime", startDate.toString())
        Log.d("LocalDateTime", endDate.toString())
        Log.d("LocalDateTime", current.until(startDate, ChronoUnit.HOURS).toString())
        Log.d("LocalDateTime", current.until(endDate, ChronoUnit.HOURS).toString())

        val array = IntArray(2)
        array[0] = current.until(startDate, ChronoUnit.HOURS).toInt()
        array[1] = current.until(endDate, ChronoUnit.HOURS).toInt()

        return array
    }

}