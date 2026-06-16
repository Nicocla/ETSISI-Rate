package com.example.valoracionprofesores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResenaAdapter(
    private val listaResenas: List<Resena>,
    private val onLongClick: (Resena) -> Unit
) : RecyclerView.Adapter<ResenaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // YA NO BUSCAMOS EL ID DEL EMAIL PORQUE NO EXISTE
        // val tvEmail: TextView = view.findViewById(R.id.tvEmailResena) <--- BORRADO

        val tvPuntuacion: TextView = view.findViewById(R.id.tvPuntuacionResena)
        val tvComentario: TextView = view.findViewById(R.id.tvComentarioResena)

        // Tags
        val tagHorario: TextView = view.findViewById(R.id.tvTagHorario)
        val tagMaterial: TextView = view.findViewById(R.id.tvTagMaterial)
        val tagAtencion: TextView = view.findViewById(R.id.tvTagAtencion)
        val tagTutorias: TextView = view.findViewById(R.id.tvTagTutorias)
        val tagGuia: TextView = view.findViewById(R.id.tvTagGuia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resena, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resena = listaResenas[position]

        // --- SECCIÓN DE EMAIL ELIMINADA (ANONIMATO TOTAL) ---
        // Ya no ponemos texto en holder.tvEmail

        holder.tvPuntuacion.text = String.format("%.1f ⭐", resena.puntuacion)
        holder.tvComentario.text = resena.comentario ?: "Sin comentario"

        // Rellenar etiquetas
        holder.tagHorario.text = "🕒 Horario: ${resena.nota_horario.toInt()}"
        holder.tagMaterial.text = "📚 Material: ${resena.nota_material.toInt()}"
        holder.tagAtencion.text = "🙋‍♂️ Atención: ${resena.nota_atencion.toInt()}"
        holder.tagTutorias.text = "🎓 Tutorías: ${resena.nota_tutorias.toInt()}"
        holder.tagGuia.text = "📋 Guía: ${resena.nota_guia.toInt()}"

        // El borrado sigue funcionando igual aunque no se vea el email
        holder.itemView.setOnLongClickListener {
            onLongClick(resena)
            true
        }
    }

    override fun getItemCount() = listaResenas.size
}