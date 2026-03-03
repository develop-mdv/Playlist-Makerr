package com.example.playlistmakerr.presentation.util

import android.content.Context
import com.example.playlistmakerr.R

object MinutesFormatter {
    fun format(count: Int, context: Context): String {
        val mod100 = count % 100
        val mod10 = count % 10
        return when {
            mod100 in 11..19 -> context.getString(R.string.minutes_many, count)
            mod10 == 1 -> context.getString(R.string.minutes_one, count)
            mod10 in 2..4 -> context.getString(R.string.minutes_few, count)
            else -> context.getString(R.string.minutes_many, count)
        }
    }
}
