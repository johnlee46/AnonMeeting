package com.comp3617.anonmeeting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager : LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps : Location? = null
    private var locationNetwork : Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var marker: Marker
    var latitude = 100.00
    var longitude = 10.00
    var lat2= 100.00
    var long2 = 10.00
    private var count = 0;
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //mMap.setMyLocationEnabled(true);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //getCurLoc()

        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        getLocation()
        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //var current = LatLng(longitude,latitude)
        //mMap.addMarker(MarkerOptions().position(current).title("Test marker"))
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(current))


    }

    fun onClick(v : View){
        getLocation()
        //var current = LatLng(longitude,latitude)
        //mMap.addMarker(MarkerOptions().position(current).title("from onclick Test marker"))
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(current))

    }

    fun saveLoc(v : View){
        val retIntent = Intent()
        retIntent.putExtra("Lat",lat2)
        retIntent.putExtra("Long",long2)
        setResult(Activity.RESULT_OK,retIntent)
        finish()
    }

    override fun onMapLongClick(point: LatLng){
            //mMap.clear()
        if(count == 0) {
            count++
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        } else {
            marker.position = point

        }
        lat2 = marker.position.latitude
        long2 = marker.position.longitude


    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(hasGps || hasNetwork){
            if(hasGps) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    50F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationGps = location
                            }
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderEnabled(provider: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderDisabled(provider: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                    })

                }

            if(hasNetwork) {
                Log.d("CodeAndroidLocation", "hasNetwork")
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    50F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationNetwork = location
                            }
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderEnabled(provider: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderDisabled(provider: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                    })

            }
            //Log.d("CodeAndroidLocation", locationGps!!.longitude.toString())
            //Log.d("CodeAndroidLocation", locationGps!!.latitude.toString())
            val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(localGpsLocation != null){
                locationGps = localGpsLocation
                Log.d("CodeAndroidLocation", "" + locationGps!!.longitude)
                Log.d("CodeAndroidLocation", "" + locationGps!!.latitude)
                var current = LatLng(locationGps!!.latitude,locationGps!!.longitude)
                mMap.addMarker(MarkerOptions().position(current).title("from onclick Test marker")  .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                val zoomLevel = 16.0f //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, zoomLevel))
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(current,zoomLevel))
                //longitude = locationGps!!.longitude
                //latitude = locationGps!!.latitude
            }
            /*
            val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if(localGpsLocation != null){
                locationNetwork = localNetworkLocation
                Log.d("CodeAndroidLocation", locationNetwork!!.longitude.toString())
                Log.d("CodeAndroidLocation", locationNetwork!!.latitude.toString())
                longitude = locationNetwork!!.longitude
                latitude = locationNetwork!!.latitude
            } */
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurLoc(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location ->
                //latitude =  location.latitude
                //longitude = location.longitude
            }
    }

}
