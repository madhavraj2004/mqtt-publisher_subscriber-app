package com.example.task.ui.home;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttViewModel extends ViewModel {
    private static final String TAG = "MqttViewModel";
    private MqttAndroidClient mqttClient;

    public MqttAndroidClient getMqttClient(Context context) {
        if (mqttClient == null) {
            String clientId = MqttClient.generateClientId();
            mqttClient = new MqttAndroidClient(context, "tcp://broker.hivemq.com:1883", clientId);
            Log.d(TAG, "MQTT client created: " + clientId);
        }
        return mqttClient;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disconnectClient();
    }

    public void disconnectClient() {
        try {
            if (mqttClient != null) {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                    Log.d(TAG, "MQTT client disconnected");
                } else {
                    Log.d(TAG, "MQTT client is not connected, no need to disconnect");
                }
            } else {
                Log.d(TAG, "MQTT client is null");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error while disconnecting: " + e.getMessage());
        }
    }


}
