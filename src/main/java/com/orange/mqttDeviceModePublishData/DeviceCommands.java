package com.orange.mqttDeviceModePublishData;

import com.google.gson.Gson;
import com.orange.mqttDeviceModePublishData.json.devData.LoCommand;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;

import static com.orange.mqttDeviceModePublishData.MqttTopics.MQTT_TOPIC_SUBSCRIBE_COMMAND;

@SuppressWarnings("WeakerAccess")
public class DeviceCommands implements MqttCallback {
    public static final int QOS = 1;
    private final MqttClient mqttClient;

    public DeviceCommands(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void subscribeToCommands() throws MqttException {
        // register callback (to handle received commands)
        mqttClient.setCallback(this);

        // Subscribe to data
        mqttClient.subscribe(MQTT_TOPIC_SUBSCRIBE_COMMAND);
        System.out.println("Device commands subscribed.");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        // parse message as command
        LoCommand command = new Gson().fromJson(new String(mqttMessage.getPayload()), LoCommand.class);
        System.out.println("Device command received: " + new Gson().toJson(command));

        LoCommand.LoCommandResponse response = new LoCommand.LoCommandResponse(new HashMap<>(), command.cid);
        response.res.put("my-ack", "this is my command acknowledge to " + command.req);

        new Thread(() -> {
            try {

                String responseJson = new Gson().toJson(response);
                System.out.println("Publishing command acknowledge message: " + responseJson);

                MqttMessage message = new MqttMessage(responseJson.getBytes());
                message.setQos(QOS);

                mqttClient.publish(MqttTopics.MQTT_TOPIC_RESPONSE_COMMAND, message);
                System.out.println("Command ack published");

            } catch (MqttException me) {
                System.out.println("reason " + me.getReasonCode());
                System.out.println("msg " + me.getMessage());
                System.out.println("loc " + me.getLocalizedMessage());
                System.out.println("cause " + me.getCause());
                System.out.println("excep " + me);
                me.printStackTrace();
           }
        }).start();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Device commands: Connection lost");
        mqttClient.notifyAll();
    }
}
