package com.androidx.ulife.utlis.click;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author:  Nicky
 * Date:  2021/10/26 18:22 PM
 * Desc:  富媒体消息常量
 */
public final class ClickEventConstant {
    @IntDef({ClickType.DIAL_PHONE, ClickType.SEND_SMS, ClickType.SEND_EMAIL, ClickType.COPY_TEXT, ClickType.SHARE, ClickType.OPEN_URL,
            ClickType.OPEN_DEEPLINK, ClickType.OPEN_MAP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClickType {
        //消息点击类型
        int DIAL_PHONE = 0; //打电话
        int SEND_SMS = 1;//发短信
        int SEND_EMAIL = 2;//发邮件
        int COPY_TEXT = 3;//复制文本
        int SHARE = 4;//分享
        int OPEN_URL = 5;//跳转url
        int OPEN_DEEPLINK = 6;//跳转deeplink
        int OPEN_MAP = 7;//打开地图应用到具体位置
    }
}