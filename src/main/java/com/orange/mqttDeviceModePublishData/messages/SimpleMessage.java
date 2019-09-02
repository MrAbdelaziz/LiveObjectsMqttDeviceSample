package com.orange.mqttDeviceModePublishData.messages;

import com.google.gson.Gson;
import com.orange.mqttDeviceModePublishData.json.devData.LoData;
import com.orange.mqttDeviceModePublishData.json.devData.SampleData;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class SimpleMessage {
    private static final int QOS = 1;

    byte[] prepareMessage(String stream, String model) {
        // create message
        LoData loData = new LoData();

        Date msgDt    = new Date();
        loData.s      = stream;
        loData.m      = model;
        loData.ts     = toISO8601UTC(msgDt);
        loData.loc    = new Double[] {
            48.125 + (((double) (msgDt.getTime() % 1000)) / 1000),
            2.185 + (((double) (msgDt.getTime() % 1000)) / 1000)
        };
        loData.v      = preparePayload(msgDt);
        loData.t      = Arrays.asList("SampleTag");

        String msg = new Gson().toJson(loData);
        System.out.println(msg);
        return msg.getBytes();
    }

    Object preparePayload(Date msgDt) {
        SampleData myData = new SampleData();
        myData.log = "Message from deviceMode on dev/data on " + msgDt;
        myData.temperature = (int) (Math.pow((msgDt.getTime() % 1000) / 100, 2));
        myData.hygrometry = (int) ((msgDt.getTime() % 1000) / 10);
        return myData;
    }

    public MqttMessage getMessage(String stream, String model) {
        MqttMessage mqttMessage = new MqttMessage(prepareMessage(stream, model));
        mqttMessage.setQos(QOS);
        return mqttMessage;
    }

    static String toISO8601UTC(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }
}