package com.itram.mytracker.services

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.itram.mytracker.other.Constants.ACTION_PAUSE_SERVICE
import com.itram.mytracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.itram.mytracker.other.Constants.ACTION_STOP_SERVICE

class TrackingService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Log.d("TRACKER", "START OR RESUME SERVICE")
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

}