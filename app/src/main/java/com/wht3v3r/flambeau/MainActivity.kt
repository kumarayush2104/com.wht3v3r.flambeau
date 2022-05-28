package com.wht3v3r.flambeau

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.widget.*
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var lightsensor: Sensor ?= null
    var detail: TextView ?= null
    var detail1: TextView ?= null
    var cm: CameraManager ?= null
    var camera: String ?= null
    var sm: SensorManager ?= null
    private var proxsensor: Sensor ?= null
    var proximity: Float ?= null
    var sens: Float = 10F
    var light: Float ?= null
    var tglbut: ToggleButton ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightsensor = sm!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        detail = findViewById(R.id.textview)
        detail1 = findViewById(R.id.textview1)
        cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        camera = cm!!.cameraIdList[0] as String
        proxsensor = sm!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        tglbut = findViewById(R.id.bgcheck)
        var slider: Slider = findViewById(R.id.sensitivity)
        var sensIndicator: TextView = findViewById(R.id.textview2)
        sensIndicator.setText("Sensitivity: " + sens.toString())

        slider.addOnChangeListener(object: Slider.OnChangeListener {
            @SuppressLint("RestrictedApi")
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
              sensIndicator.setText("Sensitivity: " + value.toString())
                sens = value
            }
        })
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var type = event!!.sensor

        if ( type.type == Sensor.TYPE_PROXIMITY) {
            detail1!!.setText("Proximity Sensor: " + event.values[0].toString())
            proximity = event.values[0]
        }
        if (type.type == Sensor.TYPE_LIGHT) {
            detail!!.setText("Light Sensor: " + event.values[0].toString())
            light = event.values[0]
        }

        if(proximity == proxsensor!!.maximumRange) {
            if(light!! < sens!!) {
                cm!!.setTorchMode(camera!!, true)
            } else {
                cm!!.setTorchMode(camera!!, false)
            }
        } else {
            cm!!.setTorchMode(camera!!, false)
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onResume() {
        super.onResume()
        sm!!.registerListener(this, lightsensor, SensorManager.SENSOR_DELAY_NORMAL)
        sm!!.registerListener(this, proxsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        if(tglbut!!.isChecked) {
            startActivity(intent)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sm!!.unregisterListener(this)
    }
}