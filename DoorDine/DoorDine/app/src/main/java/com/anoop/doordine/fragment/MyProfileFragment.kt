package com.anoop.doordine.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.anoop.doordine.R

/**
 * A simple [Fragment] subclass.
 */
class MyProfileFragment : Fragment() {

    lateinit var txtName: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView
    lateinit var txtPhone: TextView

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = this.getActivity()!!.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        // Inflate the layout for this fragment
        val view:View =  inflater.inflate(R.layout.fragment_my_profile, container, false)

        txtAddress = view.findViewById(R.id.txtDefaultAddress)
        txtName = view.findViewById(R.id.txtName)
        txtPhone = view.findViewById(R.id.txtPhone)
        txtEmail = view.findViewById(R.id.txtEmail)

        txtName.text = sharedPreferences.getString("name", "American Airlines")
        txtAddress.text = sharedPreferences.getString("address", "American Airlines")
        txtPhone.text = "+91 "+sharedPreferences.getString("mobileNumber", "American Airlines")
        txtEmail.text = sharedPreferences.getString("email", "American Airlines")

        return view
    }

}
