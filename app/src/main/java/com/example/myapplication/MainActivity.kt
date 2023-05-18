package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val recycler get() = findViewById<RecyclerView>(R.id.recycler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CustomAdapter()
        }

        val rand = java.util.Random()
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                delay(2000)
                flowData.value = "${rand.nextFloat()}"
            }
        }
    }

    companion object {
        val flowData = MutableStateFlow<String?>(null)
    }
}

class CustomAdapter: RecyclerView.Adapter<CustomAdapter.VH>() {

    private val jobList = mutableMapOf<String, Job>()

    class VH(view: View, private val scope: CoroutineScope, private val jobs: MutableMap<String, Job>) : ViewHolder(view) {

        fun bind(position: Int) {
            (itemView as TextView).apply {
                val key = hashCode().toString()
                jobs[key]?.cancel()
                jobs[key] = scope.launch {
                    MainActivity.flowData .collect { data -> if (data != null) text = "$position $data" }
                }
                Log.d(CustomAdapter::class.java.name, "jobs count $key ${jobs.size}")
            }
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

        val rand = java.util.Random()

        return VH(TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
            val value = rand.nextFloat()
            setBackgroundColor(rgb(value, value, value))
        }, scope, jobList)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }


}

fun rgb(red: Float, green: Float, blue: Float): Int {
    return -0x1000000 or
        ((red * 255.0f + 0.5f).toInt() shl 16) or
        ((green * 255.0f + 0.5f).toInt() shl 8) or (blue * 255.0f + 0.5f).toInt()
}
