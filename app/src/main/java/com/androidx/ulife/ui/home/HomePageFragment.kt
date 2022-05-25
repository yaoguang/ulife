package com.androidx.ulife.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.util.valueIterator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidx.ulife.base.BaseBindingFragment
import com.androidx.ulife.dao.HomePagePart
import com.androidx.ulife.databinding.FragmentHomePageBinding
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HomePageFragment : BaseBindingFragment<FragmentHomePageBinding>() {
    private val viewModel by androidViewModels<HomePageViewModel>()

    private val partAdapter = HomePagePartAdapter()

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.homePartListState.collect { parts ->
                        val list = ArrayList<HomePagePart>()
                        parts.valueIterator().forEach { list.add(it) }
                        partAdapter.setNewInstance(list)
                        cancel()
                    }
                }
                launch {
                    viewModel.homePartRefreshShare.collect { part ->
                        val index = partAdapter.data.indexOfFirst { it.partType == part.partType }
                        if (index >= 0)
                            partAdapter.setData(index, part)
                        else
                            partAdapter.addData(part)
                    }
                }
            }
        }
    }

    override fun init() {
        binding.reqData.layoutManager = LinearLayoutManager(requireContext())
        binding.reqData.adapter = partAdapter

        binding.request.setOnClickListener { v ->
            lifecycleScope.launch {
//                viewModel.homeInfo()
//                    .onStart {
//                        binding.reqData.adapter = partAdapter
//                        partAdapter.setNewInstance(null)
//                        v.isEnabled = false
//                    }.onCompletion {
//                        v.isEnabled = true
//                    }.catch {
//                        ToastUtils.showShort("请求失败")
//                    }
//                    .collect {
//                        partAdapter.addData(it)
//                    }

                viewModel.refreshHomeInfo()
            }
        }
    }
}