package com.anoop.doordine.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.anoop.doordine.R
import com.anoop.doordine.fragment.*
import kotlinx.android.synthetic.main.drawer_header.view.*


//main activity will have several fragments

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    //views in the drawer:
    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var headerView: View
    lateinit var imgProfilePicture: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        frameLayout = findViewById(R.id.frameLayout)
        navigationView  = findViewById(R.id.navigationView)






        navigationView.getHeaderView(0).txtHeader.text = sharedPreferences.getString("name", "American Airlines")

        setUpToolBar()

        //this is to check if the intent is coming from the favorites page (in order to immediately refresh it)
        if(sharedPreferences.getBoolean("isOpen", false)){
            sharedPreferences.edit().putBoolean("isOpen", false).apply()
            openFavorites()
        }

        else{
            openRestaurants()
        }


        var previousMenuItem: MenuItem? = null




        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //________________________ON CLICK LISTENER FOR DRAWER ITEMS______________________________
        navigationView.setNavigationItemSelectedListener {
            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false;
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it;





            when(it.itemId) {
                R.id.logout -> {
                    // logout:

                        val dialog = AlertDialog.Builder(this@MainActivity)
                        dialog.setTitle("LOGOUT")
                        dialog.setMessage("Are you sure you would like to log out?")
                        dialog.setPositiveButton("YES") { text, listener ->
                            //use an implicit intent: (open things that are in the phone but outside the APP)
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            sharedPreferences.edit().clear().apply() //clear all shared preferences before logging out...
                            startActivity(intent)
                            Toast.makeText(
                                this@MainActivity,
                                "Logout successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish() //back button will not lead to this dead activity.
                        }
                        dialog.setNegativeButton("No") { text, listener ->
                            //do nothing
                        }
                        dialog.create()
                        dialog.show()

                }
                R.id.myProfile->{
                    supportFragmentManager.beginTransaction().
                    replace(
                        R.id.frameLayout,
                        MyProfileFragment()
                    ).
                    commit()
                    supportActionBar?.title = "About Me"
                    drawerLayout.closeDrawers()
                }
                R.id.home->{
                    openRestaurants()
                    drawerLayout.closeDrawers()

                }
                R.id.aboutApp->{
                    supportFragmentManager.beginTransaction().
                    replace(
                        R.id.frameLayout,
                        AboutAppFragment()
                    ).
                    commit()
                    supportActionBar?.title = "Frequently Asked Questions"
                    drawerLayout.closeDrawers()

                }
                R.id.favRestaurants->{
                    supportFragmentManager.beginTransaction().
                    replace(
                        R.id.frameLayout,
                        FavoritesFragment()
                    ).
                    commit()
                    supportActionBar?.title = "Favorite Restaurants"
                    drawerLayout.closeDrawers()
                }

                R.id.orderHistory->{
                    supportFragmentManager.beginTransaction().
                    replace(
                        R.id.frameLayout,
                        OrderHistoryFragment()
                    ).
                    commit()
                    supportActionBar?.title = "Order History"
                    drawerLayout.closeDrawers()
                }

            }

            return@setNavigationItemSelectedListener true


        }

        //________________________ON CLICK LISTENER FOR DRAWER ITEMS______________________________
        headerView = navigationView.getHeaderView(0)
        imgProfilePicture = headerView.findViewById(R.id.imgProfilePic)
        imgProfilePicture.setOnClickListener {
            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false;
            }


            supportFragmentManager.beginTransaction().
            replace(
                R.id.frameLayout,
                MyProfileFragment()
            ).
            commit()
            supportActionBar?.title = "About Me"
            navigationView.setCheckedItem(R.id.myProfile)
            drawerLayout.closeDrawers()
        }


    }










    //_____________functions________________________________________________
    fun setUpToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Welcome"
        //setting up hamburger button:

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    //click listener to the HB icon
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home)
        {
            //when it is clicked, the drawer comes out!
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    fun openRestaurants(){
        supportFragmentManager.beginTransaction().
            //replace the current fragment to new one.
        replace(
            R.id.frameLayout,
            AllRestaurantsFragment()
        ).
        commit()
        supportActionBar?.title = "Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    fun openFavorites(){
        supportFragmentManager.beginTransaction().
            //replace the current fragment to new one.
        replace(
            R.id.frameLayout,
            FavoritesFragment()
        ).
        commit()
        supportActionBar?.title = "Favorites"
        navigationView.setCheckedItem(R.id.favRestaurants)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frameLayout)
        when(frag){
            !is AllRestaurantsFragment -> openRestaurants()
            else -> super.onBackPressed()
        }


    }
    //_____________functions________________________________________________
}
