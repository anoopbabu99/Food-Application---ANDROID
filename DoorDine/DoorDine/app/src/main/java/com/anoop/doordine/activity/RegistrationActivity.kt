package com.anoop.doordine.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
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

class RegistrationActivity : AppCompatActivity() {

    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var btnCreate: Button
    lateinit var etName: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var etEmail: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var etAddress: EditText

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        setContentView(R.layout.activity_registration)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "REGISTER YOURSELF"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        btnCreate = findViewById(R.id.btnReset)
        etName = findViewById(R.id.etName)
        etMobileNumber = findViewById(R.id.etPhoneNo)
        etPassword = findViewById(R.id.etPassword)
        etEmail = findViewById(R.id.etEmailAddress)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etAddress = findViewById(R.id.etAddress)

        btnCreate.setOnClickListener {
            val mobileNumber = etMobileNumber.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val name = etName.text.toString()
            val email= etEmail.text.toString()
            val address = etAddress.text.toString()
            var count = 0;

            //checking conditions while registration
            if(confirmPassword!=password){
                etConfirmPassword.setError("Passwords Mismatched")
               count++
            }
            if(mobileNumber.length!=10){
                etMobileNumber.setError("Invalid phone number")
                count++
            }
            if(name.length<4){
                etName.setError("Name too short")
                count++
            }
            if(!email.contains('@')){
                etEmail.setError("Email must contain @ symbol")
                count++
            }
            if(email.length<5){
                etEmail.setError("Email length too short")
                count++
            }
            if(password.length<6){
                etPassword.setError("Weak password, enter 5 or more symbols")
                count++
            }
            if(address.length<10){
                etAddress.setError("Enter valid address")
                count++
            }

            if(count!=0){
                return@setOnClickListener
            }





            //_______________________INTERNET_____________________________________________________
            // create POST request:
            val queue = Volley.newRequestQueue(this@RegistrationActivity)
            val url = "http://13.235.250.119/v2/register/fetch_result"
            //here we need to details typed to JSON
            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", password)
            jsonParams.put("name", name)
            jsonParams.put("email", email)
            jsonParams.put("address", address)


            if (ConnectionManager().checkConnectivity(this@RegistrationActivity)) {
                System.out.println("I am here connected to net")
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
                                System.out.println("success")
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
                                val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                                //successfully dumped data
                            }
                            else {
                                //display error message
                                Toast.makeText(
                                    this@RegistrationActivity,
                                    "Email or phone number is already in use.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            //display error message

                            Toast.makeText(
                                this@RegistrationActivity,
                                "Some error occurred while parsing JSON file received 2",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

                    Response.ErrorListener
                    {
                        //display error message
                        Toast.makeText(
                            this@RegistrationActivity,
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
                val dialog = AlertDialog.Builder(this@RegistrationActivity)
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
                    val dialog2 = AlertDialog.Builder(this@RegistrationActivity)
                    dialog2.setTitle("EXIT")
                    dialog2.setMessage("Are you sure you want to exit?")
                    dialog2.setPositiveButton("Yes"){text,listener->
                        ActivityCompat.finishAffinity(this@RegistrationActivity)
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

    fun internet(){

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
