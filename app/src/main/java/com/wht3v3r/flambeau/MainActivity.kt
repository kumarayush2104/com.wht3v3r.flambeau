package com.wht3v3r.flambeau

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.hardware.*
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.app.NotificationCompat
import com.google.android.material.slider.Slider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private var notificationManager: NotificationManager? = null
    private var sensorManager: SensorManager? = null
    private var cameraManager: CameraManager? = null
    private var proximitySensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var camera: String? = null
    var executor: ExecutorService? = null

    private var lightView: TextView? = null
    private var proximityView: TextView? = null
    private var sensController: Slider? = null
    private var toggleButton: ToggleButton? = null
    private var sensView: TextView? = null

    private var liveNotification: Notification? = null

    private var whatsCurrentSensitivity: Float = 20F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.hide()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        executor = Executors.newFixedThreadPool(1)


        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        camera = cameraManager!!.cameraIdList[0]

        lightView = findViewById(R.id.lightView)
        proximityView = findViewById(R.id.proximityView)
        sensController = findViewById(R.id.SensController)
        toggleButton = findViewById(R.id.backgroundCheck)
        sensView = findViewById(R.id.sensView)

        val notificationChannel = NotificationChannel("default", "Flambeau Service" , NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationChannel.setSound(null, null)

        notificationManager!!.createNotificationChannel(notificationChannel)

        liveNotification = NotificationCompat.Builder(this, "default").setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setContentTitle("Flambeau Service is still running in background").setOngoing(true).build()


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
            if(this.isInterrupted) return
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

            if(lightValue <  whatsCurrentSensitivity  && proximityValue == proximitySensor!!.maximumRange) cameraManager!!.setTorchMode(camera!!, true)
            else cameraManager!!.setTorchMode(camera!!, false)
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    override fun onPause() {
        super.onPause()
        if(!toggleButton!!.isChecked) {
            exitProcess(1)
        } else {
            notificationManager!!.notify(1, liveNotification)
        }
    }

    override fun onResume() {
        super.onResume()
        notificationManager!!.cancelAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager!!.cancelAll()
    }
}