package com.example.trace_back_nfc


import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttException
import java.io.IOException
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity() {

//    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfc_ReadButton: Button
    private lateinit var nfc_WriteButton: Button
    private lateinit var nfc_Read_EditText: EditText
    private lateinit var nfc_Write_EditText: EditText
    private lateinit var nfc_Error_EditText: EditText

    private lateinit var mqttAndroidClient: MqttAndroidClient

    private lateinit var mqtt_trigger_supply_chain_task_Button: Button

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfc_Read_EditText = findViewById(R.id.nfc_read_edittext)
        nfc_Write_EditText = findViewById(R.id.nfc_write_edittext)
        nfc_Error_EditText = findViewById(R.id.nfc_error_edittext)

        nfc_ReadButton = findViewById(R.id.nfc_read_button)
        nfc_WriteButton = findViewById(R.id.nfc_write_button)

        nfc_ReadButton.setOnClickListener() { read_tag() }
        nfc_WriteButton.setOnClickListener() { write_tag() }

        mqtt_trigger_supply_chain_task_Button = findViewById(R.id.mqtt_supply_chain_button)
        mqtt_trigger_supply_chain_task_Button.setOnClickListener() { mqtt_trigger_supply_chain_task() }

        connect(getApplicationContext())

//        val test_mqtt_class = Test_Mqtt_Class()
//        test_mqtt_class.connect(getApplicationContext())

//        // Check for available NFC Adapter
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//        if (nfcAdapter == null) {
//            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
//            finish()
//            return
//        }
//        // Register callback
//        nfcAdapter?.setNdefPushMessageCallback(this, this)
    }

    private fun mqtt_trigger_supply_chain_task() {
        val test_mqtt_class = Test_Mqtt_Class()
        val trigger_json = "{\"start\":1,\"delay\":1}"
        //nfc_Read_EditText.text
        Log.i(" mqtt_trigger_supply_chain_task0", "post")
        //test_mqtt_class.publish("yieldy1/adjust_brightness", trigger_json,mqttAndroidClient)
        //test_mqtt_class.publish("yieldy2/adjust_brightness", trigger_json,mqttAndroidClient)
        //test_mqtt_class.publish("yieldy3/adjust_brightness", trigger_json,mqttAndroidClient)

//        test_mqtt_class.publish("manufacture/main", trigger_json,mqttAndroidClient)
//        test_mqtt_class.publish("manufacture/drying", trigger_json,mqttAndroidClient)
        test_mqtt_class.publish(nfc_Read_EditText.text.toString(), trigger_json,mqttAndroidClient)
        Log.i(" mqtt_trigger_supply_chain_task1", "wait")
    }

    @ExperimentalStdlibApi
    private fun read_tag() {
        val tag_intent:Intent = getIntent();
        tag_intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val nDefTag = Ndef.get(tag)
            nfc_Read_EditText.setText(nDefTag.cachedNdefMessage.getRecords()[0].payload.decodeToString(3,nDefTag.cachedNdefMessage.getRecords()[0].payload.size,false))
//            Log.i("nfc_read_message0", "item at ${nDefTag.cachedNdefMessage}")
//            Log.i("nfc_read_message1", "item at ${nDefTag.cachedNdefMessage.getRecords()[0]}")
//            Log.i("nfc_read_message2", "item at ${nDefTag.cachedNdefMessage.getRecords()[0].payload.decodeToString(3,nDefTag.cachedNdefMessage.getRecords()[0].payload.size,false)}")
        }
//        tag_intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMsgs ->
//
//            val messages: List<NdefMessage> = rawMsgs.map { it as NdefMessage }
//            for (message_index in messages.indices) {
//                Log.i("nfc_read_message", "item at $message_index is ${messages[message_index]}")
//                Log.i("nfc_read_NdefMessage", "item at ${messages[message_index]} is ${messages[message_index].getRecords()[0]}")
//                Log.i("nfc_read_payload", "item at $message_index is ${messages[message_index].getRecords()[0].payload}")
//                nfc_EditText.setText(messages[message_index].getRecords()[0].payload.toString())
//                // println("item at $index is ${items[index]}")
//            }
//        }
    }

    private fun write_tag(): Boolean{
        val payload:String=nfc_Write_EditText.text.toString()
        val current_Locale:Locale = Locale.getDefault()
        //val domain:Locale="com.example.trace_back_nfc"
        val encodeInUtf8_determine:Boolean = true
        val nfcRecord =  createTextRecord(payload,current_Locale,encodeInUtf8_determine)
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        val tag_intent:Intent = getIntent();
        //val messageWrittenSuccessfully:Boolean = false
        tag_intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val messageWrittenSuccessfully:Boolean = writeMessageToTag(nfcMessage, tag)
            if (messageWrittenSuccessfully)
                nfc_Error_EditText.setText("Successful Written to Tag".toString())
            else
                nfc_Error_EditText.setText("Something When wrong Try Again".toString())
            return messageWrittenSuccessfully
        }
        return false
    }

    fun createTextRecord(payload: String, locale: Locale, encodeInUtf8: Boolean): NdefRecord {
        //US-ASCII Charset.forName("US-ASCII").toString()
        //[122, 104] [zh]  langBytes
        val langBytes = locale.language.toByteArray(Charset.forName("US-ASCII"))
        val utfEncoding = if (encodeInUtf8) Charset.forName("UTF-8") else Charset.forName("UTF-16")
        val textBytes = payload.toByteArray(utfEncoding)
        //UTF-8 utfEncoding.toString() utfEncoding過了
        //這邊一個中文字組 (Word)由三個字節產生，英文為一個字節ex:len(真理大學UCAN實驗室)=25
        //[-25, -100, -97, -25, -112, -122, -27, -92, -89, -27, -83, -72, 85, 67, 65, 78, -27, -81, -90, -23, -87, -105, -27, -82, -92] textBytes.contentToString()
        //shl 左移一位 utfBit:0 or 128
        val utfBit: Int = if (encodeInUtf8) 0 else 1 shl 7
        //langBytes.size=2=[zh]
        val status = (utfBit + langBytes.size).toChar()
        val data = ByteArray(1 + langBytes.size + textBytes.size)
        data[0] = status.toByte()
        //System.arraycopy(來源陣列、來源起始索引、目的陣列、目的起始索引、複製長度)
        System.arraycopy(langBytes, 0, data, 1, langBytes.size)
        System.arraycopy(textBytes, 0, data, 1 + langBytes.size, textBytes.size)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), data)
    }

    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    //Message to large to write to NFC tag
                    Log.i("nfc_write_writeMessageToTag", "2")
                    return false
                }
                if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    //Message is written to tag
                    Log.i("nfc_write_writeMessageToTag", "3")
                    return true
                } else {
                    //NFC tag is read-only
                    Log.i("nfc_write_writeMessageToTag", "4")
                    return false
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)

            nDefFormatableTag?.let {
                try {
                    Log.i("nfc_write_writeMessageToTag", "5")
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    //The data is written to the tag
                    return true
                } catch (e: IOException) {
                    //Failed to format tag
                    Log.i("nfc_write_writeMessageToTag", "6")
                    Log.i("nfc_write_writeMessageToTag", e.toString())
                    return false
                }
            }
            //NDEF is not supported
            Log.i("nfc_write_writeMessageToTag", "7")
            return false

        } catch (e: Exception) {
            Log.i("nfc_write_writeMessageToTag", "8")
            Log.i("nfc_write_writeMessageToTag", e.toString())
            //Write operation has failed
        }
        return false
    }

    fun connect(applicationContext : Context) {
        //REASON_CODE_INVALID_CLIENT_ID(A-Z、a-z、0-9、'./_%)
        mqttAndroidClient = MqttAndroidClient ( applicationContext.applicationContext,"tcp://192.168.1.133:1883","ucan_lab")
        //mqttAndroidClient = MqttAndroidClient ( applicationContext.applicationContext,"tcp://mqtt.eclipse.org:1883","ucan_lab")
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
}
