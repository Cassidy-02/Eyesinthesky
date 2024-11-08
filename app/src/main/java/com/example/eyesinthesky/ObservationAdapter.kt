package com.example.eyesinthesky

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ObservationAdapter(private val observations: List<BirdsObservation>): RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_observation, parent, false)
        return ObservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        val observation = observations[position]
        holder.birdNameTextView.text = observation.birdName
        holder.notesTextView.text = observation.notes
    }

    override fun getItemCount(): Int {
        return observations.size
    }

    class ObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val birdNameTextView: TextView = itemView.findViewById(R.id.birdNameTextView)
        val notesTextView: TextView = itemView.findViewById(R.id.notesTextView)
    }
}