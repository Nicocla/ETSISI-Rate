package com.example.valoracionprofesores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfesorAdapter(
    private val listaProfesores: List<Profesor>,
    private val esAdmin: Boolean,                     // Recibe si somos admin
    private val onBorrarClick: (Profesor) -> Unit,    // Función para la papelera
    private val onItemClick: (Profesor) -> Unit       // Función para entrar al detalle
) : RecyclerView.Adapter<ProfesorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvDepartamento: TextView = view.findViewById(R.id.tvDepartamento)
        val rbMedia: RatingBar = view.findViewById(R.id.rbMedia)
        val ivBorrar: ImageView = view.findViewById(R.id.ivBorrarProfesor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profesor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profesor = listaProfesores[position]

        // 1. Rellenar los textos
        holder.tvNombre.text = profesor.nombre
        holder.tvDepartamento.text = profesor.departamento

        // 2. Rellenar las estrellas
        // *NOTA: Si en tu clase Profesor la nota se llama de otra forma
        // (por ejemplo: profesor.media), cámbialo en la siguiente línea:
        holder.rbMedia.rating = profesor.media.toFloat() / 2f      // 3. LÓGICA DE LA PAPELERA (ADMIN)
        if (esAdmin) {
            holder.ivBorrar.visibility = View.VISIBLE
            holder.ivBorrar.setOnClickListener {
                onBorrarClick(profesor)
            }
        } else {
            holder.ivBorrar.visibility = View.GONE
        }

        // 4. CLIC EN LA TARJETA (Para ver detalles)
        holder.itemView.setOnClickListener {
            onItemClick(profesor)
        }
    }

    override fun getItemCount() = listaProfesores.size
}