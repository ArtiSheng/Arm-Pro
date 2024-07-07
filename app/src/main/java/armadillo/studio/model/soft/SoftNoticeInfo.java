/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.soft;

import armadillo.studio.model.Basic;

public class SoftNoticeInfo extends Basic {
    private data data;

    public static class data {
        private String title;

        private String msg;

        private Integer titleColor;

        private Integer msgColor;

        private String confirmText;

        private Integer confirmTextColor;

        private Integer confirmAction;

        private String confirmBody;

        private String cancelText;

        private Integer cancelTextColor;

        private String extraText;

        private Integer extraTextColor;

        private Integer extraAction;

        private String extraBody;

        private Integer dialogStyle;

        private String backgroundUrl;

        private Boolean smartPop;

        private Boolean cancelable;


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title == null ? null : title.trim();
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg == null ? null : msg.trim();
        }

        public Integer getTitleColor() {
            return titleColor;
        }

        public void setTitleColor(Integer titleColor) {
            this.titleColor = titleColor;
        }

        public Integer getMsgColor() {
            return msgColor;
        }

        public void setMsgColor(Integer msgColor) {
            this.msgColor = msgColor;
        }

        public String getConfirmText() {
            return confirmText;
        }

        public void setConfirmText(String confirmText) {
            this.confirmText = confirmText == null ? null : confirmText.trim();
        }

        public Integer getConfirmTextColor() {
            return confirmTextColor;
        }

        public void setConfirmTextColor(Integer confirmTextColor) {
            this.confirmTextColor = confirmTextColor;
        }

        public Integer getConfirmAction() {
            return confirmAction;
        }

        public void setConfirmAction(Integer confirmAction) {
            this.confirmAction = confirmAction;
        }

        public String getConfirmBody() {
            return confirmBody;
        }

        public void setConfirmBody(String confirmBody) {
            this.confirmBody = confirmBody == null ? null : confirmBody.trim();
        }

        public String getCancelText() {
            return cancelText;
        }

        public void setCancelText(String cancelText) {
            this.cancelText = cancelText == null ? null : cancelText.trim();
        }

        public Integer getCancelTextColor() {
            return cancelTextColor;
        }

        public void setCancelTextColor(Integer cancelTextColor) {
            this.cancelTextColor = cancelTextColor;
        }

        public String getExtraText() {
            return extraText;
        }

        public void setExtraText(String extraText) {
            this.extraText = extraText == null ? null : extraText.trim();
        }

        public Integer getExtraTextColor() {
            return extraTextColor;
        }

        public void setExtraTextColor(Integer extraTextColor) {
            this.extraTextColor = extraTextColor;
        }

        public Integer getExtraAction() {
            return extraAction;
        }

        public void setExtraAction(Integer extraAction) {
            this.extraAction = extraAction;
        }

        public String getExtraBody() {
            return extraBody;
        }

        public void setExtraBody(String extraBody) {
            this.extraBody = extraBody == null ? null : extraBody.trim();
        }

        public Integer getDialogStyle() {
            return dialogStyle;
        }

        public void setDialogStyle(Integer dialogStyle) {
            this.dialogStyle = dialogStyle;
        }

        public String getBackgroundUrl() {
            return backgroundUrl;
        }

        public void setBackgroundUrl(String backgroundUrl) {
            this.backgroundUrl = backgroundUrl == null ? null : backgroundUrl.trim();
        }

        public Boolean getSmartPop() {
            return smartPop;
        }

        public void setSmartPop(Boolean smartPop) {
            this.smartPop = smartPop;
        }

        public Boolean getCancelable() {
            return cancelable;
        }

        public void setCancelable(Boolean cancelable) {
            this.cancelable = cancelable;
        }

        @Override
        public String toString() {
            return "data{" +
                    "title='" + title + '\'' +
                    ", msg='" + msg + '\'' +
                    ", titleColor=" + titleColor +
                    ", msgColor=" + msgColor +
                    ", confirmText='" + confirmText + '\'' +
                    ", confirmTextColor=" + confirmTextColor +
                    ", confirmAction=" + confirmAction +
                    ", confirmBody='" + confirmBody + '\'' +
                    ", cancelText='" + cancelText + '\'' +
                    ", cancelTextColor=" + cancelTextColor +
                    ", extraText='" + extraText + '\'' +
                    ", extraTextColor=" + extraTextColor +
                    ", extraAction=" + extraAction +
                    ", extraBody='" + extraBody + '\'' +
                    ", dialogStyle=" + dialogStyle +
                    ", backgroundUrl='" + backgroundUrl + '\'' +
                    ", smartPop=" + smartPop +
                    ", cancelable=" + cancelable +
                    '}';
        }
    }

    public SoftNoticeInfo.data getData() {
        return data;
    }

    public void setData(SoftNoticeInfo.data data) {
        this.data = data;
    }
}
