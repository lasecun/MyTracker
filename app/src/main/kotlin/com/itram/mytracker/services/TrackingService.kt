package com.itram.mytracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.itram.mytracker.R
import com.itram.mytracker.other.Constants.ACTION_PAUSE_SERVICE
import com.itram.mytracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.itram.mytracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.itram.mytracker.other.Constants.ACTION_STOP_SERVICE
import com.itram.mytracker.other.Constants.FASTEST_LOCATION_INTERVAL
import com.itram.mytracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.itram.mytracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.itram.mytracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.itram.mytracker.other.Constants.NOTIFICATION_ID
import com.itram.mytracker.ui.MainActivity

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    private var isFirstRun = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        postInitialValue()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracker(it)
        })
    }

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracker(isTracking: Boolean) {
        if (isTracking) {
            val request = LocationRequest().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_LOCATION_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Log.d("location", "Location: ${location.longitude} + ${location.latitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun postInitialValue() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        Log.d("TRACKER", "START SERVICE")
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Log.d("TRACKER", "RESUME SERVICE")
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Log.d("TRACKER", "ACTION_PAUSE_SERVICE")
                }

                ACTION_STOP_SERVICE -> {
                    Log.d("TRACKER", "ACTION_STOP_SERVICE")
                }

                else -> Unit
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyline()

        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentInfo("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}