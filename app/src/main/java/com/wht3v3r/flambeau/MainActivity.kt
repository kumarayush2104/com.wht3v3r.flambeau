package com.wht3v3r.flambeau

import android.app.NotificationManager
import android.hardware.*
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private var notiManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var cameraManager: CameraManager? = null
    private var proximitySensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var camera: String? = null

    private var lightView: TextView? = null
    private var proximityView: TextView? = null
    private var sensController: Slider? = null
    private var toggleButton: ToggleButton? = null
    private var sensView: TextView? = null

    private var whatsCurrentSensitivity: Float = 20F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.hide()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        notiManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        camera = cameraManager!!.cameraIdList[0]

        lightView = findViewById(R.id.lightView)
        proximityView = findViewById(R.id.proximityView)
        sensController = findViewById(R.id.SensController)
        toggleButton = findViewById(R.id.backgroundCheck)
        sensView = findViewById(R.id.sensView)

        sensController!!.stepSize = 0.1F
    }

    override fun onStart() {
        super.onStart()
        FlambeauThread().start()

        sensController!!.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            whatsCurrentSensitivity = value
            sensView!!.text = "Sensitivity: " + value.toString()
        })
    }

    inner class FlambeauThread: Thread(), SensorEventListener {

        private var lightValue = lightSensor!!.maximumRange
        private var proximityValue = proximitySensor!!.maximumRange

        override fun run() {
            super.run()
            sensorManager!!.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager!!.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        override fun onSensorChanged(p0: SensorEvent?) {
            if(p0!!.sensor.type == Sensor.TYPE_LIGHT) lightValue = p0.values[0]
            else if(p0.sensor.type == Sensor.TYPE_PROXIMITY) proximityValue = p0.values[0]

            onValueChanged()
        }

        private fun onValueChanged() {
            lightView!!.text = "Light Sensor: " + lightValue.toString()
            proximityView!!.text = "Proximity Sensor: " + proximityValue.toString()

            if(lightValue <  whatsCurrentSensitivity) cameraManager!!.setTorchMode(camera!!, true)
            else cameraManager!!.setTorchMode(camera!!, false)
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    override fun onPause() {
        super.onPause()
        if(!toggleButton!!.isChecked) {
            FlambeauThread().suspend()
            cameraManager!!.setTorchMode(camera!!, false)
            sensorManager!!.unregisterListener(FlambeauThread())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FlambeauThread().stop()
        cameraManager!!.setTorchMode(camera!!, false)
        sensorManager!!.unregisterListener(FlambeauThread())
    }
}