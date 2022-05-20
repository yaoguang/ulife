@file:Suppress("UNCHECKED_CAST")

package com.androidx.ulife.utlis

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

abstract class BaseViewBindingQuickAdapter<T, VB : ViewBinding>(
    @LayoutRes private val layoutResId: Int,
    data: MutableList<T>? = null
) : BaseQuickAdapter<T, BaseViewBindingHolder<VB>>(layoutResId, data) {

    private val vbInflateMethod: Method

    init {
        val type = javaClass.genericSuperclass
        val clazz = (type as ParameterizedType).actualTypeArguments[1] as Class<VB>
        vbInflateMethod = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewBindingHolder<VB> {
        val vb = vbInflateMethod.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        return BaseViewBindingHolder<VB>(vb.root).apply {
            binding = vb
        }
    }
}