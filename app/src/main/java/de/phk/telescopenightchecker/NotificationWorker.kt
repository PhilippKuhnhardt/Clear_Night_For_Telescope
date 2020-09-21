package de.phk.telescopenightchecker

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        downloadWeather()
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun sendNotification(title : String, text : String){
        val CHANNEL_ID = "default"
        var builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = 0
        with(NotificationManagerCompat.from(this.applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }

    /**
     * Downloads the weatherdata from openweathermap
     */
    private fun downloadWeather(){

        //TODO: Make this dynamic
        val lat = 49.853691
        val lon = 12.004130

        val key = BuildConfig.OWM_KEY
        var responseJSONObject: JSONObject? = null
        val queue = Volley.newRequestQueue(this.applicationContext)
        val url = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=current,minutely&appid=$key"
        //TODO: Remove before release
        Log.d("URL", url)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Result", response.toString())
                if(analyzeWeatherData(response)){
                    //Weather is fine
                    sendNotification(this.applicationContext.getString(R.string.notification_title), this.applicationContext.getString(R.string.good_weather_msg))
                }else{
                    //Weather isn't fine
                    sendNotification(this.applicationContext.getString(R.string.notification_title), this.applicationContext.getString(R.string.bad_weather_msg))
                }

            },
            { error ->
                // TODO: Handle error
                Log.d("Result", error.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }

    /**
     * Analyses the weather
     * @param weatherData Should contain the entirety of the downloaded weather Data
     * @return true = Weather is fine for tonight, false = Weather isn't good
     */
    private fun analyzeWeatherData(weatherData : JSONObject) : Boolean{
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
    private fun minimalCloudsForHour(hourData : JSONObject) : Boolean{
        val maxCloudValue = 10
        if (hourData.getInt("clouds") <= maxCloudValue){
            return true
        }
        return false
    }

    /**
     * @return  Returns which hours from the next 48 are relevant for the analysis. E.g. if it's 19pm and you want the data for 22pm-1am it will return an Array with 3 to 6
     */
    private fun getRelevantHours() : IntArray{
        //TODO: Make this dynamic
        //TODO: Consider that the start time might be on the next day
        val startHour = 21
        val startMinute = 0
        val endHour = 3
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
        if(array[0] < 0 ){
            array[0] = 0
        }
        array[1] = current.until(endDate, ChronoUnit.HOURS).toInt()

        return array
    }
}

