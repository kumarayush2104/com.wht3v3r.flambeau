package com.wht3v3r.flambeau

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class Flambeau: Service(), SensorEventListener {

    private var sensorManager: SensorManager ?= null
    private var lightSensor: Sensor ?= null
    private var proximitySensor: Sensor ?= null
    private var cameraManager: CameraManager ?= null
    private  var camera: String ?= null
    var proximitySensorValue: Float = 10F
    var lightSensorValue: Float= 60F
    var sensitivity: Float = 10F

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        camera = cameraManager!!.cameraIdList[0] as String
        sensorManager!!.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)

        sendValues(0, proximitySensor!!.maximumRange)

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {

        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                var sensitivityReceiver = intent!!.getFloatExtra("Sensitivity", 100F)
                if (sensitivityReceiver != 100F) sensitivity = sensitivityReceiver
            }
        }, IntentFilter("SensitivityValues"))


        var sensorType = event!!.sensor.type
        if( sensorType == Sensor.TYPE_PROXIMITY) {
            proximitySensorValue = event.values[0]
            sendValues(0, event.values[0])
        }
        else if (sensorType == Sensor.TYPE_LIGHT) {
            lightSensorValue = event.values[0]
            sendValues(1, event.values[0])
        }



        if(proximitySensorValue == proximitySensor!!.maximumRange) {
            if(lightSensorValue!! < sensitivity!!) cameraManager!!.setTorchMode(camera!!, true)
            else cameraManager!!.setTorchMode(camera!!, false)
        } else cameraManager!!.setTorchMode(camera!!, false)

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager!!.unregisterListener(this)
    }

    fun sendValues(SensorType: Int, SensorValue: Float) {
        // 0 for Proximity, 1 for Light
        val broadcastIntent = Intent("SensorValues")

        if (SensorType == 1) broadcastIntent.putExtra("Light", SensorValue.toString())
        else if (SensorType == 0) broadcastIntent.putExtra("Proximity", SensorValue.toString())

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}