package com.example.havadurumu

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_day.view.*

class DayListAdapter (dayList: ArrayList<Daily>) : RecyclerView.Adapter<DayListAdapter.DayViewHolder>()  {

    var dayList = dayList
    var mExpandedPosition =-1
    var previousExpandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var rowCard = inflater.inflate(R.layout.card_day,parent,false)

        return DayViewHolder(rowCard,parent)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
         var currentWeather = dayList[position]
        holder.setData(position,currentWeather)
    }

    override fun getItemCount(): Int {
        return dayList.size
    }




    inner class DayViewHolder(view: View, parent: ViewGroup) : RecyclerView.ViewHolder(view) {
        var rowCard = view
        var icon = rowCard.ivicon_day
        var day = rowCard.tvday_time
        var max_temp= rowCard.tvmax_temp
        var min_temp = rowCard.tvmin_temp
        var details = rowCard.rvDetails


        fun setData(position :Int, currentWeather: Daily){
            day.text  =currentWeather.day
            max_temp.text = currentWeather.max_temp
            min_temp.text = currentWeather.min_temp
            details.layoutManager = LinearLayoutManager(rowCard.context, LinearLayoutManager.HORIZONTAL,false)
            details.adapter = HourListAdapter(currentWeather.forecasts)


            var isExpanded :Boolean = (position ==mExpandedPosition)
            details.visibility = if(isExpanded) View.VISIBLE else View.GONE


            if (isExpanded)
                previousExpandedPosition = position;

            if (currentWeather.icon != "null") {
                Glide.with(rowCard.context).load("https:"+currentWeather.icon).into(icon)
            } else {
                icon.setImageResource(R.drawable.ic_launcher_background)
            }

            rowCard.setOnClickListener { view->
                mExpandedPosition = if (isExpanded) -1 else position
                notifyItemChanged(position)
                notifyItemChanged(previousExpandedPosition)
            }
        }

    }

}