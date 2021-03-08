package com.nguyen.maps3

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.nguyen.maps3.MainActivity.Companion.EXTRA_MAP_TITLE
import com.nguyen.maps3.MainActivity.Companion.EXTRA_USER_MAP

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val TAG = "CreateMapActivity"
    }

    private lateinit var mMap: GoogleMap
    private var markers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_map)

        supportActionBar?.title = intent.getStringExtra(EXTRA_MAP_TITLE)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // hint to help user what to do when he/she gets to this screen
        mapFragment.view?.let {
            val white = ContextCompat.getColor(this, android.R.color.white)
            Snackbar.make(it, "Long press to add a marker", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", {})
                .setActionTextColor(white)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mi_save) {
            // action to take when menu "Save" is clicked
            Log.i(TAG, "Tapped on save!")
            if (markers.isEmpty()) {
                Toast.makeText(this, "There must be at least one marker on the map", Toast.LENGTH_LONG).show()
            } else {
                // convert map markers newly created by user into a list of Place's
                val places = markers.map { Place(it.title, it.snippet, it.position.latitude, it.position.longitude) }
                // save list of Places into intent and return it to the parent Activity
                val userMap = UserMap(intent.getStringExtra(EXTRA_MAP_TITLE)!!, places)
                val data = Intent()
                data.putExtra(EXTRA_USER_MAP, userMap)
                setResult(RESULT_OK, data)
                finish()
            }
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // short click on map to remove an existing marker
        mMap.setOnInfoWindowClickListener {
            Log.i(TAG, "setOnInfoWindowClickListener- delete this marker")
            markers.remove(it)
            it.remove()
        }

        // long click on map to add a new marker
        mMap.setOnMapLongClickListener {
            Log.i(TAG, "setOnMapLongClickListener")
            showAlertDialog(it)
        }
        // Add a marker in Silicon Valley and zoom in the camera
        // zoom levels: 1=world; 5=landmass/continent; 10=city; 15=streets; 20=buildings
        val siliconValley = LatLng(37.4, -122.1)
        val camera = CameraUpdateFactory.newLatLngZoom(siliconValley, 10f)
        mMap.moveCamera(camera)
    }

    // present dialog to create a new marker with title and snippet
    private fun showAlertDialog(latLng: LatLng) {
        val inflater = LayoutInflater.from(this)
        val binding = com.nguyen.maps3.databinding.DialogCreatePlaceBinding.inflate(inflater)
        // val form = inflater.inflate(R.layout.dialog_create_place, null)
        val dialog = AlertDialog
            .Builder(this)
            .setTitle("Create a marker")
            // .setView(form)
            .setView(binding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        // action to create a marker when OK button is pressed
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            if (title.trim().isEmpty() || description.trim().isEmpty()) {
                Toast.makeText(this, "Place must have non-empty title and description", Toast.LENGTH_LONG).show()
            } else {
                val options = MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .snippet(description)
                val marker = mMap.addMarker(options)
                markers.add(marker)
                dialog.dismiss()
            }
        }
    }
}