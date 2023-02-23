package com.example.havadurumu
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.enes_spinner.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class MainActivity : AppCompatActivity(), OnItemSelectedListener, LocationListener {

    var latitude: String?=null
    var longitude: String?=null

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    lateinit var hourAdapter :HourListAdapter
    lateinit var dayAdapter :DayListAdapter
    var tvSehir:TextView?=null

    var hourList :ArrayList<Hourly> = arrayListOf()
    var dayList :ArrayList<Daily> = arrayListOf()
    var daysText = arrayOf("Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi","Pazar")
    var sehirAdi : String ="---"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var spinnerAdapter =ArrayAdapter.createFromResource(this, R.array.cities,R.layout.enes_spinner)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setOnItemSelectedListener(this)  // itemi ceçince tıklanma metodları bu sınıfta dedik
        spinner.adapter = spinnerAdapter

        spinner.setSelection(1)
        getLocation()

        hourAdapter = HourListAdapter(hourList)
        rvHour.adapter = hourAdapter
        rvHour.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL,false)

        dayAdapter = DayListAdapter(dayList)
        rvDay.adapter = dayAdapter
        rvDay.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL,false)


    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }



    private fun retrieveData(city: String, lon: String?, lat : String?) {
        var url =""
        if(lon != null && lat != null){
            url="https://api.weatherapi.com/v1/forecast.json?key=c26bfc9919e246bfb8955855231302&q=${lat+","+lon}&days=7&lang=tr"
            // url= "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&lang=tr&units=metric&appid=e6cafd1c94f7cebf34fe3570451e3ff2"
        }else{
            url="https://api.weatherapi.com/v1/forecast.json?key=c26bfc9919e246bfb8955855231302&q=${city}&days=7&lang=tr"
            // url= "https://api.openweathermap.org/data/2.5/weather?q=${city}&lang=tr&units=metric&appid=e6cafd1c94f7cebf34fe3570451e3ff2"
        }


        val havaDurumuObjeRequest = JsonObjectRequest( com.android.volley.Request.Method.GET,url,null,
            object : Response.Listener<JSONObject>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(response: JSONObject?) {

                    var current = response?.getJSONObject("current")
                    var temperature =current?.getString("temp_c")
                    var isDay = current?.getString("is_day")
                    var icon = current?.getJSONObject("condition")?.getString("icon")
                    var description = current?.getJSONObject("condition")?.getString("text")
                    sehirAdi = response?.getJSONObject("location")?.getString("name").toString()
                    tvSehir!!.setText(sehirAdi)

                    var hourForecastList = response?.getJSONObject("forecast")?.getJSONArray("forecastday")?.getJSONObject(0)?.getJSONArray("hour")

                    hourList.clear()
                    dayList.clear()

                    var currentHour = tarihYazdir()!!.split(",")[1]

                    for (i in 0..hourForecastList?.length()!!-1){
                        var time = hourForecastList.getJSONObject(i)?.getString("time").toString().substringAfter(" ")
                        if (time>currentHour){
                            Log.e("",time +" --- "+ currentHour)
                            var temp = hourForecastList.getJSONObject(i)?.getString("temp_c").toString()
                            var icon = hourForecastList.getJSONObject(i)?.getJSONObject("condition")?.getString("icon").toString()
                            hourList.add(Hourly(time ,temp,icon))
                        }
                    }

                    var dayForecastList = response?.getJSONObject("forecast")?.getJSONArray("forecastday")
                    dayForecastList?.remove(0)
                    var currentNumOfDay = daysText.indexOf(tarihYazdir()!!.split(",")[0])
                    Log.e("---", currentNumOfDay.toString())


                    for (i in 0..dayForecastList?.length()!!-1){
                        var dayDetailList : ArrayList<Hourly> = arrayListOf()

                        var currentDayText = daysText.get(((currentNumOfDay+i+1) %7))


                        var max_temp = dayForecastList.getJSONObject(i).getJSONObject("day").getString("maxtemp_c").substringBefore(".")+ "°"
                        var min_temp = dayForecastList.getJSONObject(i).getJSONObject("day").getString("mintemp_c").substringBefore(".")+ "°"
                        var icon = dayForecastList.getJSONObject(i).getJSONObject("day").getJSONObject("condition").getString("icon")
                        var hourList = dayForecastList.getJSONObject(i).getJSONArray("hour")
                        for (j in 0..hourList.length()-1){
                            var d_hour = hourList.getJSONObject(j).getString("time")
                            var d_temp = hourList.getJSONObject(j).getString("temp_c")
                            var d_icon = hourList.getJSONObject(j).getJSONObject("condition").getString("icon")

                            dayDetailList.add(Hourly(d_hour,d_temp,d_icon))
                        }

                        var weather = Daily(currentDayText,max_temp,min_temp,icon,dayDetailList)
                        dayList.add(weather)

                    }


                    var arkaPlan = resources.getIdentifier((if (isDay=="1" || sehirAdi=="ıgdır")"day" else "night").toString(),"drawable",packageName)
                    myConstraint.setBackgroundResource(arkaPlan)


                    if (icon !== null) {
                        Glide.with(this@MainActivity).load("https:"+icon).into(ivIcon)
                    } else {
                        ivIcon.setImageResource(R.drawable.ic_launcher_background)
                    }

                    tvTemp.setText(temperature.toString().split(".").get(0))
                    tvDate.setText(tarihYazdir()!!.split(",")[0])
                    tvCondition.setText(description)



                    setTextColors(isDay)



                    hourAdapter.notifyDataSetChanged()
                    dayAdapter.notifyDataSetChanged()

                }},
            object :Response.ErrorListener{
                override fun onErrorResponse(error: VolleyError?) {

                }
            }
        )


        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumuObjeRequest)
    }



    private fun setTextColors(isDay: String?) {
        if(isDay=="1"){
            tvTemp.setTextColor(Color.parseColor("#FF000000"))
            tvCondition.setTextColor(Color.parseColor("#FF000000"))
            tvSehir?.setTextColor(resources.getColor(R.color.black))
            spinner.background.setColorFilter(resources.getColor(R.color.black),PorterDuff.Mode.SRC_ATOP)
            tvDate.setTextColor(Color.parseColor("#FF000000"))
            celsiusIcon.setTextColor(Color.parseColor("#FF000000"))
        }else{
            tvTemp.setTextColor(Color.parseColor("#FFFFFFFF"))
            tvCondition.setTextColor(Color.parseColor("#FFFFFFFF"))
            tvSehir?.setTextColor(resources.getColor(R.color.white))
            spinner.background.setColorFilter(resources.getColor(R.color.white),PorterDuff.Mode.SRC_ATOP)
            tvDate.setTextColor(Color.parseColor("#FFFFFFFF"))
            celsiusIcon.setTextColor(Color.parseColor("#FFFFFFFF"))
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun tarihYazdir(): String? {
        var takvim = Calendar.getInstance()
        var formatlayici = SimpleDateFormat("EEEE,kk", Locale( "tr"))
        var tarih = formatlayici.format(takvim.time)
        return tarih
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
        tvSehir = view as TextView

        if(position==0){
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)==false){
                Toast.makeText(this, "GPS Kapalı", Toast.LENGTH_SHORT).show()
            }else{
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),2)

                    retrieveData("null",longitude,latitude)

                }else{
                    retrieveData("igdir",null,null)

                }
            }



        }else{
            var secilenSehir = parent?.getItemAtPosition(position).toString()
            retrieveData(secilenSehir,null,null)
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//
//        if(requestCode == 60){
//            if(grantResults.size >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                spinner.setSelection(1)
//                Toast.makeText(this, "İzin ver len", Toast.LENGTH_SHORT).show()
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }


    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

    }

    override fun onLocationChanged(location: Location) {
        longitude = String.format("%.6f",location.longitude)
        latitude = String.format("%.6f",location.latitude)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

