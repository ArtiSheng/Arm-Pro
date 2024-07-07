/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package armadillo.studio.model.sys;

import armadillo.studio.model.Basic;

public class Upload extends Basic {
    private data data;

    public static class data {
        private String uuid;
        private String token;
        private boolean cache;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean isCache() {
            return cache;
        }

        public void setCache(boolean cache) {
            this.cache = cache;
        }
    }

    public Upload.data getData() {
        return data;
    }

    public void setData(Upload.data data) {
        this.data = data;
    }
}
