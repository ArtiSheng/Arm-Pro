/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.sys;

import java.io.Serializable;

import armadillo.studio.model.Basic;

public class Help extends Basic implements Serializable {
    public String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
