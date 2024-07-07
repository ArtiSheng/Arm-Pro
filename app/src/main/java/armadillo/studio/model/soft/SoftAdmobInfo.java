/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.model.soft;

import armadillo.studio.model.Basic;

public class SoftAdmobInfo extends Basic {
    private data data;

    public static class data {
        private Integer handle;

        private String bannerIds;

        private String interstitialIds;

        private String rewardedIds;

        private String openIds;

        private String rules;

        public Integer getHandle() {
            return handle;
        }

        public void setHandle(Integer handle) {
            this.handle = handle;
        }

        public String getBannerIds() {
            return bannerIds;
        }

        public void setBannerIds(String bannerIds) {
            this.bannerIds = bannerIds == null ? null : bannerIds.trim();
        }

        public String getInterstitialIds() {
            return interstitialIds;
        }

        public void setInterstitialIds(String interstitialIds) {
            this.interstitialIds = interstitialIds == null ? null : interstitialIds.trim();
        }

        public String getRewardedIds() {
            return rewardedIds;
        }

        public void setRewardedIds(String rewardedIds) {
            this.rewardedIds = rewardedIds == null ? null : rewardedIds.trim();
        }

        public String getOpenIds() {
            return openIds;
        }

        public void setOpenIds(String openIds) {
            this.openIds = openIds == null ? null : openIds.trim();
        }

        public String getRules() {
            return rules;
        }

        public void setRules(String rules) {
            this.rules = rules == null ? null : rules.trim();
        }
    }

    public SoftAdmobInfo.data getData() {
        return data;
    }

    public void setData(SoftAdmobInfo.data data) {
        this.data = data;
    }
}
