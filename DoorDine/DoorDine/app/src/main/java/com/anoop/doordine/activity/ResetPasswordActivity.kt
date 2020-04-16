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

class ResetPasswordActivity : AppCompatActivity() {
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    lateinit var etOTP:EditText
    lateinit var etNewPassword:EditText
    lateinit var etConfirmPasswordForgot: EditText
    lateinit var btnSubmit: Button





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "RESET PASSWORD"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        etOTP=findViewById(R.id.etOTP)
        etNewPassword=findViewById(R.id.etNewPassword)
        etConfirmPasswordForgot=findViewById(R.id.etConfirmPassword)

        btnSubmit=findViewById(R.id.btnReset)





        btnSubmit.setOnClickListener(View.OnClickListener {
            var count = 0
            if(etOTP.text.isBlank())
            {
                etOTP.setError("OTP missing")
                count++
            }
            if(etNewPassword.text.isBlank())
            {
                etNewPassword.setError("Password Missing")
                count++
            }
            if(etConfirmPasswordForgot.text.isBlank())
            {
                etConfirmPasswordForgot.setError("Confirm Password Missing")
                count++
            }
            if((etNewPassword.text.toString().toInt()!=etConfirmPasswordForgot.text.toString().toInt()))
            {
                etConfirmPasswordForgot.setError("Password mismatched")
                count++
            }

            if(count!=0)
                return@OnClickListener

            //_______________________INTERNET_____________________________________________________
            // create POST request:
            val queue = Volley.newRequestQueue(this@ResetPasswordActivity)
            val url = "http://13.235.250.119/v2/reset_password/fetch_result"
            val loginUser = JSONObject()

            loginUser.put("mobile_number", intent.getStringExtra("mobile"))
            loginUser.put("password", etNewPassword.text.toString())
            loginUser.put("otp", etOTP.text.toString())




            if (ConnectionManager().checkConnectivity(this@ResetPasswordActivity)) {
                //I am here connected to net
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

                                val serverMessage=responseJsonObjectData.getString("successMessage")

                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    serverMessage,
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                                startActivity(intent)
                            }

                            else {
                                //display error message
                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    "Invalid credentials",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } catch (e: Exception) {
                            //display error message

                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "Some error occurred while parsing JSON file received",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

                    Response.ErrorListener
                    {
                        //display error message
                        Toast.makeText(
                            this@ResetPasswordActivity,
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
                val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
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
                    val dialog2 = AlertDialog.Builder(this@ResetPasswordActivity)
                    dialog2.setTitle("EXIT")
                    dialog2.setMessage("Are you sure you want to exit?")
                    dialog2.setPositiveButton("Yes"){text,listener->
                        ActivityCompat.finishAffinity(this@ResetPasswordActivity)
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
