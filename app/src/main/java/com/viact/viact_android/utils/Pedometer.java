package com.viact.viact_android.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Pedometer implements SensorEventListener {
    private static final String TAG = "Walk";

    public interface PedometerListener {
        void onNewStep(int step);
    }

    private PedometerListener listener;

    private SensorManager sensorManager;
    private Sensor stepsensor;
    private Sensor walksensor;


    public Pedometer(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepsensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
//        walksensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    public void start() {
        sensorManager.registerListener(this, stepsensor, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this, walksensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void setListener(PedometerListener l) {
        listener = l;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR ) {
                if (listener != null) {
                    listener.onNewStep(0);
                }
            }
//            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
//                if (listener != null) {
//                    listener.onNewStep(0);//(int)event.values[0]);
//                }
//            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
