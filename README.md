# MQTT Publisher/Subscriber App

This repository is a **Java-based test application for MQTT**, designed to demonstrate and test MQTT publish/subscribe capabilities. It provides a simple interface for publishing messages to MQTT topics and subscribing to receive messages from MQTT topics, making it ideal for learning, debugging, or verifying MQTT broker setups.

---

## Features

- **Publish messages** to any MQTT topic.
- **Subscribe** to MQTT topics and receive real-time messages.
- **Test connectivity** and message flow with an MQTT broker.
- Simple, clean Java codebase for easy understanding and extension.

---

## What is MQTT?

[MQTT](https://mqtt.org/) (Message Queuing Telemetry Transport) is a lightweight messaging protocol optimized for IoT, mobile, and low-bandwidth applications. It uses a publish/subscribe model, allowing devices to communicate asynchronously through a central broker.

---

## Getting Started

### Prerequisites

- Java 8 or newer
- An MQTT broker (e.g., [Mosquitto](https://mosquitto.org/), [HiveMQ public broker](https://www.hivemq.com/public-mqtt-broker/), etc.)
- [Eclipse Paho](https://www.eclipse.org/paho/clients/java/) or another MQTT Java client library (if not already included)

### Setup

1. **Clone the repository:**

   ```sh
   git clone https://github.com/madhavraj2004/mqtt-publisher_subscriber-app.git
   cd mqtt-publisher_subscriber-app
   ```

2. **Install dependencies:**
   
   - If using Maven, add the following to your `pom.xml`:
     ```xml
     <dependency>
       <groupId>org.eclipse.paho</groupId>
       <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
       <version>1.2.5</version>
     </dependency>
     ```
   - Or download the JAR directly from the [Paho downloads](https://www.eclipse.org/paho/clients/java/).

3. **Configure MQTT Broker settings:**  
   Update the broker URL, client ID, username, and password as needed in the application's source code.

---

## Usage

### Running the App

Compile and run the application using your favorite IDE or from the command line:

```sh
javac -cp .:path/to/org.eclipse.paho.client.mqttv3-1.2.5.jar YourMainClass.java
java -cp .:path/to/org.eclipse.paho.client.mqttv3-1.2.5.jar YourMainClass
```

### Using the Publisher

- Set the topic and the message you want to publish in the app or via command line (based on implementation).
- Publish the message.
- Observe delivery confirmation or error logs.

### Using the Subscriber

- Set the topic you want to subscribe to.
- Start the subscriber.
- Wait for messages and observe the received data in the console.

---

## Example

```java
String broker       = "tcp://broker.hivemq.com:1883";
String clientId     = "JavaSample";
String topic        = "test/topic";

MqttClient client = new MqttClient(broker, clientId);
client.connect();
client.subscribe(topic, (t, msg) -> {
    System.out.println("Received: " + new String(msg.getPayload()));
});
client.publish(topic, new MqttMessage("Hello MQTT!".getBytes()));
```

---

## License

This project is open source and available under the [MIT License](LICENSE).

---

## Acknowledgements

- [Eclipse Paho MQTT Java Client](https://www.eclipse.org/paho/clients/java/)
- [MQTT.org](https://mqtt.org/)

---

Feel free to open issues or pull requests to improve this test application!
