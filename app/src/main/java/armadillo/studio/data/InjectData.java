/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.data;

import com.google.gson.JsonElement;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class InjectData {
    private AtomicReference<File> file = new AtomicReference<>();
    private String packName;
    private int size;
    private HashMap<String, JsonElement> otherRule;
    private boolean isOk;
    private Throwable throwable;

    public InjectData(File file,
                      String packName,
                      int size,
                      HashMap<String, JsonElement> otherRule,
                      boolean isOk,
                      Throwable throwable) {
        if (file != null)
            this.file.set(file);
        this.packName = packName;
        this.size = size;
        this.otherRule = otherRule;
        this.isOk = isOk;
        this.throwable = throwable;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public AtomicReference<File> getFile() {
        return file;
    }

    public void setFile(AtomicReference<File> file) {
        this.file = file;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public void addRule(String name, JsonElement value) {
        if (otherRule != null)
            this.otherRule.put(name, value);
    }

    public HashMap<String, JsonElement> getOtherRule() {
        return otherRule;
    }

    public boolean isOk() {
        return isOk;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
