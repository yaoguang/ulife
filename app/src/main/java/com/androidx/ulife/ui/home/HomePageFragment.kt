package com.androidx.ulife.ui.home

import androidx.recyclerview.widget.LinearLayoutManager
import com.androidx.ulife.base.BaseBindingFragment
import com.androidx.ulife.databinding.FragmentHomePageBinding
import com.androidx.ulife.model.RequestCode
import com.androidx.ulife.ui.home.HomePageViewModel.Companion.REQUEST_LIST_DATA
import com.blankj.utilcode.util.ToastUtils

class HomePageFragment : BaseBindingFragment<FragmentHomePageBinding>() {
    private val viewModel by androidViewModels<HomePageViewModel>()

    override fun init() {
        val adapter = HomePageAdapter()
        binding.reqData.layoutManager = LinearLayoutManager(requireContext())
        binding.reqData.adapter = adapter

        adapter.setNewInstance(null)

        viewModel.listData.observe(this) { array ->
            if (array.isNullOrEmpty()) {
                adapter.setNewInstance(array)
            } else
                adapter.notifyDataSetChanged()
        }

        viewModel.errorData.observe(this) {
            if (it.tag == REQUEST_LIST_DATA) {
                if (it.others == RequestCode.SUCCESS) {
                    ToastUtils.showShort("请求成功")
                } else
                    ToastUtils.showShort(it.error?.message ?: "请求失败")
                binding.request.isEnabled = true
            }
        }

        binding.request.setOnClickListener {
            it.isEnabled = false
            adapter.setNewInstance(null)
            viewModel.requestListData()
        }
    }
}