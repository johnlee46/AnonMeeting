package com.comp3617.anonmeeting

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView

import com.google.firebase.FirebaseApp



import kotlinx.android.synthetic.main.activity_main.*

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity() {
    private lateinit var db : FirebaseFirestore;
    private lateinit var locationManager : LocationManager
    var userList  = arrayListOf("Users In Session")
    //private lateinit var userArray : Array<String>
    var distance = "BLANK"
    var long = -123.1153
    var lat = 49.2833
    var curlong = -123.1153
    var curlat = 49.2833
    val REQUEST_LOCATION = 100
    var docData = HashMap<String, Any?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        db = FirebaseFirestore.getInstance()
        //sessionCreate.setVisibility(View.GONE)
        //listView1.setVisibility(View.GONE)
        val lvTeams = findViewById<ListView>(R.id.listView1)
        val adapter = ListAdapter(this, userList)

        lvTeams.setAdapter(adapter)
        //adapter.notifyDataSetChanged()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            curlat = location!!.latitude
            curlong = location!!.longitude
            LongCur.text = String.format("%.2f",curlong)
            LatCur.text = String.format("%.2f",curlat)
            updateList()
            //tvLocation.text = "${location?.latitude}, ${location?.longitude}"
            //val task = GeoCoderTask().execute(location!!)
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

    }

    fun onClickCreateSession(v: View){
        val basicIntent = Intent(this, MapsActivity::class.java)
        startActivityForResult(basicIntent, 100)
        docData = HashMap<String, Any?>()
        docData["Latitude"] = long
        docData["Longitude"] = lat

        db.collection("data").document(sessionID.text.toString())
            .set(docData)
            .addOnSuccessListener { Log.d("logtag", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }
        runOnUiThread {
            onClickJoinSession(v)
            populateTasks()

        }
    }
    fun onClickJoinSession2(v : View){
        val basicIntent = Intent(this, JoinSession::class.java)
        startActivityForResult(basicIntent, 150)
    }
    fun onClickJoinSession(v: View) {

    //runOnUiThread {
        userList.clear()
        val docRef = db.collection("data").document(sessionID.text.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var tempLat = document.get("Latitude")
                    var tempLong = document.get("Longitude")
                    Log.d("logtag", tempLong.toString());
                    Log.d("logtag", tempLat.toString());
                } else {
                    Log.d("logtag", "failed");
                }
            }
            .addOnFailureListener { exception ->
                Log.d("logtag", "get failed with ", exception)
            }

        LatTarget.text = String.format("%.2f", lat)
        LongTarget.text = String.format("%.2f", long)
        val url =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$curlat,$curlong&destinations=$lat%2C$long&departure_time=now&key=AIzaSyC4tEZtUYTwK2XWTpzcfIjJKmLZigU5Ehg"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    Log.d("logtag", "failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("logtag", "succeed")
                    if (response.isSuccessful) {
                        //Toast.makeText(applicationContext,response.body()?.string(),Toast.LENGTH_SHORT).show()
                        //tvJSON.text = response.body()?.string()
                        val json = response.body()?.string()

                        //httpResponse is the output of google api
                        val jsonRespRouteDistance = JSONObject(json)
                            .getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray("elements")
                            .getJSONObject(0)
                            .getJSONObject("duration_in_traffic")

                        distance = jsonRespRouteDistance.get("text").toString()

                        Log.d("distanceTag",distance)
                        docData = HashMap<String, Any?>()
                        var nestedData = nickname.text.toString() + "  -- ETA :" + distance
                        //nestedData[nickname.text.toString()] = nickname.text.toString() +"  -- ETA :" + 50

                        docData["UserList"] = nestedData

                        db.collection("data").document(sessionID.text.toString()).collection("Users").document(nickname.text.toString())
                            .set(docData, SetOptions.merge())
                            .addOnSuccessListener { Log.d("logtag", "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }
                        db.collection("data").document(sessionID.text.toString()).collection("Users").get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    Log.d("logtag", "${document.id} => ${document.data}")
                                    userList.add(document.data.toString().replace("{", " ").replace("UserList=", " ").replace("}", " "))
                                }
                            }
                            .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }

                        listView1.setVisibility(View.VISIBLE)


                        runOnUiThread {
                            tvJSON.text = distance
                        }

                    }
                }

            })


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item?.itemId == R.id.setMarker){
            val basicIntent = Intent(this, MapsActivity::class.java)
            startActivityForResult(basicIntent, 100)

        }

        if(item?.itemId == R.id.update){
            val url =
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$curlat,$curlong&destinations=$lat%2C$long&departure_time=now&key=AIzaSyC4tEZtUYTwK2XWTpzcfIjJKmLZigU5Ehg"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        Log.d("logtag", "failed")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d("logtag", "succeed")
                        if (response.isSuccessful) {
                            //Toast.makeText(applicationContext,response.body()?.string(),Toast.LENGTH_SHORT).show()
                            //tvJSON.text = response.body()?.string()
                            val json = response.body()?.string()

                            //httpResponse is the output of google api
                            val jsonRespRouteDistance = JSONObject(json)
                                .getJSONArray("rows")
                                .getJSONObject(0)
                                .getJSONArray("elements")
                                .getJSONObject(0)
                                .getJSONObject("duration_in_traffic")

                            distance = jsonRespRouteDistance.get("text").toString()

                            Log.d("distanceTag",distance)
                            docData = HashMap<String, Any?>()
                            var nestedData = nickname.text.toString() + "  -- ETA :" + distance
                            //nestedData[nickname.text.toString()] = nickname.text.toString() +"  -- ETA :" + 50

                            docData["UserList"] = nestedData

                            db.collection("data").document(sessionID.text.toString()).collection("Users").document(nickname.text.toString())
                                .set(docData, SetOptions.merge())
                                .addOnSuccessListener { Log.d("logtag", "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }
                            db.collection("data").document(sessionID.text.toString()).collection("Users").get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        Log.d("logtag", "${document.id} => ${document.data}")
                                        userList.add(document.data.toString().replace("{", " ").replace("UserList=", " ").replace("}", " "))
                                    }
                                }
                                .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }

                            listView1.setVisibility(View.VISIBLE)


                            runOnUiThread {
                                tvJSON.text = distance
                            }

                        }
                    }

                })

            db.collection("data").document(sessionID.text.toString()).collection("Users").document(nickname.text.toString())
                .update("UserList", nickname.text.toString() + "  -- ETA :" + distance)


            populateTasks()

        }

        if(item?.itemId == R.id.jsonData){
           // Thread(Runnable{
            val url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=49.2833,-123.1153&destinations=49.2833%2C-123.1100&departure_time=now&key=AIzaSyC4tEZtUYTwK2XWTpzcfIjJKmLZigU5Ehg"
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        Log.d("logtag","failed")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d("logtag","succeed")
                        if (response.isSuccessful) {
                            //Toast.makeText(applicationContext,response.body()?.string(),Toast.LENGTH_SHORT).show()
                            runOnUiThread {
                                //tvJSON.text = response.body()?.string()
                                val json = response.body()?.string()

                                //httpResponse is the output of google api
                                val jsonRespRouteDistance = JSONObject(json)
                                    .getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray("elements")
                                    .getJSONObject(0)
                                    .getJSONObject("duration_in_traffic")

                                val distance = jsonRespRouteDistance.get("text").toString()

                                val destination_addr = JSONObject(json)
                                    .get("destination_addresses")
                                    .toString()


                                tvJSON.text = distance
                            }
                        }
                    }

                })

           // })



        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }



    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Start Listening for Updates
            startListeningForLocationUpdates()
        }
        else {
            //I don't have the permission, so request user for the permissions
            requestLocationPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private fun startListeningForLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 50f, locationListener);
    }


    private fun requestLocationPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
        } else {

            // GPS permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode:Int,data:Intent?){
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            long = data!!.getDoubleExtra("Long", -123.1153)
            lat = data!!.getDoubleExtra("Lat", 49.2833)
            LatTarget.text =  String.format("%.2f",lat)
            LongTarget.text =  String.format("%.2f",long)
            sessionCreate.setVisibility(View.VISIBLE)
        }


        if(requestCode == 150 && resultCode == Activity.RESULT_OK){
            var sessionIDfromIntent = data!!.getStringExtra("sessionID")
            var namefromIntent = data!!.getStringExtra("Name")

            runOnUiThread {
                userList.clear()
                val docRef = db.collection("data").document(sessionIDfromIntent)
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            var tempLat = document.get("Latitude")
                            var tempLong = document.get("Longitude")
                            Log.d("logtag", tempLong.toString());
                            Log.d("logtag", tempLat.toString());
                        } else {
                            Log.d("logtag", "failed");
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("logtag", "get failed with ", exception)
                    }


                docData = HashMap<String, Any?>()
                val nestedData =  namefromIntent + "  -- ETA :" + 50
                //nestedData[nickname.text.toString()] = nickname.text.toString() +"  -- ETA :" + 50

                docData["UserList"] = nestedData

                db.collection("data").document(sessionIDfromIntent).collection("Users").document(namefromIntent)
                    .set(docData, SetOptions.merge())
                    .addOnSuccessListener { Log.d("logtag", "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }


                db.collection("data").document(sessionIDfromIntent).collection("Users").get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            Log.d("logtag", "${document.id} => ${document.data}")
                            userList.add(document.data.toString().replace("{", " ").replace("UserList=", " ").replace("}", " "))
                        }
                    }
                    .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }

                listView1.setVisibility(View.VISIBLE)
            }
            //populateTasks(userList)
            //adapter.notifyDataSetChanged()

        }

    }


    private fun populateTasks(){
            userList.clear()
            db.collection("data").document(sessionID.text.toString()).collection("Users").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d("logtag", "${document.id} => ${document.data}")
                        userList.add(document.data.toString().replace("{", " ").replace("UserList=", " ").replace("}", " "))
                    }
                }
                .addOnFailureListener { e -> Log.w("logtag", "Error writing document", e) }


    }
    private fun getETA(CurrentLat:String,CurrentLong:String,TargetLat:String,TargetLong:String) : String {
        val url =
            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$CurrentLat,$CurrentLong&destinations=$TargetLat%2C$TargetLong&departure_time=now&key=AIzaSyC4tEZtUYTwK2XWTpzcfIjJKmLZigU5Ehg"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    Log.d("logtag", "failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("logtag", "succeed")
                    if (response.isSuccessful) {
                        //Toast.makeText(applicationContext,response.body()?.string(),Toast.LENGTH_SHORT).show()
                            //tvJSON.text = response.body()?.string()
                            val json = response.body()?.string()

                            //httpResponse is the output of google api
                            val jsonRespRouteDistance = JSONObject(json)
                                .getJSONArray("rows")
                                .getJSONObject(0)
                                .getJSONArray("elements")
                                .getJSONObject(0)
                                .getJSONObject("duration_in_traffic")

                            distance = jsonRespRouteDistance.get("text").toString()

                        runOnUiThread{
                            tvJSON.text = distance
                        }



                    }
                }

            })
        Log.d("logtag2",distance)
        return distance

    }

        fun updateList(){
            //userList.clear()
            val url =
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$curlat,$curlong&destinations=$lat%2C$long&departure_time=now&key=AIzaSyC4tEZtUYTwK2XWTpzcfIjJKmLZigU5Ehg"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        Log.d("logtag", "failed")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d("logtag", "succeed")
                        if (response.isSuccessful) {
                            //Toast.makeText(applicationContext,response.body()?.string(),Toast.LENGTH_SHORT).show()
                            //tvJSON.text = response.body()?.string()
                            val json = response.body()?.string()

                            //httpResponse is the output of google api
                            val jsonRespRouteDistance = JSONObject(json)
                                .getJSONArray("rows")
                                .getJSONObject(0)
                                .getJSONArray("elements")
                                .getJSONObject(0)
                                .getJSONObject("duration_in_traffic")

                            distance = jsonRespRouteDistance.get("text").toString()

                            Log.d("distanceTag",distance)
                            runOnUiThread {
                                tvJSON.text = distance
                            }

                        }
                    }

                })

            db.collection("data").document(sessionID.text.toString()).collection("Users").document(nickname.text.toString())
                .update("UserList", nickname.text.toString() + "  -- ETA :" + distance)


            //populateTasks()


        }






}
