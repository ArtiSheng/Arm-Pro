/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.soft;

import java.util.List;

import armadillo.studio.model.Basic;

public class SoftSingleTrialInfo extends Basic {
    private List<data> data;

    public static class data {

        private Integer count;

        private String lastTime;

        private String mac;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getLastTime() {
            return lastTime;
        }

        public void setLastTime(String lastTime) {
            this.lastTime = lastTime;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac == null ? null : mac.trim();
        }

    }

    public List<data> getData() {
        return data;
    }

    public void setData(List<data> data) {
        this.data = data;
    }
}
