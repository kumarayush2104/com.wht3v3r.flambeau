package com.wht3v3r.flambeau

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {
    var lightSensorView: TextView ?= null
    var proximitySensorView: TextView ?= null
    var FlambeauService: Intent ?= null
    var sensitivitySlider: Slider ?= null
    var sensitivityIndicator: TextView ?= null
    var backgroundServiceToggleButton: ToggleButton ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lightSensorView = findViewById(R.id.lightSensor)
        proximitySensorView = findViewById(R.id.proximitySensor)
        FlambeauService = Intent(this, Flambeau::class.java)
        sensitivitySlider = findViewById(R.id.sensitivitySlider)
        sensitivityIndicator = findViewById(R.id.sensitivityValue)
        backgroundServiceToggleButton = findViewById(R.id.backgroundServiceToggle)

       LocalBroadcastManager.getInstance(this).registerReceiver( object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                var lightValues = intent!!.getStringExtra("Light")
                var proximityValues = intent!!.getStringExtra("Proximity")
                if(lightValues != null ) lightSensorView!!.setText("Light Sensor: " + lightValues.toString())
                if(proximityValues != null ) proximitySensorView!!.setText("Proximity Sensor: " + proximityValues.toString())
            }
        },  IntentFilter("SensorValues"))
    }

    override fun onResume() {
        super.onResume()
        sensitivitySlider!!.addOnChangeListener(object: Slider.OnChangeListener {
            @SuppressLint("RestrictedApi")
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                sensitivityIndicator!!.setText("Sensitivity: " + value.toString())
                sendSensitivityValues(value)
            }
        })
        startService(FlambeauService)
    }

    override fun onPause() {
        super.onPause()
        if(!backgroundServiceToggleButton!!.isChecked) finish()

    }
    fun sendSensitivityValues(value: Float) {
        val intent = Intent("SensitivityValues")
        intent.putExtra("Sensitivity", value)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}