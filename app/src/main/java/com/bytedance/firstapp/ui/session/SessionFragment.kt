package com.bytedance.firstapp.ui.session

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytedance.firstapp.R
import com.bytedance.firstapp.databinding.ActivitySessionBinding
import com.bytedance.firstapp.ui.chat.MainActivity

class SessionFragment : DialogFragment() {

    private var _binding: ActivitySessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.buttonNewChat.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            dismiss() // Close the dialog after starting the activity
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Apply the animations
            setWindowAnimations(R.style.DialogAnimation)

            // Calculate 2/3 of the screen width
            val screenWidth = resources.displayMetrics.widthPixels
            val dialogWidth = (screenWidth * 2 / 3.0).toInt()

            // Set the calculated width and full height
            setLayout(dialogWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            setGravity(Gravity.START)
            setBackgroundDrawableResource(android.R.color.transparent)

            // Correct way to set dimAmount
            val attributes = attributes
            attributes.dimAmount = 0.6f
            setAttributes(attributes)

            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun setupRecyclerView() {
        sessionAdapter = SessionAdapter { session ->
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("SESSION_ID", session.id)
            startActivity(intent)
            dismiss()
            Toast.makeText(requireContext(), "Opening session: ${session.title}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerViewSessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
