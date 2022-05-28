package com.wht3v3r.flambeau

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var lightsensor: Sensor ?= null
    var detail: TextView ?= null
    var detail1: TextView ?= null
    var cm: CameraManager ?= null
    var camera: String ?= null
    var sm: SensorManager ?= null
    private var proxsensor: Sensor ?= null
    var sens: Int = 5
    var proximity: Float ?= null
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
    }

    fun refresh(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when(view.id) {
                R.id.lowcheck -> if (checked) {
                    sens = 5
                }
                R.id.medcheck -> if (checked) {
                    sens = 11
                }
                R.id.highcheck -> if (checked) {
                    sens = 16
                }
            }
        }
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
            if(light!! < sens) {
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