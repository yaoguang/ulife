package com.androidx.ulife.ui.home

import android.annotation.SuppressLint
import com.androidx.ulife.R
import com.androidx.ulife.dao.HomeImsiListPart
import com.androidx.ulife.dao.HomePagePart
import com.androidx.ulife.dao.HomeCarrierPart
import com.androidx.ulife.databinding.ItemHomePageBinding
import com.androidx.ulife.model.PART_TYPE_TOP_UP
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.UlifeResp
import com.androidx.ulife.utlis.BaseViewBindingHolder
import com.androidx.ulife.utlis.BaseViewBindingQuickAdapter

class HomePagePartAdapter : BaseViewBindingQuickAdapter<HomePagePart, ItemHomePageBinding>(R.layout.item_home_page) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewBindingHolder<ItemHomePageBinding>, item: HomePagePart) {
        val sp = StringBuilder()
        sp.append("type:${item.partType}； version:${item.version}； time:${item.updateTime}； data:${item.dataProto?.dataPartCase}")
        if ((item.partType == PART_TYPE_USSD || item.partType == PART_TYPE_TOP_UP) && item.dataPart != null) {
            appendImsi(sp, (item.dataPart as HomeImsiListPart).imsi1)
            appendImsi(sp, (item.dataPart as HomeImsiListPart).imsi2)
        }
        holder.binding.info.text = sp
    }

    private fun appendImsi(sp: StringBuilder, part: HomeCarrierPart?) {
        kotlin.run {
            val imsi = part?.dataProto as? UlifeResp.ImsiUssdPart?
            if (imsi != null)
                sp.append("\nimsi1: mccMnc:${imsi.imsi}； version:${part.version}； time:${part.updateTime}； data:${imsi.dataSetList.size}")
        }
        kotlin.run {
            val imsi = part?.dataProto as? UlifeResp.ImsiRechargePart?
            if (imsi != null)
                sp.append("\nimsi1: mccMnc:${imsi.imsi}； version:${part.version}； time:${part.updateTime}； data:${imsi.dataSetList.size}")
        }
    }
}