package com.example.task.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.task.R;
import com.github.mikephil.charting.data.Entry;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import info.mqtt.android.service.MqttAndroidClient;
import info.mqtt.android.service.Ack;

public class HomeFragment extends Fragment {
    private static final int MAX_RETRIES = 5; // Set a maximum number of retries to prevent infinite attempts
    private static final long RETRY_INTERVAL_MS = 5000; // Initial retry interval (5 seconds)
    private long retryInterval = RETRY_INTERVAL_MS; // Dynamic retry interval for backoff

    private static String MQTT_TOPIC = ""; // Default topic
    private static final String MQTT_BROKER_URL = "tcp://broker.hivemq.com:1883";


    private static final int MAX_RECORDS = 10; // Size of the data queue for each parameter

    // Initialize a map that holds queues for each parameter
    private Map<String, ArrayBlockingQueue<Entry>> dataQueues = new HashMap<>();
    private EditText editTextDeviceId;
    private Button buttonSubscribe;
    private TextView textViewStatus;
    private TextView textViewReceivedMessages;

    private MqttAndroidClient mqttClient;
    private String currentTopic;
    private Map<String, List<Entry>> currentData = new HashMap<>();

    private EditText editTextTopic;
    private EditText editTextMessage;


    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewStatus = view.findViewById(R.id.textViewStatus);
        textViewReceivedMessages = view.findViewById(R.id.textViewReceivedMessages);
        editTextTopic = view.findViewById(R.id.editTextTopic);
        editTextMessage = view.findViewById(R.id.editTextMessage);

        buttonSubscribe = view.findViewById(R.id.buttonsubscribe);
        buttonSubscribe.setOnClickListener(v -> {
            // Use the topic entered or the default one if empty
            MQTT_TOPIC = editTextTopic.getText().toString().isEmpty() ? MQTT_TOPIC : editTextTopic.getText().toString();
            String message = editTextMessage.getText().toString();
            publishMessage(MQTT_TOPIC, message);  // Call to publish the message
        });

        handler = new Handler();
        setupMqttClient();  // Setup MQTT client

        return view;
    }

    private void setupMqttClient() {
        String clientId = UUID.randomUUID().toString();
        mqttClient = new MqttAndroidClient(requireContext().getApplicationContext(), MQTT_BROKER_URL, clientId, Ack.AUTO_ACK);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "Connected to MQTT broker");
                    textViewStatus.setText("Connected to MQTT broker");
                    retryInterval = RETRY_INTERVAL_MS; // Reset retry interval on successful connection
                    subscribeToTopic(MQTT_TOPIC); // Subscribe to the topic on connection
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Failed to connect to MQTT broker: " + exception.toString());
                    textViewStatus.setText("Failed to connect: " + exception.getMessage());

                    // Retry connection logic with backoff
                    if (retryInterval <= (RETRY_INTERVAL_MS * MAX_RETRIES)) {
                        Log.d("MQTT", "Retrying connection in " + retryInterval + "ms");
                        handler.postDelayed(() -> setupMqttClient(), retryInterval);
                        retryInterval *= 2; // Exponential backoff
                    } else {
                        Log.e("MQTT", "Max retries reached. Unable to connect to MQTT broker.");
                        textViewStatus.setText("Max retry attempts reached. Unable to connect.");
                    }
                }
            });

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("MQTT", "Connection lost: " + (cause != null ? cause.getMessage() : "Unknown error"));
                    textViewStatus.setText("Connection lost. Reconnecting...");
                    // Try to reconnect if the connection is lost
                    setupMqttClient();
                }

                @Override
                public void messageArrived(String MQTT_TOPIC, MqttMessage message) throws Exception {
                    // When a message is received, update the UI or process the message as needed
                    String receivedMessage = new String(message.getPayload());
                    Log.d("MQTT", "Message received from topic " + MQTT_TOPIC + ": " + receivedMessage);
                    textViewReceivedMessages.setText("Message received: " + receivedMessage);
                    // Process the message (example: parsing JSON, updating charts, etc.)
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Optional: Handle message delivery confirmation if necessary
                    Log.d("MQTT", "Message delivery completed for token: " + token.getMessageId());
                }
            });
        } catch (Exception e) {
            Log.e("MQTT", "Error setting up MQTT client: " + e.getMessage());
            textViewStatus.setText("Error setting up MQTT client.");
        }
    }

    // Method to subscribe to a topic
    private void subscribeToTopic(String MQTT_TOPIC) {
        try {
            mqttClient.subscribe(MQTT_TOPIC, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "Successfully subscribed to topic: " + MQTT_TOPIC);
                    textViewStatus.setText("Subscribed to topic: " + MQTT_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Failed to subscribe to topic: " + MQTT_TOPIC + ", " + exception.getMessage());
                    textViewStatus.setText("Failed to subscribe to topic.");
                }
            });
        } catch (Exception e) {
            Log.e("MQTT", "Error subscribing to topic: " + e.getMessage());
        }
    }

    // Method to publish a message to a topic
    private void publishMessage(String MQTT_TOPIC, String message) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(1);  // Set the QoS level (1 for "at least once delivery")
                mqttClient.publish(MQTT_TOPIC, mqttMessage, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("MQTT", "Message published successfully to topic: " + MQTT_TOPIC);
                        textViewStatus.setText("Message published successfully.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e("MQTT", "Failed to publish message to topic: " + MQTT_TOPIC + ", " + exception.getMessage());
                        textViewStatus.setText("Failed to publish message.");
                    }
                });
            } catch (Exception e) {
                Log.e("MQTT", "Error publishing message: " + e.getMessage());
                textViewStatus.setText("Error publishing message.");
            }
        } else {
            Log.e("MQTT", "MQTT client is not connected.");
            textViewStatus.setText("Not connected to MQTT broker.");
        }
    }


}
