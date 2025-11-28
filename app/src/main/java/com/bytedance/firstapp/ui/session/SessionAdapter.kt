package com.bytedance.firstapp.ui.session

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.firstapp.data.model.Session
import com.bytedance.firstapp.databinding.ItemSessionBinding

class SessionAdapter(
    private val onClick: (Session) -> Unit,
    private val onLongClick: (Session) -> Unit
) : ListAdapter<Session, SessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(
        private val binding: ItemSessionBinding,
        private val onClick: (Session) -> Unit,
        private val onLongClick: (Session) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private var currentSession: Session? = null

        init {
            binding.root.setOnClickListener {
                currentSession?.let { onClick(it) }
            }
            binding.root.setOnLongClickListener {
                currentSession?.let { onLongClick(it) }
                true
            }
        }

        fun bind(session: Session) {
            currentSession = session
            binding.textViewSessionTitle.text = session.title
            binding.textViewSessionPreview.text = session.lastMessagePreview
        }
    }
}

class SessionDiffCallback : DiffUtil.ItemCallback<Session>() {
    override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
        return oldItem == newItem
    }
}
