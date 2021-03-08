package com.nguyen.maps3

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nguyen.maps3.databinding.ActivityMainBinding
import com.nguyen.maps3.databinding.DialogCreateMapBinding
import java.io.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
        const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
        const val RC_CREATE_MAP = 1999
        const val FILENAME = "UserMaps.data"
    }

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MapAdapter
    var userMaps = mutableListOf<UserMap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvMaps.layoutManager = LinearLayoutManager(this)

        // userMaps = generateSampleData().toMutableList()
        val mapsFromFile = deserializeUserMaps(this)
        userMaps = mapsFromFile.toMutableList()
        adapter = MapAdapter(userMaps, object: MapAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                Log.i(TAG, "onItemClick $position")
                val intent = Intent(this@MainActivity, DisplayMapActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, userMaps[position])
                startActivity(intent)
                // animate transitioning to DisplayMapActivity
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })
        binding.rvMaps.adapter = adapter

        binding.fabCreateMap.setOnClickListener {
            Log.i(TAG, "Tap on FAB")
            showAlertDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_CREATE_MAP && resultCode == RESULT_OK) {
            // extract userMap returned from CreateMapActivity
            val userMap = data?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            Log.i(TAG, "onActivityResult with new map title ${userMap.title}")
            // add new userMap into existing list of UserMap
            userMaps.add(userMap)
            adapter.notifyItemInserted(userMaps.lastIndex)
            // save list of UserMaps to disk
            serializerUserMaps(this, userMaps)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showAlertDialog() {
        // present dialog to prompt user to enter title for a new map
        val inflater = LayoutInflater.from(this)
        val binding = DialogCreateMapBinding.inflate(inflater)
        val dialog = AlertDialog
            .Builder(this)
            .setTitle("Map title")
            .setView(binding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        // action to create a marker when OK button is pressed
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = binding.etTitle.text.toString()
            if (title.trim().isEmpty()) {
                // title is empty: show Toast and keep showing the dialog
                Toast.makeText(this, "Map must have a non-empty title", Toast.LENGTH_LONG).show()
            } else {
                // title is valid: navigate to CreateMapActivity
                dialog.dismiss()
                val intent = Intent(this@MainActivity, CreateMapActivity::class.java)
                intent.putExtra(EXTRA_MAP_TITLE, title)
                startActivityForResult(intent, RC_CREATE_MAP)
            }
        }
    }

    private fun getDataFile(context: Context): File {
        Log.i(TAG, "getting file from directory ${context.filesDir}")
        return File(context.filesDir, FILENAME)
    }

    private fun serializerUserMaps(context: Context, userMaps: List<UserMap>) {
        Log.i(TAG, "serializerUserMaps")
        val file = getDataFile(context)
        val stream = FileOutputStream(file)
        ObjectOutputStream(stream).use {
            it.writeObject(userMaps)
        }
    }

    private fun deserializeUserMaps(context: Context): List<UserMap> {
        Log.i(TAG, "deserializerUserMaps")
        val file = getDataFile(context)
        if (!file.exists()) {
            Log.i(TAG, "data file does not exist yet")
            return emptyList()
        } else {
            val stream = FileInputStream(file)
            ObjectInputStream(stream).use {
                return it.readObject() as List<UserMap>
            }
        }
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
                UserMap(
                        "Memories from University",
                        listOf(
                                Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                                Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                                Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                        )
                ),
                UserMap("January vacation planning!",
                        listOf(
                                Place("Tokyo", "Overnight layover", 35.67, 139.65),
                                Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                                Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                        )),
                UserMap("Singapore travel itinerary",
                        listOf(
                                Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                                Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                                Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                                Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                        )
                ),
                UserMap("My favorite places in the Midwest",
                        listOf(
                                Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                                Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                                Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                                Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                                Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                        )
                ),
                UserMap("Restaurants to try",
                        listOf(
                                Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                                Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                                Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                                Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                                Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                        )
                )
        )
    }
}