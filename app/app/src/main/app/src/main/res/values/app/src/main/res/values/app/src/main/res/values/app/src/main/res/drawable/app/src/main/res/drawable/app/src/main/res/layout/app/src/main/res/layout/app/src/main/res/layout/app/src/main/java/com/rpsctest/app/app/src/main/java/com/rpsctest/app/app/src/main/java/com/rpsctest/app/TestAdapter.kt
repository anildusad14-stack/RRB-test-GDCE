package com.rpsctest.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestAdapter(
    private var testList: List<TestItem>,
    private val onOpenClick: (TestItem) -> Unit,
    private val onDeleteClick: (TestItem) -> Unit
) : RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    class TestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTestName: TextView = view.findViewById(R.id.tvTestName)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val btnOpen: Button = view.findViewById(R.id.btnOpen)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override.onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false)
        return TestViewHolder(view)
    }

    override.onBindViewHolder(holder: TestViewHolder, position: Int) {
        val test = testList[position]
        holder.tvTestName.text = test.name
        holder.tvFileName.text = test.fileName
        
        holder.btnOpen.setOnClickListener { onOpenClick(test) }
        holder.btnDelete.setOnClickListener { onDeleteClick(test) }
    }

    override.getItemCount() = testList.size

    fun updateList(newList: List<TestItem>) {
        testList = newList
        notifyDataSetChanged()
    }
}
