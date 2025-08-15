package com.gpssimulator.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gpssimulator.R
import com.gpssimulator.data.database.RouteEntity
import com.gpssimulator.data.model.MovementType
import com.gpssimulator.databinding.ItemRouteHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onRouteClick: (RouteEntity) -> Unit
) : ListAdapter<RouteEntity, HistoryAdapter.RouteViewHolder>(RouteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding, onRouteClick)
    }
    
    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class RouteViewHolder(
        private val binding: ItemRouteHistoryBinding,
        private val onRouteClick: (RouteEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(route: RouteEntity) {
            binding.apply {
                routeNameTextView.text = route.name
                routeDistanceTextView.text = "${route.totalDistance.toInt()}m"
                routeTypeTextView.text = route.movementType.name
                routeDateTextView.text = formatDate(route.createdAt)
                routeStatusTextView.text = if (route.isCompleted) "Completed" else "Incomplete"
                
                // Set status color
                routeStatusTextView.setTextColor(
                    android.content.ContextCompat.getColor(
                        root.context,
                        if (route.isCompleted) android.R.color.holo_green_dark else android.R.color.holo_orange_dark
                    )
                )
                
                // Show duration if completed
                if (route.isCompleted && route.actualDuration > 0) {
                    routeDurationTextView.text = formatDuration(route.actualDuration)
                    routeDurationTextView.visibility = android.view.View.VISIBLE
                } else {
                    routeDurationTextView.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener {
                    onRouteClick(route)
                }
            }
        }
        
        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return format.format(date)
        }
        
        private fun formatDuration(durationMillis: Long): String {
            val seconds = durationMillis / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            
            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        }
    }
    
    private class RouteDiffCallback : DiffUtil.ItemCallback<RouteEntity>() {
        override fun areItemsTheSame(oldItem: RouteEntity, newItem: RouteEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: RouteEntity, newItem: RouteEntity): Boolean {
            return oldItem == newItem
        }
    }
}
