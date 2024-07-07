/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.soft;

import armadillo.studio.model.Basic;

public class SoftCustomInfo extends Basic {
    private data data;
    public static class data {
        private Integer customLoaderMode;

        private String customLoaderPath;

        private Integer customInvokeMode;

        private String customInvokeRule;

        public Integer getCustomLoaderMode() {
            return customLoaderMode;
        }

        public void setCustomLoaderMode(Integer customLoaderMode) {
            this.customLoaderMode = customLoaderMode;
        }

        public String getCustomLoaderPath() {
            return customLoaderPath;
        }

        public void setCustomLoaderPath(String customLoaderPath) {
            this.customLoaderPath = customLoaderPath == null ? null : customLoaderPath.trim();
        }

        public Integer getCustomInvokeMode() {
            return customInvokeMode;
        }

        public void setCustomInvokeMode(Integer customInvokeMode) {
            this.customInvokeMode = customInvokeMode;
        }

        public String getCustomInvokeRule() {
            return customInvokeRule;
        }

        public void setCustomInvokeRule(String customInvokeRule) {
            this.customInvokeRule = customInvokeRule == null ? null : customInvokeRule.trim();
        }

    }

    public SoftCustomInfo.data getData() {
        return data;
    }

    public void setData(SoftCustomInfo.data data) {
        this.data = data;
    }
}
