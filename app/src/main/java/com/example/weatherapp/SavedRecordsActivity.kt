package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.content.Intent

class SavedRecordsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: WeatherRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_records)

        database = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.recyclerViewRecords)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WeatherRecordAdapter(emptyList()) { record ->
            val intent = Intent(this, RecordDetailActivity::class.java)
            intent.putExtra(RecordDetailActivity.EXTRA_RECORD_ID, record.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords() // refresh list in case a record was added/edited/deleted elsewhere
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val records = database.weatherDao().getAllRecords()
            adapter.updateRecords(records)

            if (records.isEmpty()) {
                recyclerView.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE
            }
        }
    }
}