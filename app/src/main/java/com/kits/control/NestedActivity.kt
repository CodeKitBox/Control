package com.kits.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kits.control.utils.Faker


class NestedActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested)

//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        val adapter = MyAdapter()
//        adapter.dataList = initData()
//
//        recyclerView.adapter = adapter
    }
    private fun initData():MutableList<String>{
        val list = mutableListOf<String>()
        for ( i in 0 .. 200){
            list.add("${Faker.getRandomWord(1,5)} recyclerview  $i")
        }
        return list
    }
}

