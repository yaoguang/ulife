package com.androidx.ulife.ui.home

import com.androidx.ulife.base.BaseBindingFragment
import com.androidx.ulife.databinding.FragmentHomePageBinding

class HomePageFragment : BaseBindingFragment<FragmentHomePageBinding>() {
    private val viewModel by androidViewModels<HomePageViewModel>()

    override fun init() {
    }
}