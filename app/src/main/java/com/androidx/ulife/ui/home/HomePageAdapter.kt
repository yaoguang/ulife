package com.androidx.ulife.ui.home

import android.annotation.SuppressLint
import com.androidx.ulife.R
import com.androidx.ulife.databinding.ItemHomePageBinding
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.UlifeResp
import com.androidx.ulife.utlis.BaseViewBindingHolder
import com.androidx.ulife.utlis.BaseViewBindingQuickAdapter

class HomePageAdapter : BaseViewBindingQuickAdapter<UlifeResp.QueryResponse, ItemHomePageBinding>(R.layout.item_home_page) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewBindingHolder<ItemHomePageBinding>, item: UlifeResp.QueryResponse) {
        val sp = StringBuilder()
        sp.append("type:${item.partType}； version:${item.version}； time:${item.updateTime}； data:${item.dataPartCase}")
        if (item.partType == PART_TYPE_USSD) {
            appendImsi(sp, item.ussdPart.imsi1)
            appendImsi(sp, item.ussdPart.imsi2)
        }
        holder.binding.info.text = sp
    }

    private fun appendImsi(sp: StringBuilder, imsi: UlifeResp.ImsiPart?) {
        if (imsi != null)
            sp.append("\nimsi1: mccMnc:${imsi.mccmnc}； version:${imsi.version}； time:${imsi.updateTime}； data:${imsi.dataSetList.size}")
    }
}