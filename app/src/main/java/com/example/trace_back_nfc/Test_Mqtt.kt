package com.example.trace_back_nfc

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class Test_Mqtt_Class {

    private lateinit var mqttAndroidClient: MqttAndroidClient

    fun connect(applicationContext : Context) {
        //REASON_CODE_INVALID_CLIENT_ID(A-Z、a-z、0-9、'./_%)
        //mqttAndroidClient = MqttAndroidClient ( applicationContext.applicationContext,"tcp://192.168.0.133:1883","ucan_lab")
        mqttAndroidClient = MqttAndroidClient ( applicationContext.applicationContext,"tcp://mqtt.eclipse.org:1883","ucan_lab")
        try {
            val token = mqttAndroidClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("Connection", "success")
                    //connectionStatus = true
                    // Give your callback on connection established here
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false
                    Log.i("Connection", "failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String) {
        //訂閱
        val qos = 2 // Mention your qos value
        try {
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Give your callback on Subscription here
                }
                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    // Give your subscription failure callback here
                }
            })
        } catch (e: MqttException) {
            // Give your subscription failure callback here
        }
    }

    fun unSubscribe(topic: String) {
        //取消訂閱
        try {
            val unsubToken = mqttAndroidClient.unsubscribe(topic)
            unsubToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Give your callback on unsubscribing here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Give your callback on failure here
                }
            }
        } catch (e: MqttException) {
            // Give your callback on failure here
        }
    }

    fun receiveMessages() {
        //接收資訊
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                //connectionStatus = false
                // Give your callback on failure here
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    val data = String(message.payload, charset("UTF-8"))
                    // data is the desired received message
                    // Give your callback on message received here
                } catch (e: Exception) {
                    // Give your callback on error here
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.i("receiveMessages_deliveryComplete1", token.toString())
                // Acknowledgement on delivery complete
            }
        })
    }

    fun publish(topic: String, data: String,mqttAndroidClient:MqttAndroidClient) {
        //傳送訊息
        val encodedPayload : ByteArray
        try {
            encodedPayload = data.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.qos = 2
            message.isRetained = false
            mqttAndroidClient.publish(topic, message)
        } catch (e: Exception) {
            // Give Callback on error here
            Log.i(" publish_error0", e.toString())
        } catch (e: MqttException) {
            Log.i(" publish_error1", e.toString())
            // Give Callback on error here
        }
    }

    fun disconnect() {
        //取消連接
        try {
            val disconToken = mqttAndroidClient.disconnect()
            disconToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    //connectionStatus = false
                    // Give Callback on disconnection here
                }
                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    // Give Callback on error here
                }
            }
        } catch (e: MqttException) {
            // Give Callback on error here
        }
    }

    // Author: Chaitanya Bhojwani Aug 21, 2019，Fork by 真理大學UCAN實驗室
    // https://medium.com/@chaitanya.bhojwani1012/eclipse-paho-mqtt-android-client-using-kotlin-56129ff5fbe7
}