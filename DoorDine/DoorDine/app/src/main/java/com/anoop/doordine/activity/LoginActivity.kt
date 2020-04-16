package com.anoop.doordine.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anoop.doordine.R
import com.anoop.doordine.util.ConnectionManager
import org.json.JSONObject

import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    lateinit var txtRegisterYourself: TextView

    lateinit var sharedPreferences: SharedPreferences

    lateinit var progressLayout: RelativeLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        progressLayout= findViewById(R.id.progressLayout)
        progressLayout.visibility = View.INVISIBLE

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        txtRegisterYourself = findViewById(R.id.txtCreateAccount)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)

        txtRegisterYourself.setPaintFlags(txtRegisterYourself.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        txtForgotPassword.setPaintFlags(txtForgotPassword.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        etMobileNumber = findViewById(R.id.etPhoneNo)
        etPassword = findViewById(R.id.etPassword)



        txtRegisterYourself.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        txtForgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }




        btnLogin.setOnClickListener {
            val mobileNumber = etMobileNumber.text.toString()
            val password = etPassword.text.toString()

            if (etMobileNumber.text.isBlank())
            {
                etMobileNumber.setError("Mobile Number Missing")

                return@setOnClickListener
            }
            else
            {
                if (etPassword.text.isBlank())
                {
                    etPassword.setError("Password Missing")

                    return@setOnClickListener
                }
            }

            if(etMobileNumber.length()!=10){
                etMobileNumber.setError("Phone number too short")
                return@setOnClickListener
            }
            if (etPassword.length()<5)
            {
                etPassword.setError("Password too short")

                return@setOnClickListener
            }

            progressLayout.visibility = View.VISIBLE

            //_______________________INTERNET_____________________________________________________
            // create POST request:
            val queue = Volley.newRequestQueue(this@LoginActivity)
            val url = "http://13.235.250.119/v2/login/fetch_result"
            //here we need to send mobile # and password.
            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", password)
            if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                //("I am here connected to net")
                val jsonObjectRequest = @SuppressLint("ResourceType")
                object : JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParams,
                    Response.Listener
                    {
                        //here response will be written:

                        try {

                            val JsonObject = it.getJSONObject("data")
                            val success = JsonObject.getBoolean("success")

                            if (success) {


                                //COLLECT DATA
                                val bookJsonObject = JsonObject.getJSONObject("data")
                                val userId = bookJsonObject.getString("user_id")
                                val name = bookJsonObject.getString("name")
                                val email = bookJsonObject.getString("email")
                                val mobileNumber = bookJsonObject.getString("mobile_number")
                                val address = bookJsonObject.getString("address")

                                //successfully collected data


                                //DUMP DATA INTO SHARED PREFERENCES
                                savePreferences(userId, name, email, mobileNumber, address)
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()

                                //successfully dumped data

                            }
                            else {
                                //display error message
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Sign in failed: Invalid credentials",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressLayout.visibility = View.INVISIBLE
                            }
                        } catch (e: Exception) {
                            //display error message

                            Toast.makeText(
                                this@LoginActivity,
                                "Some error occurred while parsing JSON file received",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressLayout.visibility = View.INVISIBLE
                        }
                    },

                    Response.ErrorListener
                    {
                        //display error message
                        Toast.makeText(
                            this@LoginActivity,
                            "Volley error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressLayout.visibility = View.INVISIBLE

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
                progressLayout.visibility = View.INVISIBLE
                val dialog = AlertDialog.Builder(this@LoginActivity)
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
                    val dialog2 = AlertDialog.Builder(this@LoginActivity)
                    dialog2.setTitle("EXIT")
                    dialog2.setMessage("Are you sure you want to exit?")
                    dialog2.setPositiveButton("Yes"){text,listener->
                        ActivityCompat.finishAffinity(this@LoginActivity)
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

        }
    }

    fun  savePreferences(userId: String, name: String, email: String, mobileNumber: String, address: String){
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("userId", userId).apply()
        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("mobileNumber", mobileNumber).apply()
        sharedPreferences.edit().putString("address", address).apply()
    }
}
