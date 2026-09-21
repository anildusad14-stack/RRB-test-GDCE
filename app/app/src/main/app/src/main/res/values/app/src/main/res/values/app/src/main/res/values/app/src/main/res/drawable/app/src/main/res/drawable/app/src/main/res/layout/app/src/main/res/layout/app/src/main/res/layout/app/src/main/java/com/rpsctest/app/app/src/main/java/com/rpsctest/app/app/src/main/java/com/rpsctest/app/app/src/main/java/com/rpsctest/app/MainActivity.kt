package com.rpsctest.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var testManager: TestManager
    private lateinit var adapter: TestAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private var allTests: List<TestItem> = emptyList()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Toast.makeText(this, "Importing test, please wait...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch(Dispatchers.IO) {
                val success = testManager.importFile(uri)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@MainActivity, "Test Imported Successfully!", Toast.LENGTH_SHORT).show()
                        loadTests()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to import test. Ensure it's valid HTML/ZIP.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        testManager = TestManager(this)
        recyclerView = findViewById(R.id.recyclerView)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TestAdapter(emptyList(), this::openTest, this::confirmDelete)
        recyclerView.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAddTest).setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        loadTests()
    }

    private fun loadTests() {
        allTests = testManager.getTests()
        adapter.updateList(allTests)
        tvEmptyState.visibility = if (allTests.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openTest(test: TestItem) {
        val intent = Intent(this, TestViewerActivity::class.java).apply {
            putExtra("INDEX_PATH", test.indexFilePath)
        }
        startActivity(intent)
    }

    private fun confirmDelete(test: TestItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Test")
            .setMessage("Are you sure you want to delete '${test.name}'? Your original file will not be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                testManager.deleteTest(test)
                loadTests()
                Toast.makeText(this, "Deleted locally", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(android.R.menu.search_view, menu)
        
        val searchItem = menu.add(Menu.NONE, 1, Menu.NONE, "Search")
        searchItem.setShowAsAction(Menu.SHOW_AS_ACTION_IF_ROOM or Menu.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        
        val searchView = SearchView(this)
        searchView.queryHint = getString(R.string.search_hint)
        searchItem.actionView = searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = allTests.filter { 
                    it.name.contains(newText ?: "", ignoreCase = true) || 
                    it.fileName.contains(newText ?: "", ignoreCase = true) 
                }
                adapter.updateList(filtered)
                return true
            }
        })
        return true
    }
}
