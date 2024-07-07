/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.handle;

import java.io.Serializable;

public class ConfigRule implements Serializable {
    private String def;
    private String desc;
    private String name;
    private boolean checkbox;
    private boolean checked;

    public ConfigRule(String def, String desc, String name, boolean checkbox, boolean checked) {
        this.def = def;
        this.desc = desc;
        this.name = name;
        this.checkbox = checkbox;
        this.checked = checked;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
