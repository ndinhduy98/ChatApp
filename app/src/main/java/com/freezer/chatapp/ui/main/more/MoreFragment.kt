package com.freezer.chatapp.ui.main.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.freezer.chatapp.data.viewmodel.MyProfileViewModel
import com.freezer.chatapp.databinding.FragmentMoreBinding
import com.freezer.chatapp.ui.BaseFragment

class MoreFragment : BaseFragment() {

    private var _binding: FragmentMoreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val myProfileViewModel: MyProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        binding.viewModel = myProfileViewModel
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}