package com.androidx.ulife.simcard

import com.androidx.ulife.model.SimCardInfo

object SimCardManager {
    lateinit var sim1: SimCardInfo
        private set
    lateinit var sim2: SimCardInfo
        private set

    init {
        sim1 = SimCardInfo(1, "aaa", 400191111L, 400, 19)
        sim2 = SimCardInfo(2, "bbb", 500201111L, 500, 20)
    }

    fun requestMccMnc(): ArrayList<Int> {
        val list = ArrayList<Int>()
        if (sim1.isValued())
            list.add(sim1.mccMnc)
        if (sim2.isValued())
            list.add(sim2.mccMnc)
        return list
    }
}