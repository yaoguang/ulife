package com.androidx.ulife.ui.home

import android.annotation.SuppressLint
import com.androidx.ulife.R
import com.androidx.ulife.dao.HomeUssdPart
import com.androidx.ulife.dao.HomePagePart
import com.androidx.ulife.dao.HomeCarrierPart
import com.androidx.ulife.databinding.ItemHomePageBinding
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.UlifeResp
import com.androidx.ulife.utlis.BaseViewBindingHolder
import com.androidx.ulife.utlis.BaseViewBindingQuickAdapter

class HomePagePartAdapter : BaseViewBindingQuickAdapter<HomePagePart, ItemHomePageBinding>(R.layout.item_home_page) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewBindingHolder<ItemHomePageBinding>, item: HomePagePart) {
        val sp = StringBuilder()
        sp.append("type:${item.partType}； version:${item.version}； time:${item.updateTime}； data:${item.dataProto?.dataPartCase}")
        if (item.partType == PART_TYPE_USSD && item.dataPart != null) {
            appendImsi(sp, (item.dataPart as HomeUssdPart).imsi1)
            appendImsi(sp, (item.dataPart as HomeUssdPart).imsi2)
        }
        holder.binding.info.text = sp
    }

    private fun appendImsi(sp: StringBuilder, part: HomeCarrierPart?) {
        val imsi: UlifeResp.ImsiPart? = part?.dataProto
        if (imsi != null)
            sp.append("\nimsi1: mccMnc:${imsi.mccmnc}； version:${part.version}； time:${part.updateTime}； data:${imsi.dataSetList.size}")
    }
}