package armadillo.studio.model.sys;

import java.util.List;

import armadillo.studio.model.Basic;

public class TaskInfo extends Basic {
    private List<data> data;

    public static class data {
        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public List<data> getData() {
        return data;
    }

    public void setData(List<data> data) {
        this.data = data;
    }
}
