package armadillo.studio.model.sys;

import java.util.List;

import armadillo.studio.model.Basic;

public class Notice extends Basic {
    private List<data> data;

    public static class data {
        private String title;

        private long time;

        private String msg;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }

    public List<Notice.data> getData() {
        return data;
    }

    public void setData(List<Notice.data> data) {
        this.data = data;
    }

}
