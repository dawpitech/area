package com.uwu.area

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class AuthRedirectActivity : ComponentActivity() {
    private val TAG = "AuthRedirectActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "Trying to authenticate..."
            setPadding(20, 40, 20, 40)
        }
        setContentView(tv)

        val data: Uri? = intent?.data
        if (data == null) {
            Toast.makeText(this, "No callback :(", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val token = data.getQueryParameter("token")
        val code = data.getQueryParameter("code")
        val state = data.getQueryParameter("state")
        val error = data.getQueryParameter("error")

        Log.d(TAG, "Callback URI: $data")
        Log.d(TAG, "token=$token code=$code state=$state error=$error")

        tv.text = buildString {
            append("Got callback\n\n")
            append("URI: $data\n\n")
            if (!token.isNullOrEmpty()) append("token: $token\n")
            if (!code.isNullOrEmpty()) append("code: $code\n")
            if (!state.isNullOrEmpty()) append("state: $state\n")
            if (!error.isNullOrEmpty()) append("error: $error\n")
        }

        if (error == null) {
            Toast.makeText(this, "Auth succeeded", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Auth didn't succeed, good luck o7", Toast.LENGTH_LONG).show()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                token?.let { putExtra("oauth_token", it) }
            }
            startActivity(i)
            finish()
        }, 1200)
    }
}
