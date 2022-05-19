package com.wht3v3r.flambeau

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var sensor: Sensor ?= null
    var detail: TextView ?= null
    var cm: CameraManager ?= null
    var camera: String ?= null
    var sm: SensorManager ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sm!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        detail = findViewById(R.id.textview)
        cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        camera = cm!!.cameraIdList[0]
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var value = event!!.values[0]
        detail!!.setText(value.toString())
        if (value < 12) {
            cm!!.setTorchMode(camera.toString(), true)
        } else {
            cm!!.setTorchMode(camera.toString(), false)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onResume() {
        super.onResume()
        sm!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sm!!.unregisterListener(this)
    }
}