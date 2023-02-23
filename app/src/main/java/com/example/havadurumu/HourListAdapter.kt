package com.example.havadurumu

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.card_day.view.*
import kotlinx.android.synthetic.main.card_hour.view.*

class HourListAdapter(weatherList: ArrayList<Hourly>) : RecyclerView.Adapter<HourListAdapter.HourViewHolder>() {

    var weatherList = weatherList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var rowCard = inflater.inflate(R.layout.card_hour,parent,false)
        return HourViewHolder(rowCard)


    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        var currentWeather = weatherList[position]
        holder.setData(position,currentWeather)

    }

    fun notifyChanges(){
        notifyDataSetChanged()
    }




    inner class HourViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var rowCard = view
        var icon = rowCard.iv_icon_hour
        var temp = rowCard.tvtemp_hour
        var hour = rowCard.tvhour_hour

        fun setData(position :Int, currentWeather: Hourly){
            temp.text =currentWeather.temp.substringBefore(".")+ "°"
            hour.text = currentWeather.hour.substringAfter(" ")
            if (currentWeather.icon != "null") {
                Glide.with(rowCard.context).load("https:"+currentWeather.icon).into(icon)
            } else {
                icon.setImageResource(R.drawable.ic_launcher_background)
            }

            if (weatherList.size>22){
                rowCard.setBackgroundColor(Color.TRANSPARENT)
                temp.setTextSize(16F)
                icon.layoutParams.height =110
            }

        }

    }


}