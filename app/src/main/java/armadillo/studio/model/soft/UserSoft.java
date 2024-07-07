package armadillo.studio.model.soft;


import java.io.Serializable;
import java.util.List;

import armadillo.studio.model.Basic;

public class UserSoft extends Basic {
    private List<data> data;

    public static class data implements Serializable {

        private Integer id;

        private String appkey;

        private String name;

        private String packageName;

        private Integer version;

        private Integer handle;

        private Integer total_user;

        private List<ChartInfo> chartInfos;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getAppkey() {
            return appkey;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey == null ? null : appkey.trim();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name == null ? null : name.trim();
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName == null ? null : packageName.trim();
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public Integer getHandle() {
            return handle;
        }

        public void setHandle(Integer handle) {
            this.handle = handle;
        }

        public Integer getTotal_user() {
            return total_user;
        }

        public void setTotal_user(Integer total_user) {
            this.total_user = total_user;
        }

        public List<ChartInfo> getChartInfos() {
            return chartInfos;
        }

        public void setChartInfos(List<ChartInfo> chartInfos) {
            this.chartInfos = chartInfos;
        }
    }

    public List<UserSoft.data> getData() {
        return data;
    }

    public void setData(List<UserSoft.data> data) {
        this.data = data;
    }

}
