package com.androidx.ulife.model;

import android.net.Uri;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author:  Nicky
 * Date:  2021/10/26 18:22 PM
 * Desc:  富媒体消
 */
public class IpMsgData {

    @StringDef({ShareType.TEXT, ShareType.IMAGE,
            ShareType.AUDIO, ShareType.VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareType {
        String TEXT = "text/plain";
        String IMAGE = "image/*";
        String AUDIO = "audio/*";
        String VIDEO = "video/*";
    }

    private String phone;
    private String sms;

    private String emailAddress;
    private String emailSubject;
    private String emailContent;

    private String copyContent;

    private String shareType = ShareType.TEXT;
    private String shareTitle = "";
    private String shareContent;
    private Uri shareUri;

    private String url;

    private String deeplinkUrl;
    private String deeplinkAppPackage;
    private String deeplinkAppName;
    private String deeplinkBackupUrl;


    private String mapUrl;
    private String mapLocX;
    private String mapLocY;


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getCopyContent() {
        return copyContent;
    }

    public void setCopyContent(String copyContent) {
        this.copyContent = copyContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShareType() {
        return shareType;
    }

    public void setShareType(String shareType) {
        this.shareType = shareType;
    }

    public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public Uri getShareUri() {
        return shareUri;
    }

    public void setShareUri(Uri shareUri) {
        this.shareUri = shareUri;
    }

    public String getDeeplinkUrl() {
        return deeplinkUrl;
    }

    public void setDeeplinkUrl(String deeplinkUrl) {
        this.deeplinkUrl = deeplinkUrl;
    }

    public String getDeeplinkAppPackage() {
        return deeplinkAppPackage;
    }

    public void setDeeplinkAppPackage(String deeplinkAppPackage) {
        this.deeplinkAppPackage = deeplinkAppPackage;
    }

    public String getDeeplinkAppName() {
        return deeplinkAppName;
    }

    public void setDeeplinkAppName(String deeplinkAppName) {
        this.deeplinkAppName = deeplinkAppName;
    }

    public String getDeeplinkBackupUrl() {
        return deeplinkBackupUrl;
    }

    public void setDeeplinkBackupUrl(String deeplinkBackupUrl) {
        this.deeplinkBackupUrl = deeplinkBackupUrl;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getMapLocX() {
        return mapLocX;
    }

    public void setMapLocX(String mapLocX) {
        this.mapLocX = mapLocX;
    }

    public String getMapLocY() {
        return mapLocY;
    }

    public void setMapLocY(String mapLocY) {
        this.mapLocY = mapLocY;
    }


    public static final class IpMsgDataBuilder {
        private String phone;
        private String sms;
        private String emailAddress;
        private String emailSubject;
        private String emailContent;
        private String copyContent = "";
        private String shareType = ShareType.TEXT;
        private String shareTitle = "";
        private String shareContent = "";
        private Uri shareUri;
        private String url;
        private String deeplinkUrl = "";
        private String deeplinkAppPackage = "";
        private String deeplinkAppName = "";
        private String deeplinkBackupUrl = "";
        private String mapUrl;
        private String mapLocX;
        private String mapLocY;

        private IpMsgDataBuilder() {
        }

        public static IpMsgDataBuilder anIpMsgData() {
            return new IpMsgDataBuilder();
        }

        public IpMsgDataBuilder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public IpMsgDataBuilder withSms(String sms) {
            this.sms = sms;
            return this;
        }

        public IpMsgDataBuilder withEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public IpMsgDataBuilder withEmailSubject(String emailSubject) {
            this.emailSubject = emailSubject;
            return this;
        }

        public IpMsgDataBuilder withEmailContent(String emailContent) {
            this.emailContent = emailContent;
            return this;
        }

        public IpMsgDataBuilder withCopyContent(String copyContent) {
            this.copyContent = copyContent;
            return this;
        }

        public IpMsgDataBuilder withShareType(String shareType) {
            this.shareType = shareType;
            return this;
        }

        public IpMsgDataBuilder withShareTitle(String shareTitle) {
            this.shareTitle = shareTitle;
            return this;
        }

        public IpMsgDataBuilder withShareContent(String shareContent) {
            this.shareContent = shareContent;
            return this;
        }

        public IpMsgDataBuilder withShareUri(Uri shareUri) {
            this.shareUri = shareUri;
            return this;
        }

        public IpMsgDataBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public IpMsgDataBuilder withDeeplinkUrl(String deeplinkUrl) {
            this.deeplinkUrl = deeplinkUrl;
            return this;
        }

        public IpMsgDataBuilder withDeeplinkAppPackage(String deeplinkAppPackage) {
            this.deeplinkAppPackage = deeplinkAppPackage;
            return this;
        }

        public IpMsgDataBuilder withDeeplinkAppName(String deeplinkAppName) {
            this.deeplinkAppName = deeplinkAppName;
            return this;
        }

        public IpMsgDataBuilder withDeeplinkBackupUrl(String deeplinkBackupUrl) {
            this.deeplinkBackupUrl = deeplinkBackupUrl;
            return this;
        }

        public IpMsgDataBuilder withMapUrl(String mapUrl) {
            this.mapUrl = mapUrl;
            return this;
        }

        public IpMsgDataBuilder withMapLocX(String mapLocX) {
            this.mapLocX = mapLocX;
            return this;
        }

        public IpMsgDataBuilder withMapLocY(String mapLocY) {
            this.mapLocY = mapLocY;
            return this;
        }

        public IpMsgData build() {
            IpMsgData ipMsgData = new IpMsgData();
            ipMsgData.setPhone(phone);
            ipMsgData.setSms(sms);
            ipMsgData.setEmailAddress(emailAddress);
            ipMsgData.setEmailSubject(emailSubject);
            ipMsgData.setEmailContent(emailContent);
            ipMsgData.setCopyContent(copyContent);
            ipMsgData.setShareType(shareType);
            ipMsgData.setShareTitle(shareTitle);
            ipMsgData.setShareContent(shareContent);
            ipMsgData.setShareUri(shareUri);
            ipMsgData.setUrl(url);
            ipMsgData.setDeeplinkUrl(deeplinkUrl);
            ipMsgData.setDeeplinkAppPackage(deeplinkAppPackage);
            ipMsgData.setDeeplinkAppName(deeplinkAppName);
            ipMsgData.setDeeplinkBackupUrl(deeplinkBackupUrl);
            ipMsgData.setMapUrl(mapUrl);
            ipMsgData.setMapLocX(mapLocX);
            ipMsgData.setMapLocY(mapLocY);
            return ipMsgData;
        }
    }
}
