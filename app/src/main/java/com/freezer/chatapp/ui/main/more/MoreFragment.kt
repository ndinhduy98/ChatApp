package com.freezer.chatapp.ui.main.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.freezer.chatapp.data.viewmodel.MyProfileViewModel
import com.freezer.chatapp.databinding.FragmentMoreBinding
import com.freezer.chatapp.ui.BaseFragment

class MoreFragment : BaseFragment() {

    private var _binding: FragmentMoreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: MyProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[MyProfileViewModel::class.java]

        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        _binding!!.viewModel = viewModel
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}