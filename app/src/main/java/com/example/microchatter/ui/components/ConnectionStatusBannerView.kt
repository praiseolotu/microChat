package com.example.microchatter.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.microchatter.R
import com.example.microchatter.databinding.ViewConnectionBannerBinding

class ConnectionStatusBannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding =
        ViewConnectionBannerBinding.inflate(LayoutInflater.from(context), this, true)

    fun setConnected(connected: Boolean) {
        binding.statusText.text = if (connected) "Connected" else "Disconnected"
        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (connected) R.color.green else R.color.red
            )
        )
    }
}