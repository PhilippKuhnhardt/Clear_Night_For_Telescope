package de.phk.telescopenightchecker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // val model = ViewModelProvider(this).get(WeatherViewModel)
        val model : WeatherViewModel by viewModels()
        model.setContext(application)
        model.getWeather().observe(this, Observer { goodWeather ->
            if(goodWeather){
                statusText.text = getString(R.string.good_weather_msg)
                backgroundImage.setImageResource(R.drawable.space)
            }else{
                statusText.text = getString(R.string.bad_weather_msg)
                backgroundImage.setImageResource(R.drawable.clouds)
            }
        })



        createNotificationChannel()

        val notificationHour = 12
        val notificationMinute = 0

        val current = LocalDateTime.now()
        var notificationDate = LocalDateTime.of(current.year, current.month, current.dayOfMonth, notificationHour, notificationMinute)

        if (current.until(notificationDate, ChronoUnit.MINUTES) < 0)
            notificationDate = notificationDate.plusDays(1)

        val periodicNotificationWorker = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(current.until(notificationDate, ChronoUnit.MINUTES), TimeUnit.MINUTES)
            .build()

        WorkManager
            .getInstance(this)
            .enqueue(periodicNotificationWorker)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.settings, menu)
        return true
    }

    private fun createNotificationChannel() {
        val CHANNEL_ID = "default"
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
}