package com.orange.mqttDeviceModePublishData;

import com.orange.mqttDeviceModePublishData.messages.HashMapMessage;
import com.orange.mqttDeviceModePublishData.messages.SimpleMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * This sample will publish data for a device : device1
 * At this end you can take a look in the data zone of live objects to see the data sent.
 **/
public class DataPublisher {
	// Connection parameters
	public static final String  API_KEY   = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";           // <-- REPLACE by YOUR API_KEY!
	public static final String  CLIENT_ID = "urn:lo:nsid:samples:device1";                // in device mode : should be the syntax urn:lo:nsid:{namespace}:{id}
	public static final String  STREAM    = "device1stream";                              // timeseries this message belongs to
	public static final String  MODEL     = "devtype1";                                   // data indexing model
	public static final boolean SECURED   = true;                                         // TLS-secured connection ?

	/*
	 * Run in a loop, or just send 1 message ?
	 */
	public static final boolean LOOP     = true;
	/*
	 * MSG_SRC=1: simple message built with objects
     * MSG_SRC=2: simple message built with hash map
	 */
	public static final int     MSG_SRC  = 2;

	public static void main(String[] args) {
		try {
			// create and fill the connection options
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setPassword(API_KEY.toCharArray());
			connOpts.setUserName("json+device");             // needed to publish as a device

			String server;
			if (SECURED) {
				server = "ssl://liveobjects.orange-business.com:8883";
				connOpts.setSocketFactory(SSLUtils.getLiveObjectsSocketFactory());
            }
			else {
				server = "tcp://liveobjects.orange-business.com:1883";
			}

			// now connect to LO
			MqttClient sampleClient = new MqttClient(server, CLIENT_ID, new MemoryPersistence());
			sampleClient.connect(connOpts);
			System.out.println("Connected to Live Objects in Device Mode" + (SECURED ? " with TLS" : ""));

			do {
				SimpleMessage source;
				String topic;
				switch (MSG_SRC) {
					default:
					case 1:
						source = new SimpleMessage();
						topic = "dev/data";
						break;
					case 2:
						source = new HashMapMessage();
						topic = "dev/data";
						break;
				}
				MqttMessage message = source.getMessage(STREAM, MODEL);

				// send your message
				sampleClient.publish(topic, message);
				System.out.println("Message published");
				if (LOOP) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						break;
					}
				}
			} while (LOOP);

			// disconnect
			sampleClient.disconnect();
			System.out.println("Disconnected from Live Objects");

		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
		}
	}
}
