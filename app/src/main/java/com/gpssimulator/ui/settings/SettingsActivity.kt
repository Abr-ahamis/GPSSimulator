package com.gpssimulator.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gpssimulator.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSettings()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupSettings() {
        // Mock location settings
        binding.mockLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // This would typically open developer options
            // For now, just show a message
            if (isChecked) {
                android.widget.Toast.makeText(
                    this,
                    "Enable mock location in Developer Options",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
        
        // Notification settings
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save notification preference
            val sharedPreferences = getSharedPreferences("gps_simulator_prefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
        }
        
        // Speed variation settings
        binding.speedVariationSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.speedVariationValue.text = "$progress%"
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Save speed variation preference
                val sharedPreferences = getSharedPreferences("gps_simulator_prefs", MODE_PRIVATE)
                sharedPreferences.edit().putInt("speed_variation", seekBar?.progress ?: 20).apply()
            }
        })
        
        // Load saved preferences
        loadPreferences()
    }
    
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences("gps_simulator_prefs", MODE_PRIVATE)
        
        binding.notificationSwitch.isChecked = sharedPreferences.getBoolean("notifications_enabled", true)
        binding.speedVariationSeekBar.progress = sharedPreferences.getInt("speed_variation", 20)
        binding.speedVariationValue.text = "${binding.speedVariationSeekBar.progress}%"
    }
}
