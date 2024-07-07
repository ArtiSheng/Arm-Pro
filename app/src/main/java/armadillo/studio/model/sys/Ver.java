package armadillo.studio.model.sys;

import java.util.List;

import armadillo.studio.model.Basic;

public class Ver extends Basic {

    private List<data> data;

    public static class data {
        private Integer id;

        private Integer version;

        private String versionName;

        private Boolean versionMode;

        private String versionMsg;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName == null ? null : versionName.trim();
        }

        public Boolean getVersionMode() {
            return versionMode;
        }

        public void setVersionMode(Boolean versionMode) {
            this.versionMode = versionMode;
        }

        public String getVersionMsg() {
            return versionMsg;
        }

        public void setVersionMsg(String versionMsg) {
            this.versionMsg = versionMsg == null ? null : versionMsg.trim();
        }
    }

    public List<data> getData() {
        return data;
    }

    public void setData(List<data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString() + "notice{" +
                "data=" + data.toString() +
                '}';
    }

}