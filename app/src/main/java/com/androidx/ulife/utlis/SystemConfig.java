/*
 * SHENZHEN TRANSSION COMMUNICATION LIMITED TOP SECRET.
 * Copyright (c) 2016-2036  SHENZHEN TRANSSION COMMUNICATION LIMITED
 *
 * PROPRIETARY RIGHTS of Shenzhen Transsion Communication Limited are involved in the
 * subject matter of this material.  All manufacturing, reproduction, use,
 * and sales rights pertaining to this subject matter are governed by the
 * license agreement.  The recipient of this software implicitly accepts
 * the terms of the license.
 *
 */

package com.androidx.ulife.utlis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.Utils;

/**

 * Author : weibo.jin
 * Date : 2021/10/20-13:06
 * Description :
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SystemConfig {
    private static String mCurrentFlavor;
    public static String OS_VERSION = getOsVersion(Utils.getApp());


    public @interface OSType{
        String HIOS = "hios";
        String XOS = "xos";
        String ITEL = "itel";
        String RCS = "rcs";
        String OSLAB = "bugly";
        String INDIA = "india";
    }

    public static String PHONE_MODEL = SystemPropertiesUtil.getString(Utils.getApp(), "ro.product.model");

    public static String PHONE_BARND = SystemPropertiesUtil.getString(Utils.getApp(), "ro.product.brand");

    /**
     * 是否支持虚拟卡
     */
    public static String PHONE_SUPPORT_VIRTUAL_SIM = SystemPropertiesUtil.getString(Utils.getApp(), "sys.skyroam.vsim");


    /**
     * Sim slot id
     */
    public static String PHONE_SIM_SLOT_INDEX = SystemPropertiesUtil.getString(Utils.getApp(), "sys.skyroam.sim.slot");


    /**
     * 是否支持虚拟卡
     */
    public static boolean isSupportVirtualSim(){
        return "2".equals(PHONE_SUPPORT_VIRTUAL_SIM);
    }

    /**
     * @param simSlotIndex sim卡位于卡槽index 一般为0或者1
     * @return 判断是否为虚拟卡
     */
    public static boolean isVirtualSim(int simSlotIndex){
        if( isSupportVirtualSim() ){
            return PHONE_SIM_SLOT_INDEX.contains(String.valueOf(simSlotIndex));
        }
        return false;
    }

    private static String getOsVersion(Context context) {
        String osVersion = SystemPropertiesUtil.getString(context, "ro.tranos.version");
        if (TextUtils.isEmpty(osVersion)) {
            osVersion = SystemPropertiesUtil.getString(context, "ro.os_product.version");
        }
        if (TextUtils.isEmpty(osVersion)) {
            osVersion = "";
        }
        return osVersion;
    }

    public static boolean isXOS(){
        return OS_VERSION.contains(OSType.XOS);
    }

    public static boolean isHIOS(){
        return OS_VERSION.contains(OSType.HIOS);
    }

    public static boolean isItel(){
        return OS_VERSION.contains(OSType.ITEL);
    }

    public static boolean isRCS() {
        return TextUtils.equals(OSType.RCS,mCurrentFlavor);
    }

    public static boolean isOsLab(){ return TextUtils.equals(OSType.OSLAB,mCurrentFlavor) ||TextUtils.equals(OSType.INDIA,mCurrentFlavor) ;}

    public static boolean isGoSupport(){
        return SystemPropertiesUtil.getString(Utils.getApp(), "ro.os_smartmessage_lite_support").equals("1");
    }

    private static final String MESSAGE_PACKAGE = "com.chuanglan.messaging";
    private static final String MESSAGE_ACTIVITY = "com.android.messaging.ui.conversationlist.ConversationListActivity";

    public static void setbadge(final int count) {
        Intent newIntent = new Intent("com.mediatek.action.UNREAD_CHANGED");
        newIntent.putExtra("com.mediatek.intent.extra.UNREAD_NUMBER", count);
        newIntent.putExtra("com.mediatek.intent.extra.UNREAD_COMPONENT",
                new ComponentName(MESSAGE_PACKAGE, MESSAGE_ACTIVITY));
        Utils.getApp().sendBroadcast(newIntent);
    }
}
