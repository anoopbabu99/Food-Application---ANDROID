package com.anoop.doordine.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.anoop.doordine.R


class OrderPlacedActivity : AppCompatActivity() {
    lateinit var btnPlaceOrder: Button
    lateinit var orderSuccessfullyPlaced: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)
        orderSuccessfullyPlaced=findViewById(R.id.orderSuccessfullyPlaced)
        btnPlaceOrder=findViewById(R.id.btnOkay)

        btnPlaceOrder.setOnClickListener(View.OnClickListener {

            val intent= Intent(this@OrderPlacedActivity,
                MainActivity::class.java)

            startActivity(intent)

            finishAffinity()//finish all the activities
        })
    }

    override fun onBackPressed() {
        //force user to press okay button to take him to dashboard screen
        //user can't go back
    }

}