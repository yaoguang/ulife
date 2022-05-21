package com.androidx.ulife.ui.home

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidx.ulife.base.BaseBindingFragment
import com.androidx.ulife.databinding.FragmentHomePageBinding
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomePageFragment : BaseBindingFragment<FragmentHomePageBinding>() {
    private val viewModel by androidViewModels<HomePageViewModel>()

    override fun init() {
        val adapter = HomePageAdapter()
        val partAdapter = HomePagePartAdapter()
        binding.reqData.layoutManager = LinearLayoutManager(requireContext())
        binding.reqData.adapter = adapter

        binding.request.setOnClickListener { v ->
            lifecycleScope.launch {
                viewModel.homeInfo()
                    .onStart {
                        binding.reqData.adapter = partAdapter
                        partAdapter.setNewInstance(null)
                        v.isEnabled = false
                    }.onCompletion {
                        v.isEnabled = true
                    }.catch {
                        ToastUtils.showShort("请求失败")
                    }
                    .collect {
                        partAdapter.addData(it)
                    }
            }
        }
    }
}