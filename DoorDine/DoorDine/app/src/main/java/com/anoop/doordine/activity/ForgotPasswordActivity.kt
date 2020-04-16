package com.anoop.doordine.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anoop.doordine.R
import com.anoop.doordine.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    lateinit var etPhoneNumber: EditText
    lateinit var etEmail: EditText
    lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FORGOT PASSWORD"


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        etEmail = findViewById(R.id.etEmailAddress)
        etPhoneNumber = findViewById(R.id.etPhoneNo)
        btnNext = findViewById(R.id.btnReset)

        var count = 0

        btnNext.setOnClickListener(View.OnClickListener {
            if (etPhoneNumber.text.isBlank())
            {
                etPhoneNumber.setError("Mobile Number Missing")
                count++

            }
            else
            {
                if (etEmail.text.isBlank())
                {
                    etEmail.setError("Email Missing")
                    count++

                }
                if(!etEmail.text.contains('@')){
                    etEmail.setError("Email must contain @ symbol")
                    count++
                }
            }
            if(count!=0)
                return@OnClickListener

            //_______________________INTERNET_____________________________________________________
            // create POST request:
            val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
            val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

            val loginUser = JSONObject()

            loginUser.put("mobile_number", etPhoneNumber.text)
            loginUser.put("email", etEmail.text)

            if (ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {
                //("I am here connected to net")
                val jsonObjectRequest = @SuppressLint("ResourceType")
                object : JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    loginUser,
                    Response.Listener
                    {
                        //here response will be written:

                        try {
                            val responseJsonObjectData = it.getJSONObject("data")

                            val success = responseJsonObjectData.getBoolean("success")
                            if (success) {

                                val firstTry=responseJsonObjectData.getBoolean("first_try")

                                // first try is always false for some reason :(
                                // it claims the OTP is already sent even if it is the first try...

                                if(firstTry) {
                                    Toast.makeText(
                                        this@ForgotPasswordActivity,
                                        "OTP sent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else{
                                    Toast.makeText(this@ForgotPasswordActivity, "OTP is already in your inbox", Toast.LENGTH_SHORT).show()
                                }
                                //create intent to reset password activity:

                                val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                                intent.putExtra("mobile", etPhoneNumber.text.toString())
                                startActivity(intent)


                            }
                            else {
                                //display error message
                                Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    "Invalid credentials",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            //display error message

                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Some error occurred while parsing JSON file received",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

                    Response.ErrorListener
                    {
                        //display error message
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Volley error occurred",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                ) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "7b832cd6a75856"
                        return headers
                    }


                }
                queue.add(jsonObjectRequest)
            }
            else{
                //there is no net
                val dialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                dialog.setTitle("ERROR")
                dialog.setMessage("Internet Connection NOT Found")
                dialog.setPositiveButton("Open Settings"){text,listener->
                    //use an implicit intent: (open things that are in the phone but outside the APP)
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit"){text,listener->
                    //do nothing
                    val dialog2 = AlertDialog.Builder(this@ForgotPasswordActivity)
                    dialog2.setTitle("EXIT")
                    dialog2.setMessage("Are you sure you want to exit?")
                    dialog2.setPositiveButton("Yes"){text,listener->
                        ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                    }
                    dialog2.setNegativeButton("No"){text,listener->
                        //do nothing
                    }
                    dialog2.create()
                    dialog2.show()

                }
                dialog.create()
                dialog.show()
            }
            //_______________________INTERNET_____________________________________________________

        })
    }




}
