  package com.example.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL


  class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

   private var lat:String=""
   private var lon:String=""
   private val permission_code:Int= 1
  val API:String="3033ab2f2331f6213c082986390aa122" //api key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        Toast.makeText(this,"Please Wait While Data is Fetched" , Toast.LENGTH_LONG).show()
        getlocation()
    }

    @SuppressLint("MissingPermission")
    private fun getlocation(){
        if(checkpermission()){
            if(islocationenabled()){
              fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                  val location:Location?=task.result
                    if(location==null){
                         getnewlocation()
                    }
                  else{
                      lon=location.longitude.toString()
                        lat=location.latitude.toString()
                        weatherTask().execute()
                    }
              }
            }
            else{
                Toast.makeText(this,"Please Enable Location" , Toast.LENGTH_SHORT).show()
            }
        }
        else{
            requestpermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getnewlocation(){
        locationRequest= LocationRequest()
        locationRequest.priority =LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=0
        locationRequest.fastestInterval=0
        locationRequest.numUpdates=2
       fusedLocationProviderClient!!.requestLocationUpdates(
               locationRequest,locationCallback, Looper.myLooper()
       )
    }
    private val locationCallback= object :LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastlocation: Location = p0.lastLocation
              lat=lastlocation.latitude.toString()
            lon=lastlocation.longitude.toString()
            weatherTask().execute()
        }

    }
    //function to check permission of the location
    private fun checkpermission(): Boolean{
        if(
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }

    //function to request user premission
    private fun requestpermission(){

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),permission_code)
    }

   //function to check location is enabled
    private fun islocationenabled():Boolean{
        val locationManager:LocationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager
       return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
  //function to check permission request is succesfull or not
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
          if(requestCode==permission_code){
              if(grantResults.isNotEmpty()&& grantResults[0]== PackageManager.PERMISSION_GRANTED){
                  Log.d("Debug:", "YOU HAVE THE PERMISSIONS")
              }
          }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
//menu code
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            val intent = Intent(applicationContext, about::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

      inner class weatherTask() : AsyncTask<String, Void, String>(){
          override fun onPreExecute() {

              super.onPreExecute()
          }
          override fun doInBackground(vararg params: String?): String? {
              var response:String?
              try{
                  response = URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$API").readText(
                          Charsets.UTF_8
                  )
              }catch (e: Exception){
                  response = null
              }
              return response
          }

          override fun onPostExecute(result: String?) {
              super.onPostExecute(result)
              try {
                  /* Extracting JSON returns from the API */
                  val jsonObj = JSONObject(result)
                  val main = jsonObj.getJSONObject("main")
                  val sys = jsonObj.getJSONObject("sys")
                  val wind = jsonObj.getJSONObject("wind")
                  val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                  val temp = main.getString("temp")
                  val pressure = main.getString("pressure")
                  val humidity = main.getString("humidity")
                  val windSpeed = wind.getString("speed")
                  val address = jsonObj.getString("name")
                  city_text.text= address.toUpperCase()
                  speed_text.text="WIND SPEED: "+ windSpeed +" km/h"
                  pressure_text.text="PRESSURE: "+pressure +" pascals"
                  temp_text.text= temp
                  humidity_text.text="HUMIDITY: " +humidity +" %"
                  celcius_text.text="°C"
              } catch (e: Exception){}
          }
      }
}
