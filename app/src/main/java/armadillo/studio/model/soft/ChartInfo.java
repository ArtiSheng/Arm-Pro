/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.soft;

import java.io.Serializable;

public class ChartInfo implements Serializable {
    private String time;
    private int usr_count;
    private int start_count;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUsr_count() {
        return usr_count;
    }

    public void setUsr_count(int usr_count) {
        this.usr_count = usr_count;
    }

    public int getStart_count() {
        return start_count;
    }

    public void setStart_count(int start_count) {
        this.start_count = start_count;
    }
}
