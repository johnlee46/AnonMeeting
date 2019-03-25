package com.comp3617.anonmeeting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView


class ListAdapter(private val ctx: Context, private val teams: List<String>) : ArrayAdapter<String>(ctx, 0, teams) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView: View? = null

        if (convertView == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.row_layout, parent, false)
        } else
            rowView = convertView

        val logo = rowView!!.findViewById(R.id.imgTitle) as ImageView
        val tvTitle = rowView.findViewById(R.id.teamTitle) as TextView
        val tvDesc = rowView.findViewById(R.id.description) as TextView

        val team = teams[position]

        tvTitle.text = team

        return rowView
    }
}