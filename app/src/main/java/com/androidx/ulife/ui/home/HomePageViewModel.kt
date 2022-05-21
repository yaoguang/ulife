package com.androidx.ulife.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.androidx.ulife.repository.HomePageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class HomePageViewModel(application: Application) : AndroidViewModel(application) {
    suspend fun homeInfo() = withContext(Dispatchers.IO) { HomePageRepository.homePageInfo().flowOn(Dispatchers.IO) }
}