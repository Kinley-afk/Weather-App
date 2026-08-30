package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherRecordAdapter(
    private var records: List<WeatherRecord>,
    private val onItemClick: (WeatherRecord) -> Unit
) : RecyclerView.Adapter<WeatherRecordAdapter.RecordViewHolder>() {

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationName: TextView = itemView.findViewById(R.id.itemLocationName)
        val tempCondition: TextView = itemView.findViewById(R.id.itemTempCondition)
        val humidityWind: TextView = itemView.findViewById(R.id.itemHumidityWind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.locationName.text = record.locationName
        holder.tempCondition.text = "${record.temperature.toInt()}°C · ${record.condition.replaceFirstChar { it.uppercase() }}"
        holder.humidityWind.text = "Humidity: ${record.humidity}% · Wind: ${record.windSpeed} km/h"

        holder.itemView.setOnClickListener {
            onItemClick(record)
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<WeatherRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}