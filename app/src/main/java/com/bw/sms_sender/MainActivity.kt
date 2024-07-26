package com.bw.sms_sender

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var phoneNumberEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var sendSmsButton: Button

    private val REQUEST_SEND_SMS = 1
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        messageEditText = findViewById(R.id.messageEditText)
        sendSmsButton = findViewById(R.id.sendSmsButton)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            getPhoneNumber()
        }

        sendSmsButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString()
            val message = messageEditText.text.toString()
            if (phoneNumber.isNotEmpty() && message.isNotEmpty()) {
                checkAndRequestSmsPermission(phoneNumber, message)
            } else {
                Toast.makeText(this, "Please enter phone number and message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getPhoneNumber() {
        val subscriptionManager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkAndRequestTelephonePermission();
        } else {
//            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList;

            if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                for (subscriptionInfo in activeSubscriptionInfoList) {
//                    val defaultSubscriptionId = SmsManager.getDefaultSmsSubscriptionId()
//                    var defaultSubscriptionInfo: SubscriptionInfo? = subscriptionManager.getActiveSubscriptionInfo(defaultSubscriptionId)
//                    Log.i("", "getPhoneNumber: ${defaultSubscriptionInfo?.displayName}")
                    val phoneNumber = subscriptionInfo.number
//                    val phoneNumber = SubscriptionManager.getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID);
                    // Use the phone number
                    Log.i("MainActivity", "Phone Number: $phoneNumber")
                }
            } else {
                // Handle the case where there are no active subscriptions
                Log.i("MainActivity", "No active subscriptions")
            }
        }
    }

    private fun checkAndRequestTelephonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_SEND_SMS
            )
        }
    }

    private fun checkAndRequestSmsPermission(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_SEND_SMS
            )
        } else {
            sendSms(phoneNumber, message)
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager =
                getSystemService(SmsManager::class.java)


            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val phoneNumber = phoneNumberEditText.text.toString()
                val message = messageEditText.text.toString()
                sendSms(phoneNumber, message)
            } else {
                Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getPhoneNumber()
            } else {
                // Permission denied
            }
        }
    }
}