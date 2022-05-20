package com.androidx.ulife.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.androidx.ulife.model.ErrorInfo
import com.androidx.ulife.model.RequestCode
import com.androidx.ulife.model.UlifeResp
import com.androidx.ulife.repository.HomePageRepository
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class HomePageViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val REQUEST_LIST_DATA = 1
    }

    val listData = MediatorLiveData<ArrayList<UlifeResp.QueryResponse>?>()
    val errorData = MediatorLiveData<ErrorInfo>()

    fun requestListData(partTypes: ArrayList<Int>) {
        listData.postValue(null)
        viewModelScope.launch {
            HomePageRepository.homePageInfo(partTypes)
                .catch {
                    errorData.postValue(ErrorInfo(REQUEST_LIST_DATA, it))
                }
                .onCompletion {
                }
                .collect {
                    if (it == null)
                        return@collect
                    listData.postValue(listData.value ?: ArrayList<UlifeResp.QueryResponse>().apply { add(it) })
                }
        }
    }

    fun requestListData() {
        val list = ArrayList<UlifeResp.QueryResponse>()
        listData.postValue(list)
        viewModelScope.launch(Dispatchers.IO) {
            var hasError = false
            HomePageRepository.homePageInfo()
                .catch {
                    errorData.postValue(ErrorInfo(REQUEST_LIST_DATA, it))
                    hasError = true
                }
                .onCompletion {
                    if (!hasError)
                        errorData.postValue(ErrorInfo(REQUEST_LIST_DATA, it, RequestCode.SUCCESS))
                }
                .collect {
                    LogUtils.d("ConvertValue", it)
                    if (it == null)
                        return@collect
                    list.add(it)
                    listData.postValue(list)
                }
        }
    }
}