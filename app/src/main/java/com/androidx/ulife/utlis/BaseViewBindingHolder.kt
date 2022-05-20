package com.androidx.ulife.utlis

import android.view.View
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.viewholder.BaseViewHolder

@Suppress("UNCHECKED_CAST")
open class BaseViewBindingHolder<VB : ViewBinding>(view: View) : BaseViewHolder(view) {
    lateinit var binding: VB
}