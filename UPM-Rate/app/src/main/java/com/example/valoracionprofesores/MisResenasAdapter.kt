package com.example.valoracionprofesores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MisResenasAdapter(
    private val lista: List<MiResenaItem>,
    private val onLongClick: (MiResenaItem) -> Unit
) : RecyclerView.Adapter<MisResenasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProfeHistorial)
        val tvComentario: TextView = view.findViewById(R.id.tvComentarioHistorial)
        val rb: RatingBar = view.findViewById(R.id.rbHistorial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mi_resena, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.tvNombre.text = item.nombre_profesor
        holder.tvComentario.text = item.comentario ?: "Sin comentario"
        holder.rb.rating = item.puntuacion / 2f

        // DETECTAR PULSACIÓN LARGA PARA BORRAR
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount() = lista.size
}