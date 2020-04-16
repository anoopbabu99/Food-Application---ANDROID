package com.anoop.doordine.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.anoop.doordine.R


//Splash Screen.
class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 2000 //ms (2 seconds were required to display gif, according to specifications it should have been 1 (sorry))
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            // This method will be executed once the timer is over


            sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            //if user has logged in directly go to the main activity else go to login page:
            if(isLoggedIn){
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)




    }
}
