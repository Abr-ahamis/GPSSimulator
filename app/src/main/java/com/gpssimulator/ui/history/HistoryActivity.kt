package com.gpssimulator.ui.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gpssimulator.GPSimulatorApp
import com.gpssimulator.databinding.ActivityHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        observeViewModel()
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        binding.clearButton.setOnClickListener {
            clearHistory()
        }
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter { route ->
            // Handle route click - could show details or export
        }
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.routeHistory.collectLatest { routes ->
                historyAdapter.submitList(routes)
                updateEmptyState(routes.isEmpty())
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateTextView.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        binding.clearButton.isEnabled = !isEmpty
    }
    
    private fun clearHistory() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to delete all completed routes?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.clearCompletedRoutes()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
