/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.soft;

import java.util.List;

import armadillo.studio.model.Basic;

public class SoftSingleCardInfo extends Basic {
    private List<data> data;

    public static class data {

        private String card;

        private Integer value;

        private Integer type;

        private String mac;

        private String mark;

        private String usrTime;

        private Integer usrCount;

        private Boolean usable;


        public String getCard() {
            return card;
        }

        public void setCard(String card) {
            this.card = card == null ? null : card.trim();
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac == null ? null : mac.trim();
        }

        public String getMark() {
            return mark;
        }

        public void setMark(String mark) {
            this.mark = mark == null ? null : mark.trim();
        }

        public String getUsrTime() {
            return usrTime;
        }

        public void setUsrTime(String usrTime) {
            this.usrTime = usrTime;
        }

        public Integer getUsrCount() {
            return usrCount;
        }

        public void setUsrCount(Integer usrCount) {
            this.usrCount = usrCount;
        }

        public Boolean getUsable() {
            return usable;
        }

        public void setUsable(Boolean usable) {
            this.usable = usable;
        }
    }

    public List<SoftSingleCardInfo.data> getData() {
        return data;
    }

    public void setData(List<SoftSingleCardInfo.data> data) {
        this.data = data;
    }
}
