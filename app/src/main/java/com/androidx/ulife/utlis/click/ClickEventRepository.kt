package com.androidx.ulife.utlis.click

import android.content.Context
import com.androidx.ulife.model.ClickAction.Payload

/**
 * Author:  Nicky
 * Date:  2021/10/26 18:22 PM
 * Desc:  富媒体消息点击事件
 */
interface ClickEventRepository {
    fun executeClickAction(context: Context, clickType: Int, msgData: Payload): Boolean
}