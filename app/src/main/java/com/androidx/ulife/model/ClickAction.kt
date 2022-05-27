package com.androidx.ulife.model

class ClickAction {
    var clickType = -1

    var payload: Payload? = null

    inner class Payload {
        val htmlUrl: String? = null
        val appName: String? = null
        val appPkg: String? = null
        val shareContent: String? = null
        val shareUrl: String? = null
        val title: String? = null
        val type: String? = null
        val dailNum: String? = null
        val payType = 0
        val amount = 0
        val sendContent: String? = null
        val url: String? = null
        val locationX: String? = null
        val locationY: String? = null
        val browserType = 0
        val description: String? = null
        val extData: String? = null
        val sendTo: String? = null
        val subject: String? = null
        val content: String? = null
        val forwardUrl: String? = null

        //应用市场产品详情Uri
        val marketUri: String? = null

        //应用市场包名
        val marketPkg: String? = null
    }
}