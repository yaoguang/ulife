package com.androidx.ulife.utlis.click

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.androidx.ulife.app.app
import com.androidx.ulife.model.ClickAction
import com.androidx.ulife.model.ClickAction.Payload
import com.androidx.ulife.model.IpMsgData
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.GsonUtils

/**
 * Author:  Nicky
 * Date:  2021/10/26 18:22 PM
 * Desc:  消息点击事件实现类
 */
object ClickEventRepositoryImpl : ClickEventRepository {
    @JvmStatic
    fun executeClickAction(action: String?): Boolean {
        if (action.isNullOrEmpty())
            return false
        return try {
            val act = GsonUtils.fromJson(action, ClickAction::class.java)
            if (act.payload == null) {
                false
            } else executeClickAction(app, act.clickType, act.payload!!)
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun executeClickAction(context: Context, action: ClickAction?): Boolean {
        return if (action?.payload == null) {
            false
        } else executeClickAction(context, action.clickType, action.payload!!)
    }

    override fun executeClickAction(context: Context, clickType: Int, msgData: Payload): Boolean {
        return when (clickType) {
            ClickEventConstant.ClickType.DIAL_PHONE -> dailPhone(context, msgData)
            ClickEventConstant.ClickType.SEND_SMS -> sendSms(context, msgData)
            ClickEventConstant.ClickType.SEND_EMAIL -> sendEmail(context, msgData)
            ClickEventConstant.ClickType.COPY_TEXT -> copyText(context, msgData)
            ClickEventConstant.ClickType.SHARE -> share(context, msgData)
            ClickEventConstant.ClickType.OPEN_URL -> openUrl(context, msgData)
            ClickEventConstant.ClickType.OPEN_DEEPLINK -> openDeepLink(context, msgData)
            ClickEventConstant.ClickType.OPEN_MAP -> openMap(context, msgData)
            else -> false
        }
    }

    /**
     * 拨打电话
     */
    private fun dailPhone(context: Context, msgData: Payload): Boolean {
        var result: Boolean
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + msgData.dailNum))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 发送短信
     */
    private fun sendSms(context: Context, msgData: Payload): Boolean {
        var result: Boolean
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + msgData.sendTo))
            intent.putExtra("sms_body", msgData.sendContent)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //        intent.putExtra(ColorMmsConstant.CLEAR_CONVERSATION_TASK, false);
            //        intent.setPackage("com.android.mms");
            context.startActivity(intent)
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 发送邮件
     */
    private fun sendEmail(context: Context, msgData: Payload): Boolean {
        var result: Boolean
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:" + msgData.sendTo)
            intent.putExtra(Intent.EXTRA_SUBJECT, msgData.subject)
            intent.putExtra(Intent.EXTRA_TEXT, msgData.content)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 复制文本
     */
    private fun copyText(context: Context, msgData: Payload): Boolean {
        val result: Boolean = try {
            val clipboardManager = (context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, msgData.extData))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        return result
    }

    /**
     * 分享
     */
    private fun share(context: Context, msgData: Payload): Boolean {
        var result = false
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.type = msgData.type
            when (msgData.type) {
                IpMsgData.ShareType.TEXT -> {
                    intent.putExtra(Intent.EXTRA_TEXT, msgData.shareContent)
                    context.startActivity(Intent.createChooser(intent, msgData.title))
                    result = true
                }
                IpMsgData.ShareType.IMAGE, IpMsgData.ShareType.AUDIO, IpMsgData.ShareType.VIDEO -> {
                    intent.putExtra(Intent.EXTRA_STREAM, msgData.shareUrl)
                    context.startActivity(Intent.createChooser(intent, msgData.title))
                    result = true
                }
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 跳转Url
     */
    private fun openUrl(context: Context, msgData: Payload): Boolean {
        val result: Boolean = try {
            openUrl(context, msgData.forwardUrl, true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        return result
    }

    /**
     * @param autoFillScheme 自动填充Scheme
     */
    private fun openUrl(context: Context, url: String?, autoFillScheme: Boolean) {
        if (url == null)
            return
        var uri = Uri.parse(url.trim { it <= ' ' })
        if (autoFillScheme) {
            val scheme = uri.scheme
            if (TextUtils.isEmpty(scheme)) {
                val newUrl = "http://" + url.trim { it <= ' ' }
                uri = Uri.parse(newUrl)
            }
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 跳转deeplink
     */
    private fun openDeepLink(context: Context, msgData: Payload): Boolean {
        var result = false
        try {
            if (AppUtils.isAppInstalled(msgData.appPkg)) {
                //跳转App已经安装
                result = if (TextUtils.isEmpty(msgData.forwardUrl)) {
                    //打开APP
                    openApp(context, msgData.appPkg!!)
                } else {
                    //跳转deeplink
                    openUrl(context, msgData.forwardUrl, false)
                    true
                }
            } else if (!TextUtils.isEmpty(msgData.htmlUrl)) {
                //打开备用链接
                openUrl(context, msgData.htmlUrl, true)
                result = true
            } else {
                //去应用市场下载
                gotoMarket(context, msgData.marketUri, msgData.marketPkg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 打开其它app
     */
    private fun openApp(context: Context, packageName: String): Boolean {
        var result = false
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            try {
                intent.putExtra("type", "110")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                result = true
            } catch (e: Exception) {
                e.printStackTrace()
                result = false
            }
        }
        return result
    }

    /**
     * 打开地图应用到具体位置
     */
    private fun openMap(context: Context, msgData: Payload): Boolean {
        var result: Boolean
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (msgData.url?.isNotEmpty() == true) {
                if (msgData.url.contains("map.qq.com")) {
                    intent.setPackage("com.tencent.map")
                } else if (msgData.url.contains("map.baidu.com")) {
                    intent.setPackage("com.baidu.BaiduMap")
                } else if (msgData.url.contains("amap.com") || msgData.url.contains("gaode.com")) {
                    intent.setPackage("com.autonavi.minimap")
                } else if (msgData.url.contains("map.sogou.com")) {
                    intent.setPackage("com.sogou.map.android.maps")
                }
            }
            val geo = "geo:" + msgData.locationY + "," + msgData.locationX
            intent.data = Uri.parse(geo)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivity(intent)
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    // 进入应用市场详情页
    private fun gotoMarket(context: Context, marketUri: String?, marketPackage: String?) {
        if (marketUri.isNullOrEmpty() || marketPackage.isNullOrEmpty())
            return
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(marketUri)
        intent.setPackage(marketPackage)
        context.startActivity(intent)
    }
}