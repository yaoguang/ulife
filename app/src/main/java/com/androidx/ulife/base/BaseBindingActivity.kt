package com.androidx.ulife.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseBindingActivity<VB : ViewBinding> : BaseActivity() {
    lateinit var binding: VB

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val clazz = type.actualTypeArguments[0] as Class<VB>
            val method = clazz.getMethod("inflate", LayoutInflater::class.java)
            binding = method.invoke(null, layoutInflater) as VB
            setContentView(binding.root)
        }

        init()
    }

    abstract fun init()
}